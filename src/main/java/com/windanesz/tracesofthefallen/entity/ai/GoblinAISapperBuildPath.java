package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblinSapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class GoblinAISapperBuildPath extends EntityAIBase {
	private final EntityGoblinSapper sapper;
	private final World world;
	private PathNavigateFlying flyingNavigator;
	private EntityPlayer targetPlayer;
	private BlockPos slopeStartPos;
	private BlockPos lastPlacedPos;
	private int buildTimer;
	private int stuckTicks;

	public GoblinAISapperBuildPath(EntityGoblinSapper sapper) {
		this.sapper = sapper;
		this.world = sapper.world;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.sapper.getBlocksBuilt() >= 32) {
			return false;
		}

		this.targetPlayer = this.findTargetPlayer();
		if (this.targetPlayer == null) {
			return false;
		}

		if (!this.sapper.getPlannedBlocks().isEmpty()) {
			return true;
		}

		if (hasDirectGroundPath(this.targetPlayer)) {
			return false;
		}

		if (canWalkCloserOnGround(this.targetPlayer)) {
			return false;
		}

		if (!hasPathFromAir(this.targetPlayer)) {
			return false;
		}

		generateBuildPlan(this.targetPlayer);
		return !this.sapper.getPlannedBlocks().isEmpty();
	}

	private EntityPlayer findTargetPlayer() {
		if (this.sapper.getAttackTarget() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) this.sapper.getAttackTarget();
			if (!player.isDead && !player.isSpectator() && !player.isCreative()) {
				return player;
			}
		}
		if (this.sapper.hasOwner() && this.sapper.getOwner() instanceof EntityPlayer) {
			EntityPlayer owner = (EntityPlayer) this.sapper.getOwner();
			if (!owner.isDead && !owner.isSpectator()) {
				return owner;
			}
		}
		if (!this.sapper.hasOwner() && !this.sapper.isNeutral()) {
			EntityPlayer closest = this.world.getClosestPlayerToEntity(this.sapper, 64.0D);
			if (closest != null && !closest.isDead && !closest.isSpectator() && !closest.isCreative() && !this.sapper.isOwner(closest)) {
				return closest;
			}
		}
		return null;
	}

	private boolean hasDirectGroundPath(EntityPlayer target) {
		Path groundPath = this.sapper.getNavigator().getPathToEntityLiving(target);
		if (groundPath != null && groundPath.getFinalPathPoint() != null) {
			PathPoint endPoint = groundPath.getFinalPathPoint();
			double distSq = target.getDistanceSq(endPoint.x + 0.5D, endPoint.y, endPoint.z + 0.5D);
			if (distSq <= 4.0D) {
				return true;
			}
		}
		return false;
	}

	private boolean canWalkCloserOnGround(EntityPlayer target) {
		double currentDistSq = this.sapper.getDistanceSq(target);
		if (currentDistSq <= 36.0D) {
			return false;
		}
		Path groundPath = this.sapper.getNavigator().getPathToEntityLiving(target);
		if (groundPath != null && groundPath.getFinalPathPoint() != null) {
			PathPoint endPoint = groundPath.getFinalPathPoint();
			double endDistSq = target.getDistanceSq(endPoint.x + 0.5D, endPoint.y, endPoint.z + 0.5D);
			if (endDistSq < currentDistSq - 16.0D) {
				return true;
			}
		}
		return false;
	}

	private Path getAirPath(EntityPlayer target) {
		if (this.flyingNavigator == null) {
			this.flyingNavigator = new PathNavigateFlying(this.sapper, this.world);
		}
		Path airPath = this.flyingNavigator.getPathToEntityLiving(target);
		if (airPath != null && airPath.getFinalPathPoint() != null && airPath.getCurrentPathLength() > 1) {
			return airPath;
		}
		return null;
	}

	private boolean hasPathFromAir(EntityPlayer target) {
		Path airPath = getAirPath(target);
		if (airPath != null) {
			return true;
		}
		if (this.sapper.getDistanceSq(target) <= 1024.0D) {
			RayTraceResult res = this.world.rayTraceBlocks(
					new Vec3d(this.sapper.posX, this.sapper.posY + this.sapper.getEyeHeight(), this.sapper.posZ),
					new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ),
					false, true, false
			);
			return res == null || res.typeOfHit == RayTraceResult.Type.MISS || res.entityHit == target;
		}
		return false;
	}

	private void generateBuildPlan(EntityPlayer target) {
		this.sapper.getPlannedBlocks().clear();
		List<BlockPos> plan = new ArrayList<>();
		int maxBlocks = 32;
		BlockPos curr = new BlockPos(this.sapper.posX, this.sapper.posY, this.sapper.posZ);
		this.slopeStartPos = curr;

		Path airPath = getAirPath(target);
		if (airPath != null) {
			for (int i = 0; i < airPath.getCurrentPathLength(); i++) {
				PathPoint pt = airPath.getPathPointFromIndex(i);
				BlockPos nextPt = new BlockPos(pt.x, pt.y, pt.z);
				curr = addManhattanSegmentToPlan(curr, nextPt, plan, maxBlocks);
				if (plan.size() + this.sapper.getBlocksBuilt() >= maxBlocks) {
					break;
				}
			}
		}

		BlockPos goalPos = new BlockPos(target);
		if (plan.size() + this.sapper.getBlocksBuilt() < maxBlocks) {
			addManhattanSegmentToPlan(curr, goalPos, plan, maxBlocks);
		}

		this.sapper.getPlannedBlocks().addAll(plan);
	}

	private BlockPos addManhattanSegmentToPlan(BlockPos start, BlockPos end, List<BlockPos> plan, int maxBlocks) {
		int currX = start.getX();
		int currY = start.getY();
		int currZ = start.getZ();
		int goalX = end.getX();
		int goalY = end.getY();
		int goalZ = end.getZ();

		int startX = currX;
		int startY = currY;
		int startZ = currZ;

		int manhattanDist = Math.abs(goalX - startX) + Math.abs(goalZ - startZ);
		if (manhattanDist == 0) {
			return start;
		}

		for (int step = 1; step <= manhattanDist; step++) {
			if (plan.size() + this.sapper.getBlocksBuilt() >= maxBlocks) {
				break;
			}
			int dx = Math.abs(goalX - currX);
			int dz = Math.abs(goalZ - currZ);

			if (dx >= dz && currX != goalX) {
				currX += Integer.signum(goalX - currX);
			} else if (currZ != goalZ) {
				currZ += Integer.signum(goalZ - currZ);
			}

			int desiredY = startY + Math.round((float) step * (goalY - startY) / manhattanDist);
			int stepY = desiredY - currY;
			if (stepY > 1) desiredY = currY + 1;
			if (stepY < -1) desiredY = currY - 1;

			currY = desiredY;

			BlockPos floorPos = new BlockPos(currX, currY - 1, currZ);
			if (needsDirtBlock(floorPos) && !plan.contains(floorPos)) {
				plan.add(floorPos);
			}
		}
		return new BlockPos(currX, currY, currZ);
	}

	private boolean needsDirtBlock(BlockPos pos) {
		if (this.world.isAirBlock(pos)) return true;
		IBlockState state = this.world.getBlockState(pos);
		return state.getMaterial().isLiquid() || state.getMaterial().isReplaceable();
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.sapper.getPlannedBlocks().isEmpty()
				&& this.sapper.getBlocksBuilt() < 32
				&& this.targetPlayer != null && !this.targetPlayer.isDead && !this.targetPlayer.isSpectator();
	}

	@Override
	public void startExecuting() {
		this.buildTimer = 0;
		this.stuckTicks = 0;
		if (this.slopeStartPos == null) {
			this.slopeStartPos = new BlockPos(this.sapper.posX, this.sapper.posY, this.sapper.posZ);
		}
		this.lastPlacedPos = this.slopeStartPos;
		this.sapper.stepHeight = 1.05F;
		this.sapper.getNavigator().clearPath();
	}

	@Override
	public void resetTask() {
		this.sapper.stepHeight = 1.05F;
		this.sapper.getNavigator().clearPath();
	}

	@Override
	public void updateTask() {
		if (this.sapper.getPlannedBlocks().isEmpty()) {
			return;
		}

		this.sapper.stepHeight = 1.05F;
		BlockPos targetPos = this.sapper.getPlannedBlocks().get(0);
		if (!needsDirtBlock(targetPos)) {
			this.sapper.getPlannedBlocks().remove(0);
			this.buildTimer = 0;
			return;
		}

		double distSq = this.sapper.getDistanceSqToCenter(targetPos);

		if (distSq > 9.0D) {
			this.stuckTicks++;
			BlockPos navPos = (this.lastPlacedPos != null) ? this.lastPlacedPos : this.slopeStartPos;
			if (navPos == null) {
				navPos = new BlockPos(this.sapper.posX, this.sapper.posY, this.sapper.posZ);
			}

			this.sapper.getLookHelper().setLookPosition(navPos.getX() + 0.5D, navPos.getY() + 1.0D, navPos.getZ() + 0.5D, 30.0F, 30.0F);

			if (this.sapper.getNavigator().noPath() || this.sapper.ticksExisted % 10 == 0) {
				boolean fellBelow = (this.lastPlacedPos != null && this.sapper.posY < this.lastPlacedPos.getY() - 2.0D);
				if (fellBelow && this.slopeStartPos != null) {
					this.sapper.getNavigator().tryMoveToXYZ(navPos.getX() + 0.5D, navPos.getY() + 1.0D, navPos.getZ() + 0.5D, 1.2D);
					if (this.sapper.getNavigator().noPath()) {
						this.sapper.getNavigator().tryMoveToXYZ(this.slopeStartPos.getX() + 0.5D, this.slopeStartPos.getY() + 1.0D, this.slopeStartPos.getZ() + 0.5D, 1.2D);
					}
				} else {
					this.sapper.getNavigator().tryMoveToXYZ(navPos.getX() + 0.5D, navPos.getY() + 1.0D, navPos.getZ() + 0.5D, 1.2D);
				}
			}

			if (this.sapper.getNavigator().noPath() && this.sapper.posY >= navPos.getY() - 1.0D) {
				this.sapper.getMoveHelper().setMoveTo(navPos.getX() + 0.5D, navPos.getY() + 1.0D, navPos.getZ() + 0.5D, 1.2D);
			}

			if (this.sapper.collidedHorizontally && this.sapper.getNavigator().noPath()) {
				this.sapper.getJumpHelper().setJumping();
			}
			return;
		}

		this.stuckTicks = 0;
		if (!this.sapper.getNavigator().noPath()) {
			this.sapper.getNavigator().clearPath();
		}
		this.sapper.motionX *= 0.1D;
		this.sapper.motionZ *= 0.1D;
		this.sapper.getLookHelper().setLookPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 30.0F, 30.0F);

		this.buildTimer++;
		if (this.buildTimer % 2 == 0) {
			this.sapper.swingArm(EnumHand.OFF_HAND);
		}

		if (this.buildTimer >= 2) {
			AxisAlignedBB bb = new AxisAlignedBB(targetPos);
			if (this.world.checkNoEntityCollision(bb)) {
				placeDirtBlock(targetPos);
				this.sapper.getPlannedBlocks().remove(0);
				this.buildTimer = 0;
			} else if (this.sapper.getEntityBoundingBox().intersects(bb)) {
				this.sapper.getJumpHelper().setJumping();
			} else if (this.buildTimer >= 20) {
				this.sapper.getPlannedBlocks().remove(0);
				this.buildTimer = 0;
			}
		}
	}

	private void placeDirtBlock(BlockPos pos) {
		clearHeadroom(pos.up(1));
		clearHeadroom(pos.up(2));

		this.world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
		this.lastPlacedPos = pos;
		this.sapper.setBlocksBuilt(this.sapper.getBlocksBuilt() + 1);
		ItemStack offhand = this.sapper.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
		if (!offhand.isEmpty() && offhand.getItem() == Item.getItemFromBlock(Blocks.DIRT)) {
			offhand.shrink(1);
			if (offhand.isEmpty()) {
				this.sapper.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
			}
		}
		this.world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
	}

	private void clearHeadroom(BlockPos headPos) {
		if (!this.world.isAirBlock(headPos)) {
			IBlockState state = this.world.getBlockState(headPos);
			if (state.getBlock() != Blocks.BEDROCK) {
				this.world.destroyBlock(headPos, true);
			}
		}
	}
}
