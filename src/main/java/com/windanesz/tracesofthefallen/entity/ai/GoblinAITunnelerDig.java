package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityGoblinTunneler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class GoblinAITunnelerDig extends EntityAIBase {
	private final EntityGoblinTunneler tunneler;
	private final World world;
	private EntityPlayer targetPlayer;
	private BlockPos targetDigPos;
	private BlockPos startDescentColumn;
	private EnumFacing digFacing;
	private int digTimer;
	private int maxBreakTime;

	private BlockPos lastPlayerPos;
	private int playerStationaryTicks;
	private EnumFacing tunnelFacing;
	private BlockPos goalExitColumn;
	private int tunnelFloorY;

	public GoblinAITunnelerDig(EntityGoblinTunneler tunneler) {
		this.tunneler = tunneler;
		this.world = tunneler.world;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.tunneler.getBlocksDug() >= 100 || this.tunneler.getHeldItemMainhand().isEmpty()) {
			return false;
		}

		if (!this.tunneler.getPlannedBlocks().isEmpty()) {
			return true;
		}

		if (this.tunneler.getBlocksDug() > 0) {
			return false;
		}

		if (this.tunneler.getAttackTarget() instanceof EntityPlayer) {
			this.targetPlayer = (EntityPlayer) this.tunneler.getAttackTarget();
		} else {
			this.targetPlayer = this.world.getClosestPlayerToEntity(this.tunneler, 64.0D);
		}
		if (this.targetPlayer == null || this.targetPlayer.isDead || this.targetPlayer.isSpectator()) {
			this.playerStationaryTicks = 0;
			return false;
		}

		BlockPos currentPlayerPos = new BlockPos(this.targetPlayer);
		if (this.lastPlayerPos == null || !this.lastPlayerPos.equals(currentPlayerPos)) {
			this.lastPlayerPos = currentPlayerPos;
			this.playerStationaryTicks = 0;
			return false;
		} else {
			this.playerStationaryTicks++;
		}

		if (this.playerStationaryTicks >= 60) {
			generateTunnelPlan(new BlockPos(this.tunneler.posX, this.tunneler.posY + 0.2D, this.tunneler.posZ), currentPlayerPos);
			return !this.tunneler.getPlannedBlocks().isEmpty();
		}

		return false;
	}

	private void generateTunnelPlan(BlockPos startPos, BlockPos goalPos) {
		this.tunneler.getPlannedBlocks().clear();
		EnumFacing facing = EnumFacing.getFacingFromVector((float)(goalPos.getX() - startPos.getX()), 0, (float)(goalPos.getZ() - startPos.getZ()));
		if (facing == null || facing.getAxis() == EnumFacing.Axis.Y) {
			facing = this.tunneler.getHorizontalFacing();
		}
		this.tunnelFacing = facing;
		this.goalExitColumn = goalPos;

		int startY = startPos.getY();
		int tunnelY = startY - 3;
		if (tunnelY < 1) tunnelY = 1;
		this.tunnelFloorY = tunnelY;

		// 1. Dig 3 blocks down
		BlockPos downColumn = startPos.offset(facing);
		this.startDescentColumn = downColumn;
		for (int y = startY + 1; y >= tunnelY; y--) {
			this.tunneler.addPlannedBlock(new BlockPos(downColumn.getX(), y, downColumn.getZ()));
		}

		// 2. Dig horizontally forward at tunnelY
		int currX = downColumn.getX();
		int currZ = downColumn.getZ();
		int goalX = goalPos.getX();
		int goalZ = goalPos.getZ();

		while (currX != goalX || currZ != goalZ) {
			if (Math.abs(goalX - currX) > Math.abs(goalZ - currZ)) {
				currX += Integer.signum(goalX - currX);
			} else {
				currZ += Integer.signum(goalZ - currZ);
			}
			this.tunneler.addPlannedBlock(new BlockPos(currX, tunnelY + 1, currZ));
			this.tunneler.addPlannedBlock(new BlockPos(currX, tunnelY, currZ));
		}

		// 3. Dig up at goalX, goalZ until the tunnel sees the sky (and at least reaches goalY + 1)
		int goalY = goalPos.getY();
		for (int y = tunnelY + 2; y < 255; y++) {
			BlockPos upPos = new BlockPos(goalX, y, goalZ);
			this.tunneler.addPlannedBlock(upPos);
			if (y >= goalY + 1 && this.world.canSeeSky(upPos)) {
				break;
			}
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.tunneler.getPlannedBlocks().isEmpty()
				&& this.tunneler.getBlocksDug() < 100
				&& !this.tunneler.getHeldItemMainhand().isEmpty()
				&& this.targetPlayer != null && !this.targetPlayer.isDead && !this.targetPlayer.isSpectator();
	}

	@Override
	public void startExecuting() {
		this.targetDigPos = null;
		this.tunneler.getNavigator().clearPath();
	}

	@Override
	public void resetTask() {
		if (this.targetDigPos != null) {
			this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, -1);
		}
		this.targetDigPos = null;
	}

	@Override
	public void updateTask() {
		if (this.targetDigPos == null) {
			while (!this.tunneler.getPlannedBlocks().isEmpty()) {
				BlockPos planned = this.tunneler.getPlannedBlocks().get(0);
				boolean isAirOrLadder = this.world.isAirBlock(planned) || this.world.getBlockState(planned).getBlock() == Blocks.LADDER;

				if (isAirOrLadder) {
					if (this.tunneler.getDistanceSqToCenter(planned) <= 2.5D) {
						this.tunneler.getPlannedBlocks().remove(0);
						continue;
					} else {
						moveTowardPlanned(planned);
						return;
					}
				}

				if (canMine(planned)) {
					double distSq = this.tunneler.getDistanceSqToCenter(planned);
					double dy = planned.getY() - this.tunneler.posY;
					if (distSq <= 5.0D && dy >= -1.6D) {
						this.targetDigPos = planned;
						this.digTimer = 0;
						IBlockState state = this.world.getBlockState(this.targetDigPos);
						float hardness = state.getBlockHardness(this.world, this.targetDigPos);
						Integer overrideTicks = getBlockBreakOverride(state);
						if (overrideTicks != null && overrideTicks > 0) {
							this.maxBreakTime = overrideTicks;
						} else {
							this.maxBreakTime = Math.max(1, (int) Math.round(hardness * Settings.miscSettings.tunnelerGlobalBlockBreakTime));
						}
						this.tunneler.getNavigator().clearPath();
						EnumFacing facingToPlanned = EnumFacing.getFacingFromVector((float)(planned.getX() - this.tunneler.posX), 0, (float)(planned.getZ() - this.tunneler.posZ));
						this.digFacing = (facingToPlanned != null && facingToPlanned.getAxis() != EnumFacing.Axis.Y) ? facingToPlanned : this.tunneler.getHorizontalFacing();
						break;
					} else {
						moveTowardPlanned(planned);
						return;
					}
				} else {
					this.tunneler.getPlannedBlocks().remove(0);
				}
			}
			return;
		}

		if (this.targetDigPos.getY() > this.tunneler.posY + 0.2D) {
			if (this.tunneler.isOnLadder() || isAdjacentToLadder()) {
				this.tunneler.motionY = 0.08D;
			}
		}

		this.tunneler.getLookHelper().setLookPosition(this.targetDigPos.getX() + 0.5D, this.targetDigPos.getY() + 0.5D, this.targetDigPos.getZ() + 0.5D, 30.0F, 30.0F);
		if (this.digTimer % 5 == 0) {
			this.tunneler.swingArm(EnumHand.MAIN_HAND);
			IBlockState state = this.world.getBlockState(this.targetDigPos);
			SoundType soundType = state.getBlock().getSoundType(state, this.world, this.targetDigPos, this.tunneler);
			this.world.playSound(null, this.targetDigPos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
		}
		this.digTimer++;
		int progress = (int)((float)this.digTimer / (float)this.maxBreakTime * 10.0F);
		this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, progress);

		if (this.digTimer >= this.maxBreakTime) {
			this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, -1);
			this.world.destroyBlock(this.targetDigPos, true);

			boolean isDescentColumn = (this.startDescentColumn != null && this.targetDigPos.getX() == this.startDescentColumn.getX() && this.targetDigPos.getZ() == this.startDescentColumn.getZ());
			boolean isExitColumn = (this.goalExitColumn != null && this.targetDigPos.getX() == this.goalExitColumn.getX() && this.targetDigPos.getZ() == this.goalExitColumn.getZ() && this.targetDigPos.getY() >= this.tunnelFloorY);
			if (isDescentColumn || isExitColumn) {
				tryAttachLadderToSolidWall(this.targetDigPos, this.digFacing);
			}

			this.tunneler.getPlannedBlocks().remove(this.targetDigPos);
			this.tunneler.addDugBlock(this.targetDigPos);

			int dug = this.tunneler.getBlocksDug();
			if (this.tunneler.getTorchesInStash() > 0) {
				if ((dug >= 17 && this.tunneler.getTorchesInStash() == 2) || (dug >= 34 && this.tunneler.getTorchesInStash() >= 1)) {
					tryPlaceTorchNear(this.targetDigPos);
				}
			}

			if (this.tunneler.getBlocksDug() >= 100) {
				this.tunneler.renderBrokenItemStack(this.tunneler.getHeldItemMainhand());
				this.tunneler.world.playSound(null, this.tunneler.posX, this.tunneler.posY, this.tunneler.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 0.8F, 0.8F + this.tunneler.world.rand.nextFloat() * 0.4F);
				this.tunneler.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
			}
			this.targetDigPos = null;
		}
	}

	private boolean canMine(BlockPos p) {
		if (this.world.isAirBlock(p)) return false;
		IBlockState state = this.world.getBlockState(p);
		if (state.getBlock() == Blocks.LADDER) return false;
		if (state.getBlockHardness(this.world, p) < 0) return false;
		if (state.getMaterial().isLiquid()) return false;

		String maxLevelStr = Settings.miscSettings.tunnelerToolProgressionMaxMiningLevel;
		if (maxLevelStr != null && !maxLevelStr.trim().isEmpty()) {
			try {
				int maxLevel = Integer.parseInt(maxLevelStr.trim());
				int blockLevel = state.getBlock().getHarvestLevel(state);
				if (blockLevel > maxLevel) {
					return false;
				}
			} catch (NumberFormatException e) {
				// Ignore invalid setting
			}
		}

		return true;
	}

	private void moveTowardPlanned(BlockPos planned) {
		this.tunneler.getNavigator().tryMoveToXYZ(planned.getX() + 0.5D, planned.getY(), planned.getZ() + 0.5D, 1.0D);
		double dy = planned.getY() - this.tunneler.posY;
		double dx = (planned.getX() + 0.5D) - this.tunneler.posX;
		double dz = (planned.getZ() + 0.5D) - this.tunneler.posZ;
		double distHorizontal = Math.sqrt(dx * dx + dz * dz);

		if (dy < -1.0D) {
			if (distHorizontal > 0.05D) {
				this.tunneler.motionX = (dx / distHorizontal) * 0.15D;
				this.tunneler.motionZ = (dz / distHorizontal) * 0.15D;
			}
		} else if (dy > 0.2D) {
			if (this.tunneler.isOnLadder() || isAdjacentToLadder()) {
				this.tunneler.motionY = 0.25D;
				if (distHorizontal > 0.05D) {
					this.tunneler.motionX = (dx / distHorizontal) * 0.15D;
					this.tunneler.motionZ = (dz / distHorizontal) * 0.15D;
				}
			} else {
				if (distHorizontal > 0.05D) {
					this.tunneler.motionX = (dx / distHorizontal) * 0.2D;
					this.tunneler.motionZ = (dz / distHorizontal) * 0.2D;
				}
			}
		}
	}

	private boolean isAdjacentToLadder() {
		BlockPos p = new BlockPos(this.tunneler);
		if (this.world.getBlockState(p).getBlock() == Blocks.LADDER) return true;
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (this.world.getBlockState(p.offset(facing)).getBlock() == Blocks.LADDER) return true;
		}
		return false;
	}

	private void tryAttachLadderToSolidWall(BlockPos pos, EnumFacing preferredFacing) {
		if (!this.world.isAirBlock(pos)) return;

		// 1. Check if there is ANY existing ladder in this vertical shaft column (above or below up to 5 blocks).
		// All ladders in a vertical shaft MUST align on the exact same wall facing so goblins don't get stuck.
		EnumFacing consistentFacing = null;
		for (int dy = -5; dy <= 5; dy++) {
			if (dy == 0) continue;
			BlockPos colPos = new BlockPos(pos.getX(), pos.getY() + dy, pos.getZ());
			IBlockState colState = this.world.getBlockState(colPos);
			if (colState.getBlock() == Blocks.LADDER) {
				consistentFacing = colState.getValue(BlockLadder.FACING);
				break;
			}
		}

		if (consistentFacing != null) {
			BlockPos wall = pos.offset(consistentFacing.getOpposite());
			if (this.world.isAirBlock(wall)) {
				this.world.setBlockState(wall, Blocks.COBBLESTONE.getDefaultState());
			}
			if (this.world.getBlockState(wall).isSideSolid(this.world, wall, consistentFacing)) {
				this.world.setBlockState(pos, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, consistentFacing));
				return;
			}
		}

		// 2. If no existing ladder in this column yet, prefer the tunnel's consistent main facing or preferredFacing
		EnumFacing targetFacing = (this.tunnelFacing != null) ? this.tunnelFacing : preferredFacing;
		if (targetFacing != null && targetFacing.getAxis() != EnumFacing.Axis.Y) {
			BlockPos preferredWall = pos.offset(targetFacing.getOpposite());
			if (this.world.getBlockState(preferredWall).isSideSolid(this.world, preferredWall, targetFacing)) {
				this.world.setBlockState(pos, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, targetFacing));
				return;
			}
		}

		// 3. Otherwise, use the first solid horizontal wall found
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			BlockPos wall = pos.offset(side.getOpposite());
			if (this.world.getBlockState(wall).isSideSolid(this.world, wall, side)) {
				this.world.setBlockState(pos, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, side));
				return;
			}
		}
	}

	private void tryPlaceTorchNear(BlockPos center) {
		BlockPos[] candidates = new BlockPos[]{
				center, center.up(),
				new BlockPos(this.tunneler), new BlockPos(this.tunneler).up(),
				center.offset(this.digFacing.getOpposite()), center.offset(this.digFacing.getOpposite()).up()
		};
		for (BlockPos p : candidates) {
			if (this.world.isAirBlock(p)) {
				if (this.world.getBlockState(p.down()).isTopSolid()) {
					this.world.setBlockState(p, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP));
					this.tunneler.setTorchesInStash(this.tunneler.getTorchesInStash() - 1);
					return;
				}
				for (EnumFacing side : EnumFacing.HORIZONTALS) {
					BlockPos wall = p.offset(side.getOpposite());
					if (this.world.getBlockState(wall).isSideSolid(this.world, wall, side)) {
						this.world.setBlockState(p, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, side));
						this.tunneler.setTorchesInStash(this.tunneler.getTorchesInStash() - 1);
						return;
					}
				}
			}
		}
	}

	private static Map<String, Integer> breakOverrideCache = null;

	public static void clearBreakOverridesCache() {
		breakOverrideCache = null;
	}

	private static Integer getBlockBreakOverride(IBlockState state) {
		if (breakOverrideCache == null) {
			breakOverrideCache = new HashMap<>();
			for (String entry : Settings.miscSettings.tunnelerBlockBreakTimeOverrides) {
				if (entry == null || entry.trim().isEmpty()) continue;
				String[] parts = entry.split(":");
				if (parts.length < 3) continue;
				try {
					boolean hasMeta = parts.length == 4;
					String key = hasMeta ? parts[0] + ":" + parts[1] + ":" + parts[2] : parts[0] + ":" + parts[1];
					int ticks = Integer.parseInt(hasMeta ? parts[3] : parts[2]);
					breakOverrideCache.put(key, ticks);
				} catch (NumberFormatException e) {
					com.windanesz.tracesofthefallen.TracesOfTheFallen.LOGGER.warn("Invalid tunneler block break override: " + entry);
				}
			}
		}
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		String blockId = block.getRegistryName().toString();
		Integer ticks = breakOverrideCache.get(blockId + ":" + meta);
		if (ticks == null) {
			ticks = breakOverrideCache.get(blockId);
		}
		return ticks;
	}
}
