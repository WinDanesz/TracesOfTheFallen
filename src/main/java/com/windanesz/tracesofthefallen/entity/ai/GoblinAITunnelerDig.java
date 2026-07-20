package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class GoblinAITunnelerDig extends EntityAIBase {

	private static final int MAX_BLOCKS_DUG = 100;
	private static final int PLAYER_STATIONARY_TICKS_REQUIRED = 60;

	private static final int STUCK_TICKS_BEFORE_RECOVERY = 60;
	private static final int MAX_RECOVERY_ATTEMPTS = 6;
	private static final int REPLAN_COOLDOWN_AFTER_ABORT = 100;

	private static final double DIG_START_RANGE_SQ = 5.0D;
	private static final double DIG_CANCEL_RANGE_SQ = 6.25D;
	private static final double MAX_DIG_BELOW = -1.6D;

	private static final double WAYPOINT_HORIZONTAL_TOLERANCE = 0.60D;
	private static final double WAYPOINT_VERTICAL_TOLERANCE = 0.70D;

	private final EntityGoblinTunneler tunneler;
	private final World world;

	private EntityPlayer targetPlayer;

	private BlockPos targetDigPos;
	private EnumFacing digFacing;
	private int digTimer;
	private int maxBreakTime;

	private BlockPos lastPlayerPos;
	private int playerStationaryTicks;
	private int replanCooldown;

	private EnumFacing tunnelFacing;
	private EnumFacing exitApproachFacing;
	private BlockPos startDescentColumn;
	private BlockPos goalExitColumn;
	private int tunnelFloorY;

	private EnumFacing descentLadderFacing;
	private EnumFacing exitLadderFacing;
	private int ladderGapGraceTicks;

	private BlockPos progressTarget;
	private double bestProgressDistanceSq = Double.MAX_VALUE;
	private int stuckTimer;
	private int recoveryAttempts;

	private BlockPos failedBreakPos;
	private int failedBreakAttempts;

	public GoblinAITunnelerDig(EntityGoblinTunneler tunneler) {
		this.tunneler = tunneler;
		this.world = tunneler.world;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.tunneler.getRevengeTarget() != null) {
			return false;
		}

		if (this.tunneler.getBlocksDug() >= MAX_BLOCKS_DUG || this.tunneler.getHeldItemMainhand().isEmpty()) {
			return false;
		}

		// Once a tunnel has been planned, finish it even if the original player
		// moves away, dies, or temporarily stops being a valid target.
		if (!this.tunneler.getPlannedBlocks().isEmpty()) {
			return true;
		}

		if (this.replanCooldown > 0) {
			this.replanCooldown--;
			return false;
		}

		// A tunneler that already started digging should never generate a second,
		// unrelated tunnel after its original plan has finished or been aborted.
		if (this.tunneler.getBlocksDug() > 0) {
			return false;
		}

		this.targetPlayer = this.findTargetPlayer();
		if (this.targetPlayer == null || this.targetPlayer.isDead || this.targetPlayer.isSpectator()) {
			this.playerStationaryTicks = 0;
			this.lastPlayerPos = null;
			return false;
		}

		BlockPos currentPlayerPos = new BlockPos(this.targetPlayer);
		if (this.lastPlayerPos == null || !this.lastPlayerPos.equals(currentPlayerPos)) {
			this.lastPlayerPos = currentPlayerPos;
			this.playerStationaryTicks = 0;
			return false;
		}

		this.playerStationaryTicks++;

		if (this.playerStationaryTicks >= PLAYER_STATIONARY_TICKS_REQUIRED) {
			this.generateTunnelPlan(
					new BlockPos(this.tunneler.posX, this.tunneler.posY + 0.2D, this.tunneler.posZ),
					currentPlayerPos
			);
			return !this.tunneler.getPlannedBlocks().isEmpty();
		}

		return false;
	}

	private EntityPlayer findTargetPlayer() {
		if (this.tunneler.getAttackTarget() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) this.tunneler.getAttackTarget();
			if (isValidHostileTarget(player)) {
				return player;
			}
		}

		if (this.tunneler.hasOwner() && this.tunneler.getOwner() instanceof EntityPlayer) {
			EntityPlayer owner = (EntityPlayer) this.tunneler.getOwner();
			if (!owner.isDead && !owner.isSpectator()) {
				return owner;
			}
		}

		if (!this.tunneler.hasOwner() && !this.tunneler.isNeutral()) {
			EntityPlayer closest = null;
			double closestDistanceSq = 64.0D * 64.0D;

			for (EntityPlayer player : this.world.playerEntities) {
				if (!isValidHostileTarget(player) || this.tunneler.isOwner(player)) {
					continue;
				}

				double distanceSq = this.tunneler.getDistanceSq(
						player.posX,
						player.posY,
						player.posZ
				);

				if (distanceSq < closestDistanceSq) {
					closestDistanceSq = distanceSq;
					closest = player;
				}
			}

			return closest;
		}

		return null;
	}

	private boolean isValidHostileTarget(EntityPlayer player) {
		return player != null
				&& !player.isDead
				&& !player.isSpectator()
				&& !player.isCreative();
	}

	private void generateTunnelPlan(BlockPos startPos, BlockPos goalPos) {
		this.tunneler.getPlannedBlocks().clear();

		this.targetDigPos = null;
		this.startDescentColumn = null;
		this.goalExitColumn = null;
		this.descentLadderFacing = null;
		this.exitLadderFacing = null;
		this.exitApproachFacing = null;
		this.failedBreakPos = null;
		this.failedBreakAttempts = 0;
		this.ladderGapGraceTicks = 0;

		int horizontalDx = goalPos.getX() - startPos.getX();
		int horizontalDz = goalPos.getZ() - startPos.getZ();

		EnumFacing facing;
		if (horizontalDx == 0 && horizontalDz == 0) {
			facing = this.tunneler.getHorizontalFacing();
		} else {
			facing = EnumFacing.getFacingFromVector((float) horizontalDx, 0.0F, (float) horizontalDz);
			if (facing.getAxis() == EnumFacing.Axis.Y) {
				facing = this.tunneler.getHorizontalFacing();
			}
		}

		this.tunnelFacing = facing;
		this.goalExitColumn = new BlockPos(goalPos.getX(), goalPos.getY(), goalPos.getZ());

		int startY = startPos.getY();
		int tunnelY = Math.max(1, startY - 3);
		this.tunnelFloorY = tunnelY;

		// 1. Create the descent shaft one block in front of the tunneler.
		BlockPos downColumn = startPos.offset(facing);
		this.startDescentColumn = new BlockPos(downColumn.getX(), downColumn.getY(), downColumn.getZ());

		for (int y = startY + 1; y >= tunnelY; y--) {
			this.tunneler.addPlannedBlock(new BlockPos(downColumn.getX(), y, downColumn.getZ()));
		}

		// 2. Dig a two-block-high horizontal tunnel.
		// Upper/clearance block is deliberately planned before the foot-level block,
		// so the goblin does not open a one-block-high hole and walk into it.
		int currX = downColumn.getX();
		int currZ = downColumn.getZ();
		int goalX = goalPos.getX();
		int goalZ = goalPos.getZ();
		EnumFacing lastStepFacing = facing;

		while (currX != goalX || currZ != goalZ) {
			int stepX = 0;
			int stepZ = 0;

			if (Math.abs(goalX - currX) > Math.abs(goalZ - currZ)) {
				stepX = Integer.signum(goalX - currX);
			} else {
				stepZ = Integer.signum(goalZ - currZ);
			}

			currX += stepX;
			currZ += stepZ;

			if (stepX > 0) {
				lastStepFacing = EnumFacing.EAST;
			} else if (stepX < 0) {
				lastStepFacing = EnumFacing.WEST;
			} else if (stepZ > 0) {
				lastStepFacing = EnumFacing.SOUTH;
			} else if (stepZ < 0) {
				lastStepFacing = EnumFacing.NORTH;
			}

			this.tunneler.addPlannedBlock(new BlockPos(currX, tunnelY + 1, currZ));
			this.tunneler.addPlannedBlock(new BlockPos(currX, tunnelY, currZ));
		}

		this.exitApproachFacing = lastStepFacing;

		// 3. Dig upward at the destination until the shaft reaches open sky,
		// while always reaching at least one block above the original goal.
		int goalY = goalPos.getY();
		for (int y = tunnelY + 2; y < this.world.getActualHeight(); y++) {
			BlockPos upPos = new BlockPos(goalX, y, goalZ);
			this.tunneler.addPlannedBlock(upPos);

			if (y >= goalY + 1 && this.world.canSeeSky(upPos)) {
				break;
			}
		}

		this.resetMovementTracking();
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.tunneler.getRevengeTarget() != null) {
			// Do not destroy the plan. Combat may temporarily interrupt tunneling,
			// and the goblin should resume the same tunnel afterwards.
			return false;
		}

		return !this.tunneler.getPlannedBlocks().isEmpty()
				&& this.tunneler.getBlocksDug() < MAX_BLOCKS_DUG
				&& !this.tunneler.getHeldItemMainhand().isEmpty();
	}

	@Override
	public void startExecuting() {
		this.clearCurrentDigTarget();
		this.tunneler.getNavigator().clearPath();

		this.stuckTimer = 0;
		this.recoveryAttempts = 0;
		this.ladderGapGraceTicks = 0;
		this.resetMovementTracking();
	}

	@Override
	public void resetTask() {
		this.clearCurrentDigTarget();
		this.tunneler.getNavigator().clearPath();

		this.stuckTimer = 0;
		this.recoveryAttempts = 0;
		this.ladderGapGraceTicks = 0;
		this.resetMovementTracking();
	}

	@Override
	public void updateTask() {
		if (this.targetDigPos != null) {
			this.updateDigging();
			return;
		}

		this.processNextPlannedBlock();
	}

	private void processNextPlannedBlock() {
		while (!this.tunneler.getPlannedBlocks().isEmpty()) {
			BlockPos planned = this.tunneler.getPlannedBlocks().get(0);
			IBlockState state = this.world.getBlockState(planned);

			boolean isAir = this.world.isAirBlock(planned);
			boolean isLadder = state.getBlock() == Blocks.LADDER;
			boolean isPassable = isAir || isLadder;

			if (isPassable) {
				// Vertical plan entries describe excavation, not individual movement
				// waypoints. Existing air/ladder cells need no action and must not cause
				// extra ladder placement. The next solid shaft block drives traversal.
				if (this.isVerticalShaftPosition(planned) || this.isHorizontalClearanceBlock(planned)) {
					this.consumePlannedBlock(planned);
					this.stuckTimer = 0;
					this.recoveryAttempts = 0;
					this.resetMovementTracking();
					continue;
				}

				// Foot-level horizontal cells are actual waypoints so the tunneler
				// continues advancing through the tunnel it opened.
				if (this.hasReachedPlannedPosition(planned)) {
					this.consumePlannedBlock(planned);
					this.stuckTimer = 0;
					this.recoveryAttempts = 0;
					this.resetMovementTracking();
					continue;
				}

				this.moveTowardPlanned(planned, false);
				this.trackMovementAndRecover(planned);
				return;
			}

			if (!this.canMine(planned)) {
				this.abortCurrentPlan();
				return;
			}

			double distSq = this.tunneler.getDistanceSqToCenter(planned);
			double dy = planned.getY() - this.tunneler.posY;

			if (distSq <= DIG_START_RANGE_SQ && dy >= MAX_DIG_BELOW) {
				this.beginDigging(planned);
				return;
			}

			this.moveTowardPlanned(planned, true);
			this.trackMovementAndRecover(planned);
			return;
		}
	}

	private void beginDigging(BlockPos planned) {
		IBlockState state = this.world.getBlockState(planned);
		if (!this.canMine(planned)) {
			return;
		}

		this.targetDigPos = planned;
		this.digTimer = 0;

		float hardness = state.getBlockHardness(this.world, planned);
		Integer overrideTicks = getBlockBreakOverride(state);

		if (overrideTicks != null && overrideTicks > 0) {
			this.maxBreakTime = overrideTicks;
		} else {
			double calculated = hardness * Settings.miscSettings.tunnelerGlobalBlockBreakTime;
			this.maxBreakTime = Math.max(1, (int) Math.round(calculated));
		}

		this.tunneler.getNavigator().clearPath();

		double dx = (planned.getX() + 0.5D) - this.tunneler.posX;
		double dz = (planned.getZ() + 0.5D) - this.tunneler.posZ;

		if (Math.abs(dx) > 0.01D || Math.abs(dz) > 0.01D) {
			EnumFacing facingToPlanned = EnumFacing.getFacingFromVector((float) dx, 0.0F, (float) dz);
			this.digFacing = facingToPlanned.getAxis() == EnumFacing.Axis.Y
					? this.getSafeHorizontalFacing()
					: facingToPlanned;
		} else {
			this.digFacing = this.getSafeHorizontalFacing();
		}

		this.stuckTimer = 0;
		this.recoveryAttempts = 0;
		this.resetMovementTracking();
	}

	private void updateDigging() {
		if (this.targetDigPos == null) {
			return;
		}

		// If the plan changed underneath this task, stop rendering the old break.
		if (!this.tunneler.getPlannedBlocks().contains(this.targetDigPos)) {
			this.clearCurrentDigTarget();
			return;
		}

		IBlockState state = this.world.getBlockState(this.targetDigPos);

		// Another entity or event may have removed the block while we were digging.
		// Do not place a ladder in that case: ladders are only created for shaft
		// blocks this tunneler itself successfully destroys.
		if (this.world.isAirBlock(this.targetDigPos) || state.getBlock() == Blocks.LADDER) {
			this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, -1);

			this.consumePlannedBlock(this.targetDigPos);
			this.targetDigPos = null;
			this.digTimer = 0;
			this.failedBreakPos = null;
			this.failedBreakAttempts = 0;
			return;
		}

		if (!this.canMine(this.targetDigPos)) {
			this.abortCurrentPlan();
			return;
		}

		double targetDistSq = this.tunneler.getDistanceSqToCenter(this.targetDigPos);
		double targetDy = this.targetDigPos.getY() - this.tunneler.posY;

		// The goblin may have been pushed, fallen, or climbed while digging.
		// Drop the current break attempt and reposition instead of mining forever
		// from an invalid location.
		if (targetDistSq > DIG_CANCEL_RANGE_SQ || targetDy < -1.85D) {
			this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, -1);
			this.targetDigPos = null;
			this.digTimer = 0;
			this.tunneler.getNavigator().clearPath();
			this.resetMovementTracking();
			return;
		}

		// Hold position while mining inside one of our ladder shafts. A positive
		// Y velocity here used to make the goblin slowly climb while mining until
		// the target became invalid, creating a dig/reposition loop.
		if (this.isVerticalShaftPosition(this.targetDigPos)) {
			BlockPos shaftColumn = this.getShaftColumn(this.targetDigPos);
			if (shaftColumn != null && this.isEntityInsideShaftColumn(shaftColumn)) {
				this.tunneler.getNavigator().clearPath();
				this.tunneler.fallDistance = 0.0F;
				this.tunneler.moveForward = 0.0F;
				this.tunneler.moveStrafing = 0.0F;
				this.tunneler.moveVertical = 0.0F;
				this.tunneler.motionX = 0.0D;
				this.tunneler.motionY = 0.0D;
				this.tunneler.motionZ = 0.0D;
				this.centerEntityInShaft(shaftColumn);
			}
		}

		this.tunneler.getLookHelper().setLookPosition(
				this.targetDigPos.getX() + 0.5D,
				this.targetDigPos.getY() + 0.5D,
				this.targetDigPos.getZ() + 0.5D,
				30.0F,
				30.0F
		);

		if (this.digTimer % 5 == 0) {
			this.tunneler.swingArm(EnumHand.MAIN_HAND);

			SoundType soundType = state.getBlock().getSoundType(
					state,
					this.world,
					this.targetDigPos,
					this.tunneler
			);

			this.world.playSound(
					null,
					this.targetDigPos,
					soundType.getHitSound(),
					SoundCategory.BLOCKS,
					(soundType.getVolume() + 1.0F) / 8.0F,
					soundType.getPitch() * 0.5F
			);
		}

		this.digTimer++;

		// Vanilla break animation stages are 0..9. Sending 10 can leave odd visual
		// behavior on some clients/mod combinations.
		int progress = Math.min(
				9,
				(int) ((float) this.digTimer / (float) this.maxBreakTime * 10.0F)
		);
		this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, progress);

		if (this.digTimer < this.maxBreakTime) {
			return;
		}

		BlockPos brokenPos = this.targetDigPos;
		this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), brokenPos, -1);

		boolean destroyed = this.world.destroyBlock(brokenPos, true);
		boolean nowPassable = this.world.isAirBlock(brokenPos)
				|| this.world.getBlockState(brokenPos).getBlock() == Blocks.LADDER;

		if (!destroyed && !nowPassable) {
			this.recordFailedBreak(brokenPos);

			this.targetDigPos = null;
			this.digTimer = 0;

			if (this.failedBreakAttempts >= 3) {
				this.abortCurrentPlan();
			}

			return;
		}

		this.failedBreakPos = null;
		this.failedBreakAttempts = 0;

		if (destroyed && this.world.isAirBlock(brokenPos) && this.isVerticalShaftPosition(brokenPos)) {
			this.tryAttachLadderToSolidWall(
					brokenPos,
					this.getPreferredShaftApproachFacing(brokenPos)
			);
		}

		this.consumePlannedBlock(brokenPos);

		if (destroyed) {
			this.tunneler.addDugBlock(brokenPos);
			this.tryPlaceScheduledTorch(brokenPos);
		}

		if (this.tunneler.getBlocksDug() >= MAX_BLOCKS_DUG) {
			this.breakTunnelerTool();
		}

		this.targetDigPos = null;
		this.digTimer = 0;
		this.resetMovementTracking();
	}

	private void recordFailedBreak(BlockPos pos) {
		if (pos.equals(this.failedBreakPos)) {
			this.failedBreakAttempts++;
		} else {
			this.failedBreakPos = pos;
			this.failedBreakAttempts = 1;
		}
	}

	private void breakTunnelerTool() {
		ItemStack held = this.tunneler.getHeldItemMainhand();
		if (held.isEmpty()) {
			return;
		}

		this.tunneler.renderBrokenItemStack(held);
		this.tunneler.world.playSound(
				null,
				this.tunneler.posX,
				this.tunneler.posY,
				this.tunneler.posZ,
				SoundEvents.ENTITY_ITEM_BREAK,
				SoundCategory.NEUTRAL,
				0.8F,
				0.8F + this.tunneler.world.rand.nextFloat() * 0.4F
		);
		this.tunneler.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
	}

	private boolean canMine(BlockPos pos) {
		if (this.world.isAirBlock(pos)) {
			return false;
		}

		IBlockState state = this.world.getBlockState(pos);

		if (state.getBlock() == Blocks.LADDER) {
			return false;
		}

		if (state.getBlockHardness(this.world, pos) < 0.0F) {
			return false;
		}

		if (state.getMaterial().isLiquid()) {
			return false;
		}

		String maxLevelStr = Settings.miscSettings.tunnelerToolProgressionMaxMiningLevel;
		if (maxLevelStr != null && !maxLevelStr.trim().isEmpty()) {
			try {
				int maxLevel = Integer.parseInt(maxLevelStr.trim());
				int blockLevel = state.getBlock().getHarvestLevel(state);

				if (blockLevel > maxLevel) {
					return false;
				}
			} catch (NumberFormatException ignored) {
				// Invalid configuration: preserve the old behavior and simply ignore it.
			}
		}

		return true;
	}

	private void moveTowardPlanned(BlockPos planned, boolean plannedIsSolid) {
		if (this.isVerticalShaftPosition(planned)
				&& this.tryControlVerticalShaftMovement(planned)) {
			return;
		}

		BlockPos navigationTarget = plannedIsSolid
				? this.findBestApproachPosition(planned)
				: planned;

		if (navigationTarget == null) {
			this.tunneler.getNavigator().clearPath();
			this.tunneler.getMoveHelper().setMoveTo(
					planned.getX() + 0.5D,
					this.tunneler.posY,
					planned.getZ() + 0.5D,
					0.8D
			);
			return;
		}

		if (this.tunneler.getNavigator().noPath() || this.tunneler.ticksExisted % 10 == 0) {
			boolean pathSet = this.tunneler.getNavigator().tryMoveToXYZ(
					navigationTarget.getX() + 0.5D,
					navigationTarget.getY(),
					navigationTarget.getZ() + 0.5D,
					1.0D
			);

			if (!pathSet) {
				this.tunneler.getMoveHelper().setMoveTo(
						navigationTarget.getX() + 0.5D,
						navigationTarget.getY(),
						navigationTarget.getZ() + 0.5D,
						0.9D
				);
			}
		}
	}

	private boolean tryControlVerticalShaftMovement(BlockPos planned) {
		BlockPos shaftColumn = this.getShaftColumn(planned);
		if (shaftColumn == null) {
			return false;
		}

		double centerX = shaftColumn.getX() + 0.5D;
		double centerZ = shaftColumn.getZ() + 0.5D;
		double dx = centerX - this.tunneler.posX;
		double dz = centerZ - this.tunneler.posZ;
		double horizontalDistSq = dx * dx + dz * dz;

		int feetY = (int) Math.floor(this.tunneler.posY + 0.01D);
		boolean insideShaft = this.isEntityInsideShaftColumn(shaftColumn);

		if (!insideShaft) {
			if (horizontalDistSq > 2.25D
					|| !this.isShaftCellPassable(shaftColumn, feetY)
					|| !this.isShaftCellPassable(shaftColumn, feetY + 1)
					|| !this.hasLadderInShaftNearY(shaftColumn, feetY, 2)) {
				return false;
			}

			this.tunneler.getNavigator().clearPath();
			this.tunneler.fallDistance = 0.0F;
			this.tunneler.moveForward = 0.0F;
			this.tunneler.moveStrafing = 0.0F;
			this.tunneler.moveVertical = 0.0F;
			this.tunneler.motionY = 0.0D;

			this.tunneler.getMoveHelper().setMoveTo(
					centerX,
					this.tunneler.posY,
					centerZ,
					1.0D
			);

			if (horizontalDistSq > 0.0001D) {
				double horizontalDist = Math.sqrt(horizontalDistSq);
				this.tunneler.motionX = clamp(dx / horizontalDist * 0.10D, -0.10D, 0.10D);
				this.tunneler.motionZ = clamp(dz / horizontalDist * 0.10D, -0.10D, 0.10D);
			}

			return true;
		}

		this.tunneler.getNavigator().clearPath();
		this.tunneler.fallDistance = 0.0F;
		this.tunneler.moveForward = 0.0F;
		this.tunneler.moveStrafing = 0.0F;
		this.tunneler.moveVertical = 0.0F;

		if (horizontalDistSq > 0.04D) {
			this.tunneler.motionY = 0.0D;
			this.tunneler.getMoveHelper().setMoveTo(
					centerX,
					this.tunneler.posY,
					centerZ,
					0.8D
			);

			double horizontalDist = Math.sqrt(horizontalDistSq);
			this.tunneler.motionX = clamp(dx / horizontalDist * 0.08D, -0.08D, 0.08D);
			this.tunneler.motionZ = clamp(dz / horizontalDist * 0.08D, -0.08D, 0.08D);
			return true;
		}

		this.centerEntityInShaft(shaftColumn);
		this.tunneler.motionX = 0.0D;
		this.tunneler.motionZ = 0.0D;

		double dy = planned.getY() - this.tunneler.posY;

		if (dy > 0.30D) {
			if (this.hasLadderInShaftNearY(shaftColumn, feetY, 1)) {
				this.ladderGapGraceTicks = 10;
			} else if (this.ladderGapGraceTicks > 0) {
				this.ladderGapGraceTicks--;
			} else {
				this.tunneler.motionY = 0.0D;
				return false;
			}

			this.tunneler.motionY = 0.16D;
			return true;
		}

		if (dy < -0.30D) {
			this.ladderGapGraceTicks = 0;
			this.tunneler.motionY = -0.12D;
			return true;
		}

		this.ladderGapGraceTicks = 0;
		this.tunneler.motionY = 0.0D;
		return true;
	}

	private BlockPos getShaftColumn(BlockPos pos) {
		if (this.isInDescentColumn(pos)) {
			return this.startDescentColumn;
		}

		if (this.isInExitColumn(pos)) {
			return this.goalExitColumn;
		}

		return null;
	}

	private boolean isEntityInsideShaftColumn(BlockPos shaftColumn) {
		return (int) Math.floor(this.tunneler.posX) == shaftColumn.getX()
				&& (int) Math.floor(this.tunneler.posZ) == shaftColumn.getZ();
	}

	private boolean isShaftCellPassable(BlockPos shaftColumn, int y) {
		if (y < 0 || y >= this.world.getActualHeight()) {
			return false;
		}

		BlockPos pos = new BlockPos(shaftColumn.getX(), y, shaftColumn.getZ());
		IBlockState state = this.world.getBlockState(pos);
		return this.world.isAirBlock(pos) || state.getBlock() == Blocks.LADDER;
	}

	private boolean hasLadderInShaftNearY(BlockPos shaftColumn, int y, int radius) {
		for (int offsetY = -radius; offsetY <= radius; offsetY++) {
			int checkY = y + offsetY;
			if (checkY < 0 || checkY >= this.world.getActualHeight()) {
				continue;
			}

			BlockPos checkPos = new BlockPos(shaftColumn.getX(), checkY, shaftColumn.getZ());
			if (this.world.getBlockState(checkPos).getBlock() == Blocks.LADDER) {
				return true;
			}
		}

		return false;
	}

	private void centerEntityInShaft(BlockPos shaftColumn) {
		double centerX = shaftColumn.getX() + 0.5D;
		double centerZ = shaftColumn.getZ() + 0.5D;
		double dx = centerX - this.tunneler.posX;
		double dz = centerZ - this.tunneler.posZ;

		if (dx * dx + dz * dz <= 0.04D) {
			this.tunneler.setPosition(centerX, this.tunneler.posY, centerZ);
		}
	}

	private BlockPos findBestApproachPosition(BlockPos planned) {
		int feetY = this.isHorizontalClearanceBlock(planned)
				? planned.getY() - 1
				: planned.getY();

		BlockPos best = null;
		double bestDistanceSq = Double.MAX_VALUE;

		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			BlockPos candidate = new BlockPos(
					planned.getX() + side.getXOffset(),
					feetY,
					planned.getZ() + side.getZOffset()
			);

			if (!this.canStandAt(candidate)) {
				continue;
			}

			double distanceSq = this.tunneler.getDistanceSqToCenter(candidate);
			if (distanceSq < bestDistanceSq) {
				bestDistanceSq = distanceSq;
				best = candidate;
			}
		}

		return best;
	}

	private boolean canStandAt(BlockPos feet) {
		if (!this.isBodyPassable(feet) || !this.isBodyPassable(feet.up())) {
			return false;
		}

		IBlockState below = this.world.getBlockState(feet.down());
		return below.isTopSolid() || below.getBlock() == Blocks.LADDER;
	}

	private boolean isBodyPassable(BlockPos pos) {
		IBlockState state = this.world.getBlockState(pos);

		if (state.getBlock() == Blocks.LADDER) {
			return true;
		}

		return !state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid();
	}

	private boolean hasReachedPlannedPosition(BlockPos planned) {
		double dx = (planned.getX() + 0.5D) - this.tunneler.posX;
		double dz = (planned.getZ() + 0.5D) - this.tunneler.posZ;
		double horizontalSq = dx * dx + dz * dz;
		double vertical = Math.abs(planned.getY() - this.tunneler.posY);

		return horizontalSq <= WAYPOINT_HORIZONTAL_TOLERANCE * WAYPOINT_HORIZONTAL_TOLERANCE
				&& vertical <= WAYPOINT_VERTICAL_TOLERANCE;
	}

	private void trackMovementAndRecover(BlockPos planned) {
		double currentDistanceSq = this.tunneler.getDistanceSqToCenter(planned);

		// Track actual progress toward the current target, not merely movement.
		// This prevents sideways oscillation or pacing from continually resetting
		// the stuck timer without getting the goblin any closer to its goal.
		if (this.progressTarget == null || !this.progressTarget.equals(planned)) {
			this.progressTarget = planned;
			this.bestProgressDistanceSq = currentDistanceSq;
			this.stuckTimer = 0;
			return;
		}

		if (currentDistanceSq + 0.04D < this.bestProgressDistanceSq) {
			this.bestProgressDistanceSq = currentDistanceSq;
			this.stuckTimer = 0;
			this.recoveryAttempts = 0;
			return;
		}

		this.stuckTimer++;

		if (this.stuckTimer < STUCK_TICKS_BEFORE_RECOVERY) {
			return;
		}

		this.stuckTimer = 0;
		this.recoveryAttempts++;
		this.bestProgressDistanceSq = currentDistanceSq;

		this.recoverTowardPlanned(planned);

		if (this.recoveryAttempts >= MAX_RECOVERY_ATTEMPTS) {
			// Failing safely is preferable to leaving an AI task permanently locked
			// onto an unreachable block.
			this.abortCurrentPlan();
		}
	}

	private void recoverTowardPlanned(BlockPos planned) {
		this.tunneler.getNavigator().clearPath();

		boolean plannedIsSolid = !this.world.isAirBlock(planned)
				&& this.world.getBlockState(planned).getBlock() != Blocks.LADDER;

		if (this.isVerticalShaftPosition(planned)
				&& this.tryControlVerticalShaftMovement(planned)) {
			return;
		}

		BlockPos approach = plannedIsSolid
				? this.findBestApproachPosition(planned)
				: planned;

		if (approach == null) {
			return;
		}

		boolean pathSet = this.tunneler.getNavigator().tryMoveToXYZ(
				approach.getX() + 0.5D,
				approach.getY(),
				approach.getZ() + 0.5D,
				1.2D
		);

		if (!pathSet) {
			this.tunneler.getMoveHelper().setMoveTo(
					approach.getX() + 0.5D,
					approach.getY(),
					approach.getZ() + 0.5D,
					1.1D
			);
		}

		if (this.tunneler.onGround && approach.getY() > this.tunneler.posY + 0.5D) {
			this.tunneler.motionY = 0.42D;
		}
	}

	private void resetMovementTracking() {
		this.progressTarget = null;
		this.bestProgressDistanceSq = Double.MAX_VALUE;
	}

	private void consumePlannedBlock(BlockPos pos) {
		if (this.tunneler.getPlannedBlocks().isEmpty()) {
			return;
		}

		if (pos.equals(this.tunneler.getPlannedBlocks().get(0))) {
			this.tunneler.getPlannedBlocks().remove(0);
		} else {
			this.tunneler.getPlannedBlocks().remove(pos);
		}
	}

	private void clearCurrentDigTarget() {
		if (this.targetDigPos != null) {
			this.world.sendBlockBreakProgress(this.tunneler.getEntityId(), this.targetDigPos, -1);
		}

		this.targetDigPos = null;
		this.digTimer = 0;
		this.maxBreakTime = 0;
	}

	private void abortCurrentPlan() {
		this.clearCurrentDigTarget();
		this.tunneler.getNavigator().clearPath();
		this.tunneler.getPlannedBlocks().clear();

		this.stuckTimer = 0;
		this.recoveryAttempts = 0;
		this.ladderGapGraceTicks = 0;
		this.failedBreakPos = null;
		this.failedBreakAttempts = 0;
		this.playerStationaryTicks = 0;
		this.lastPlayerPos = null;

		if (this.tunneler.getBlocksDug() == 0) {
			this.replanCooldown = REPLAN_COOLDOWN_AFTER_ABORT;
		}
	}

	private boolean isHorizontalClearanceBlock(BlockPos pos) {
		return pos.getY() == this.tunnelFloorY + 1
				&& !this.isVerticalShaftPosition(pos);
	}

	private boolean isVerticalShaftPosition(BlockPos pos) {
		return this.isInDescentColumn(pos) || this.isInExitColumn(pos);
	}

	private boolean isInDescentColumn(BlockPos pos) {
		return this.startDescentColumn != null
				&& pos.getX() == this.startDescentColumn.getX()
				&& pos.getZ() == this.startDescentColumn.getZ();
	}

	private boolean isInExitColumn(BlockPos pos) {
		return this.goalExitColumn != null
				&& pos.getX() == this.goalExitColumn.getX()
				&& pos.getZ() == this.goalExitColumn.getZ()
				&& pos.getY() >= this.tunnelFloorY;
	}

	private EnumFacing getPreferredShaftApproachFacing(BlockPos pos) {
		if (this.isInExitColumn(pos) && this.exitApproachFacing != null) {
			return this.exitApproachFacing;
		}

		return this.getSafeHorizontalFacing();
	}

	private EnumFacing getSafeHorizontalFacing() {
		if (this.tunnelFacing != null && this.tunnelFacing.getAxis() != EnumFacing.Axis.Y) {
			return this.tunnelFacing;
		}

		EnumFacing facing = this.tunneler.getHorizontalFacing();
		return facing.getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : facing;
	}

	private boolean tryAttachLadderToSolidWall(BlockPos pos, EnumFacing approachFacing) {
		IBlockState current = this.world.getBlockState(pos);

		if (current.getBlock() == Blocks.LADDER) {
			this.rememberLadderFacing(pos, current.getValue(BlockLadder.FACING));
			return true;
		}

		if (!this.world.isAirBlock(pos)) {
			return false;
		}

		EnumFacing preferred = this.getRememberedLadderFacing(pos);
		if (preferred == null) {
			preferred = this.findExistingLadderFacingInColumn(pos);
		}

		if (preferred != null && this.canAttachLadderToExistingWall(pos, preferred)) {
			return this.placeLadder(pos, preferred);
		}

		EnumFacing safeApproach = approachFacing;
		if (safeApproach == null || safeApproach.getAxis() == EnumFacing.Axis.Y) {
			safeApproach = this.getSafeHorizontalFacing();
		}

		EnumFacing[] candidates = new EnumFacing[]{
				safeApproach.rotateY(),
				safeApproach.rotateYCCW(),
				safeApproach.getOpposite(),
				safeApproach
		};

		for (EnumFacing facing : candidates) {
			if (this.canAttachLadderToExistingWall(pos, facing)) {
				return this.placeLadder(pos, facing);
			}
		}

		return false;
	}

	private boolean placeLadder(BlockPos pos, EnumFacing facing) {
		this.world.setBlockState(
				pos,
				Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, facing)
		);

		if (this.world.getBlockState(pos).getBlock() == Blocks.LADDER) {
			this.rememberLadderFacing(pos, facing);
			return true;
		}

		return false;
	}

	private boolean canAttachLadderToExistingWall(BlockPos ladderPos, EnumFacing ladderFacing) {
		BlockPos wall = ladderPos.offset(ladderFacing.getOpposite());
		return this.world.getBlockState(wall).isSideSolid(this.world, wall, ladderFacing);
	}

	private EnumFacing findExistingLadderFacingInColumn(BlockPos pos) {
		int maxDistance = 8;
		int maxY = this.world.getActualHeight();

		for (int distance = 1; distance <= maxDistance; distance++) {
			int downY = pos.getY() - distance;
			if (downY >= 0) {
				IBlockState downState = this.world.getBlockState(
						new BlockPos(pos.getX(), downY, pos.getZ())
				);
				if (downState.getBlock() == Blocks.LADDER) {
					return downState.getValue(BlockLadder.FACING);
				}
			}

			int upY = pos.getY() + distance;
			if (upY < maxY) {
				IBlockState upState = this.world.getBlockState(
						new BlockPos(pos.getX(), upY, pos.getZ())
				);
				if (upState.getBlock() == Blocks.LADDER) {
					return upState.getValue(BlockLadder.FACING);
				}
			}
		}

		return null;
	}

	private EnumFacing getRememberedLadderFacing(BlockPos pos) {
		if (this.isInDescentColumn(pos) && this.descentLadderFacing != null) {
			return this.descentLadderFacing;
		}

		if (this.isInExitColumn(pos) && this.exitLadderFacing != null) {
			return this.exitLadderFacing;
		}

		return null;
	}

	private void rememberLadderFacing(BlockPos pos, EnumFacing facing) {
		if (this.isInDescentColumn(pos)) {
			this.descentLadderFacing = facing;
		}

		if (this.isInExitColumn(pos)) {
			this.exitLadderFacing = facing;
		}
	}

	private void tryPlaceScheduledTorch(BlockPos center) {
		if (this.tunneler.getTorchesInStash() <= 0) {
			return;
		}

		int dug = this.tunneler.getBlocksDug();
		int torches = this.tunneler.getTorchesInStash();

		boolean shouldPlace = (dug >= 17 && torches == 2)
				|| (dug >= 34 && torches >= 1);

		if (shouldPlace) {
			this.tryPlaceTorchNear(center);
		}
	}

	private void tryPlaceTorchNear(BlockPos center) {
		EnumFacing safeDigFacing = this.digFacing != null
				? this.digFacing
				: this.getSafeHorizontalFacing();

		BlockPos tunnelerPos = new BlockPos(this.tunneler);

		BlockPos[] candidates = new BlockPos[]{
				center,
				center.up(),
				tunnelerPos,
				tunnelerPos.up(),
				center.offset(safeDigFacing.getOpposite()),
				center.offset(safeDigFacing.getOpposite()).up()
		};

		for (BlockPos pos : candidates) {
			// Never spend a torch in a future dig cell or a ladder shaft.
			if (this.tunneler.getPlannedBlocks().contains(pos) || this.isVerticalShaftPosition(pos)) {
				continue;
			}

			if (!this.world.isAirBlock(pos)) {
				continue;
			}

			if (this.world.getBlockState(pos.down()).isTopSolid()) {
				this.world.setBlockState(
						pos,
						Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP)
				);
				this.tunneler.setTorchesInStash(this.tunneler.getTorchesInStash() - 1);
				return;
			}

			for (EnumFacing side : EnumFacing.HORIZONTALS) {
				BlockPos wall = pos.offset(side.getOpposite());

				if (this.world.getBlockState(wall).isSideSolid(this.world, wall, side)) {
					this.world.setBlockState(
							pos,
							Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, side)
					);
					this.tunneler.setTorchesInStash(this.tunneler.getTorchesInStash() - 1);
					return;
				}
			}
		}
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private static Map<String, Integer> breakOverrideCache = null;

	public static void clearBreakOverridesCache() {
		breakOverrideCache = null;
	}

	private static Integer getBlockBreakOverride(IBlockState state) {
		if (breakOverrideCache == null) {
			breakOverrideCache = new HashMap<>();

			if (Settings.miscSettings.tunnelerBlockBreakTimeOverrides != null) {
				for (String entry : Settings.miscSettings.tunnelerBlockBreakTimeOverrides) {
					if (entry == null || entry.trim().isEmpty()) {
						continue;
					}

					String[] parts = entry.trim().split(":");
					if (parts.length != 3 && parts.length != 4) {
						TracesOfTheFallen.LOGGER.warn("Invalid tunneler block break override: " + entry);
						continue;
					}

					try {
						boolean hasMeta = parts.length == 4;

						String namespace = parts[0].trim();
						String path = parts[1].trim();

						String key = hasMeta
								? namespace + ":" + path + ":" + parts[2].trim()
								: namespace + ":" + path;

						int ticks = Integer.parseInt(
								hasMeta ? parts[3].trim() : parts[2].trim()
						);

						breakOverrideCache.put(key, ticks);
					} catch (NumberFormatException e) {
						TracesOfTheFallen.LOGGER.warn("Invalid tunneler block break override: " + entry);
					}
				}
			}
		}

		Block block = state.getBlock();
		ResourceLocation registryName = block.getRegistryName();

		if (registryName == null) {
			return null;
		}

		int meta = block.getMetaFromState(state);
		String blockId = registryName.toString();

		Integer ticks = breakOverrideCache.get(blockId + ":" + meta);
		if (ticks == null) {
			ticks = breakOverrideCache.get(blockId);
		}

		return ticks;
	}
}
