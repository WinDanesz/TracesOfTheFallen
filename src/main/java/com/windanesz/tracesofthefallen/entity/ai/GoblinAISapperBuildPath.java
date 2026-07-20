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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a walkable scaffold path toward a target.
 *
 * Important planning rules:
 * - Every planned walking node is horizontally adjacent to the previous node.
 * - Walking height changes by at most one block per horizontal step.
 * - If the target is too high for a direct ramp, the sapper plans a detour/run-up
 *   to gain enough horizontal distance for a proper staircase.
 * - Only the actual walking surface is built. The old extra block below every
 *   scaffold block was removed because it doubled the work and frequently made
 *   placement/navigation worse without improving pathability.
 * - Each placement remembers the position the sapper should stand at while placing
 *   it, instead of navigating back toward the last block it happened to place.
 */
public class GoblinAISapperBuildPath extends EntityAIBase {

	private static final int MAX_BLOCKS_BUILT = 128;
	private static final int MAX_ROUTE_STEPS = 112;
	private static final int MAX_DETOUR_DISTANCE = 24;
	private static final int REPLAN_STUCK_TICKS = 60;
	private static final int TARGET_REPLAN_INTERVAL = 20;
	private static final double TARGET_REPLAN_DISTANCE_SQ = 16.0D;

	private final EntityGoblinSapper sapper;
	private final World world;

	/**
	 * Parallel to sapper.getPlannedBlocks().
	 * Entry N is where the sapper should stand while placing planned block N.
	 */
	private final List<BlockPos> plannedStandPositions = new ArrayList<>();

	private PathNavigateFlying flyingNavigator;
	private EntityPlayer targetPlayer;
	private BlockPos slopeStartPos;
	private BlockPos lastPlacedPos;
	private BlockPos plannedTargetPos;

	private int buildTimer;
	private int stuckTicks;

	public GoblinAISapperBuildPath(EntityGoblinSapper sapper) {
		this.sapper = sapper;
		this.world = sapper.world;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.sapper.getRevengeTarget() != null) {
			return false;
		}

		if (this.sapper.getBlocksBuilt() >= MAX_BLOCKS_BUILT) {
			return false;
		}

		this.targetPlayer = this.findTargetPlayer();
		if (this.targetPlayer == null) {
			return false;
		}

		// Continue an existing plan. The local stand-position list normally survives
		// with this AI instance. If it somehow does not, updateTask() has a fallback.
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
			if (closest != null
					&& !closest.isDead
					&& !closest.isSpectator()
					&& !closest.isCreative()
					&& !this.sapper.isOwner(closest)) {
				return closest;
			}
		}

		return null;
	}

	private boolean hasDirectGroundPath(EntityPlayer target) {
		Path groundPath = this.sapper.getNavigator().getPathToEntityLiving(target);
		if (groundPath != null && groundPath.getFinalPathPoint() != null) {
			PathPoint endPoint = groundPath.getFinalPathPoint();
			double distSq = target.getDistanceSq(
					endPoint.x + 0.5D,
					endPoint.y,
					endPoint.z + 0.5D
			);
			return distSq <= 4.0D;
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
			double endDistSq = target.getDistanceSq(
					endPoint.x + 0.5D,
					endPoint.y,
					endPoint.z + 0.5D
			);
			return endDistSq < currentDistSq - 16.0D;
		}

		return false;
	}

	private Path getAirPath(EntityPlayer target) {
		if (this.flyingNavigator == null) {
			this.flyingNavigator = new PathNavigateFlying(this.sapper, this.world);
		}

		Path airPath = this.flyingNavigator.getPathToEntityLiving(target);
		if (airPath != null
				&& airPath.getFinalPathPoint() != null
				&& airPath.getCurrentPathLength() > 1) {
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
			RayTraceResult result = this.world.rayTraceBlocks(
					new Vec3d(
							this.sapper.posX,
							this.sapper.posY + this.sapper.getEyeHeight(),
							this.sapper.posZ
					),
					new Vec3d(
							target.posX,
							target.posY + target.getEyeHeight(),
							target.posZ
					),
					false,
					true,
					false
			);

			return result == null || result.typeOfHit == RayTraceResult.Type.MISS;
		}

		return false;
	}

	/**
	 * Generates several staircase candidates and picks the cheapest usable one.
	 *
	 * Unlike the old implementation, the flying path is not copied literally.
	 * Flying paths are allowed to make vertical transitions that a walking goblin
	 * cannot traverse. We only use flying navigation as a reachability hint, then
	 * construct a ground-valid staircase ourselves.
	 */
	private void generateBuildPlan(EntityPlayer target) {
		this.sapper.getPlannedBlocks().clear();
		this.plannedStandPositions.clear();

		BlockPos startWalkPos = new BlockPos(
				Math.floor(this.sapper.posX),
				Math.floor(this.sapper.getEntityBoundingBox().minY + 0.001D),
				Math.floor(this.sapper.posZ)
		);

		BlockPos goalWalkPos = new BlockPos(
				Math.floor(target.posX),
				Math.floor(target.getEntityBoundingBox().minY + 0.001D),
				Math.floor(target.posZ)
		);

		this.slopeStartPos = startWalkPos;
		this.plannedTargetPos = goalWalkPos;

		int verticalDifference = Math.abs(goalWalkPos.getY() - startWalkPos.getY());
		List<List<BlockPos>> horizontalCandidates = createHorizontalCandidates(
				startWalkPos,
				goalWalkPos,
				verticalDifference
		);

		List<BlockPos> bestRoute = null;
		double bestScore = Double.MAX_VALUE;

		for (List<BlockPos> horizontalRoute : horizontalCandidates) {
			List<BlockPos> slopedRoute = applySlopeHeights(
					horizontalRoute,
					startWalkPos.getY(),
					goalWalkPos.getY()
			);

			if (slopedRoute == null || !isContinuousStairRoute(slopedRoute)) {
				continue;
			}

			double score = scoreRoute(slopedRoute);
			if (score < bestScore) {
				bestScore = score;
				bestRoute = slopedRoute;
			}
		}

		if (bestRoute == null) {
			return;
		}

		int remainingBudget = MAX_BLOCKS_BUILT - this.sapper.getBlocksBuilt();
		Set<BlockPos> alreadyPlanned = new HashSet<>();

		for (int i = 1; i < bestRoute.size(); i++) {
			if (this.sapper.getPlannedBlocks().size() >= remainingBudget) {
				break;
			}

			BlockPos walkPos = bestRoute.get(i);
			BlockPos previousWalkPos = bestRoute.get(i - 1);
			BlockPos floorPos = walkPos.down();

			if (needsDirtBlock(floorPos) && alreadyPlanned.add(floorPos)) {
				this.sapper.getPlannedBlocks().add(floorPos);
				this.plannedStandPositions.add(previousWalkPos);
			}
		}
	}

	/**
	 * Creates direct and detoured horizontal routes.
	 *
	 * A detour is useful when the player is high above the sapper: the goblin needs
	 * additional horizontal run length so every upward transition can be exactly one
	 * block high. This prevents the old two-block vertical gaps.
	 */
	private List<List<BlockPos>> createHorizontalCandidates(BlockPos start, BlockPos goal, int requiredSteps) {
		List<List<BlockPos>> candidates = new ArrayList<>();

		BlockPos flatStart = new BlockPos(start.getX(), 0, start.getZ());
		BlockPos flatGoal = new BlockPos(goal.getX(), 0, goal.getZ());

		List<BlockPos> direct = new ArrayList<>();
		direct.add(flatStart);
		appendBalancedXZ(direct, flatGoal);

		if (direct.size() - 1 >= requiredSteps && direct.size() - 1 <= MAX_ROUTE_STEPS) {
			candidates.add(direct);
		}

		EnumFacing[] directions = {
				EnumFacing.NORTH,
				EnumFacing.SOUTH,
				EnumFacing.WEST,
				EnumFacing.EAST
		};

		for (EnumFacing awayDirection : directions) {
			EnumFacing[] sideDirections = getPerpendicularDirections(awayDirection);

			for (EnumFacing sideDirection : sideDirections) {
				int addedForThisDirection = 0;

				for (int detour = 1; detour <= MAX_DETOUR_DISTANCE; detour++) {
					List<BlockPos> route = buildDetourRoute(
							flatStart,
							flatGoal,
							awayDirection,
							sideDirection,
							detour
					);

					int steps = route.size() - 1;
					if (steps < requiredSteps || steps > MAX_ROUTE_STEPS) {
						continue;
					}

					if (hasInvalidRepeatedHorizontalNodes(route, flatStart, flatGoal)) {
						continue;
					}

					candidates.add(route);
					addedForThisDirection++;

					// Keep the candidate set small, but retain a slightly longer
					// alternative because it may avoid terrain better.
					if (addedForThisDirection >= 2) {
						break;
					}
				}
			}
		}

		return candidates;
	}

	private List<BlockPos> buildDetourRoute(BlockPos start,
											BlockPos goal,
											EnumFacing awayDirection,
											EnumFacing sideDirection,
											int detourDistance) {
		List<BlockPos> route = new ArrayList<>();
		route.add(start);

		BlockPos firstCorner = start.offset(awayDirection, detourDistance);
		BlockPos secondCorner = firstCorner.offset(sideDirection);
		BlockPos goalLane = goal.offset(sideDirection);

		appendBalancedXZ(route, firstCorner);
		appendBalancedXZ(route, secondCorner);
		appendBalancedXZ(route, goalLane);
		appendBalancedXZ(route, goal);

		return route;
	}

	private EnumFacing[] getPerpendicularDirections(EnumFacing direction) {
		if (direction.getAxis() == EnumFacing.Axis.X) {
			return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH};
		}
		return new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST};
	}

	/**
	 * Adds a Manhattan path while alternating the dominant remaining axis.
	 * This produces a more diagonal-looking staircase than moving all X first,
	 * then all Z.
	 */
	private void appendBalancedXZ(List<BlockPos> route, BlockPos goal) {
		if (route.isEmpty()) {
			route.add(goal);
			return;
		}

		BlockPos current = route.get(route.size() - 1);
		int x = current.getX();
		int z = current.getZ();

		while (x != goal.getX() || z != goal.getZ()) {
			int dx = Math.abs(goal.getX() - x);
			int dz = Math.abs(goal.getZ() - z);

			if (dx >= dz && x != goal.getX()) {
				x += Integer.signum(goal.getX() - x);
			} else if (z != goal.getZ()) {
				z += Integer.signum(goal.getZ() - z);
			} else {
				x += Integer.signum(goal.getX() - x);
			}

			route.add(new BlockPos(x, 0, z));
		}
	}

	/**
	 * Rejects self-crossing horizontal routes. The only allowed repeated X/Z node
	 * is the final goal when the goal is directly above/below the start.
	 */
	private boolean hasInvalidRepeatedHorizontalNodes(List<BlockPos> route, BlockPos start, BlockPos goal) {
		Set<BlockPos> visited = new HashSet<>();

		for (int i = 0; i < route.size(); i++) {
			BlockPos pos = route.get(i);
			boolean isLast = i == route.size() - 1;
			boolean allowedReturnToStart = isLast
					&& start.getX() == goal.getX()
					&& start.getZ() == goal.getZ()
					&& pos.getX() == start.getX()
					&& pos.getZ() == start.getZ();

			if (!visited.add(pos) && !allowedReturnToStart) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Distributes the full vertical difference over the horizontal route.
	 * Because candidate routes are required to have at least abs(dY) steps,
	 * rounding can never produce a height change larger than one block per step.
	 */
	private List<BlockPos> applySlopeHeights(List<BlockPos> horizontalRoute, int startY, int goalY) {
		int steps = horizontalRoute.size() - 1;
		int deltaY = goalY - startY;

		if (steps <= 0) {
			return null;
		}

		if (Math.abs(deltaY) > steps) {
			return null;
		}

		List<BlockPos> result = new ArrayList<>(horizontalRoute.size());

		for (int i = 0; i <= steps; i++) {
			BlockPos horizontalPos = horizontalRoute.get(i);
			int y = startY + Math.round((float) deltaY * (float) i / (float) steps);
			result.add(new BlockPos(horizontalPos.getX(), y, horizontalPos.getZ()));
		}

		return result;
	}

	private boolean isContinuousStairRoute(List<BlockPos> route) {
		for (int i = 1; i < route.size(); i++) {
			BlockPos previous = route.get(i - 1);
			BlockPos current = route.get(i);

			int horizontalDistance = Math.abs(current.getX() - previous.getX())
					+ Math.abs(current.getZ() - previous.getZ());
			int verticalDistance = Math.abs(current.getY() - previous.getY());

			if (horizontalDistance != 1 || verticalDistance > 1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Scores routes so the sapper prefers:
	 * - fewer dirt placements,
	 * - shorter ramps,
	 * - less digging through occupied headroom,
	 * - routes closer to existing terrain.
	 */
	private double scoreRoute(List<BlockPos> route) {
		double score = 0.0D;
		int neededBlocks = 0;

		for (int i = 1; i < route.size(); i++) {
			BlockPos walkPos = route.get(i);
			BlockPos floorPos = walkPos.down();

			if (needsDirtBlock(floorPos)) {
				neededBlocks++;
				score += 4.0D;
				score += getUnsupportedDepthPenalty(floorPos);

				// If we place the floor ourselves, placeDirtBlock() can clear the
				// two-block walking space. Still penalize solid obstructions.
				if (isHardHeadroom(walkPos)) {
					score += 10.0D;
				}
				if (isHardHeadroom(walkPos.up())) {
					score += 10.0D;
				}
			} else {
				// Existing floor is useful, but if the walking space above it is
				// blocked we cannot rely on floor placement to clear that space.
				if (isHardHeadroom(walkPos) || isHardHeadroom(walkPos.up())) {
					score += 80.0D;
				}
			}

			IBlockState floorState = this.world.getBlockState(floorPos);
			if (floorState.getMaterial().isLiquid()) {
				score += 3.0D;
			}
		}

		int remainingBudget = MAX_BLOCKS_BUILT - this.sapper.getBlocksBuilt();
		if (neededBlocks > remainingBudget) {
			score += 10000.0D + (neededBlocks - remainingBudget) * 100.0D;
		}

		score += route.size() * 0.15D;
		return score;
	}

	private double getUnsupportedDepthPenalty(BlockPos floorPos) {
		double penalty = 0.0D;
		BlockPos cursor = floorPos.down();

		for (int depth = 0; depth < 6; depth++) {
			if (!needsDirtBlock(cursor)) {
				break;
			}
			penalty += 0.35D;
			cursor = cursor.down();
		}

		return penalty;
	}

	private boolean isHardHeadroom(BlockPos pos) {
		if (this.world.isAirBlock(pos)) {
			return false;
		}

		IBlockState state = this.world.getBlockState(pos);
		return !state.getMaterial().isReplaceable();
	}

	private boolean needsDirtBlock(BlockPos pos) {
		if (this.world.isAirBlock(pos)) {
			return true;
		}

		IBlockState state = this.world.getBlockState(pos);
		return state.getMaterial().isLiquid() || state.getMaterial().isReplaceable();
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.sapper.getRevengeTarget() != null) {
			clearPlan();
			return false;
		}

		return !this.sapper.getPlannedBlocks().isEmpty()
				&& this.sapper.getBlocksBuilt() < MAX_BLOCKS_BUILT
				&& this.targetPlayer != null
				&& !this.targetPlayer.isDead
				&& !this.targetPlayer.isSpectator();
	}

	@Override
	public void startExecuting() {
		this.buildTimer = 0;
		this.stuckTicks = 0;

		if (this.slopeStartPos == null) {
			this.slopeStartPos = new BlockPos(
					Math.floor(this.sapper.posX),
					Math.floor(this.sapper.getEntityBoundingBox().minY + 0.001D),
					Math.floor(this.sapper.posZ)
			);
		}

		this.lastPlacedPos = this.slopeStartPos.down();
		this.sapper.stepHeight = 1.0F;
		this.sapper.getNavigator().clearPath();
	}

	@Override
	public void resetTask() {
		this.sapper.stepHeight = 1.0F;
		this.sapper.getNavigator().clearPath();
	}

	@Override
	public void updateTask() {
		if (this.sapper.getPlannedBlocks().isEmpty()) {
			return;
		}

		this.sapper.stepHeight = 1.0F;

		if (shouldReplanForMovingTarget()) {
			generateBuildPlan(this.targetPlayer);
			this.buildTimer = 0;
			this.stuckTicks = 0;
			this.sapper.getNavigator().clearPath();
			return;
		}

		if (this.sapper.getPlannedBlocks().isEmpty()) {
			return;
		}

		BlockPos targetPos = this.sapper.getPlannedBlocks().get(0);
		BlockPos standPos = getCurrentStandPosition(targetPos);

		if (!needsDirtBlock(targetPos)) {
			removeCurrentPlanEntry();
			this.buildTimer = 0;
			return;
		}

		double distSq = this.sapper.getDistanceSqToCenter(targetPos);

		if (distSq > 9.0D) {
			this.stuckTicks++;
			this.buildTimer = 0;

			this.sapper.getLookHelper().setLookPosition(
					targetPos.getX() + 0.5D,
					targetPos.getY() + 0.5D,
					targetPos.getZ() + 0.5D,
					30.0F,
					30.0F
			);

			if (this.sapper.getNavigator().noPath() || this.sapper.ticksExisted % 10 == 0) {
				this.sapper.getNavigator().tryMoveToXYZ(
						standPos.getX() + 0.5D,
						standPos.getY(),
						standPos.getZ() + 0.5D,
						0.8D
				);
			}

			// Vanilla pathing can occasionally fail on a newly-created one-block
			// staircase until the next navigator refresh. MoveHelper is a fallback,
			// but it still aims at the correct standing node, not the last block.
			if (this.sapper.getNavigator().noPath()) {
				this.sapper.getMoveHelper().setMoveTo(
						standPos.getX() + 0.5D,
						standPos.getY(),
						standPos.getZ() + 0.5D,
						0.8D
				);
			}

			if (this.sapper.collidedHorizontally) {
				this.sapper.getJumpHelper().setJumping();
			}

			if (this.stuckTicks >= REPLAN_STUCK_TICKS) {
				generateBuildPlan(this.targetPlayer);
				this.stuckTicks = 0;
				this.buildTimer = 0;
				this.sapper.getNavigator().clearPath();
			}

			return;
		}

		this.stuckTicks = 0;

		if (!this.sapper.getNavigator().noPath()) {
			this.sapper.getNavigator().clearPath();
		}

		this.sapper.motionX *= 0.1D;
		this.sapper.motionZ *= 0.1D;

		this.sapper.getLookHelper().setLookPosition(
				targetPos.getX() + 0.5D,
				targetPos.getY() + 0.5D,
				targetPos.getZ() + 0.5D,
				30.0F,
				30.0F
		);

		this.buildTimer++;

		if (this.buildTimer % 5 == 0) {
			this.sapper.swingArm(EnumHand.OFF_HAND);
		}

		if (this.buildTimer < 20) {
			return;
		}

		AxisAlignedBB bb = new AxisAlignedBB(targetPos);

		if (this.world.checkNoEntityCollision(bb)) {
			placeDirtBlock(targetPos);
			removeCurrentPlanEntry();
			this.buildTimer = 0;
		} else if (this.sapper.getEntityBoundingBox().intersects(bb)) {
			this.sapper.getJumpHelper().setJumping();
		} else if (this.buildTimer >= 60) {
			// A different entity may be temporarily occupying the placement cell.
			// Replan rather than blindly discarding a structurally important stair.
			generateBuildPlan(this.targetPlayer);
			this.buildTimer = 0;
			this.stuckTicks = 0;
		}
	}

	private boolean shouldReplanForMovingTarget() {
		if (this.targetPlayer == null
				|| this.plannedTargetPos == null
				|| this.sapper.ticksExisted % TARGET_REPLAN_INTERVAL != 0) {
			return false;
		}

		BlockPos currentTargetPos = new BlockPos(
				Math.floor(this.targetPlayer.posX),
				Math.floor(this.targetPlayer.getEntityBoundingBox().minY + 0.001D),
				Math.floor(this.targetPlayer.posZ)
		);

		double dx = currentTargetPos.getX() - this.plannedTargetPos.getX();
		double dy = currentTargetPos.getY() - this.plannedTargetPos.getY();
		double dz = currentTargetPos.getZ() - this.plannedTargetPos.getZ();

		return dx * dx + dy * dy + dz * dz >= TARGET_REPLAN_DISTANCE_SQ;
	}

	private BlockPos getCurrentStandPosition(BlockPos targetPos) {
		if (!this.plannedStandPositions.isEmpty()) {
			return this.plannedStandPositions.get(0);
		}

		if (this.lastPlacedPos != null) {
			return this.lastPlacedPos.up();
		}

		if (this.slopeStartPos != null) {
			return this.slopeStartPos;
		}

		// Fallback for an externally restored planned-block list.
		int dx = Integer.signum(targetPos.getX() - (int) Math.floor(this.sapper.posX));
		int dz = Integer.signum(targetPos.getZ() - (int) Math.floor(this.sapper.posZ));
		return targetPos.up().add(-dx, 0, -dz);
	}

	private void removeCurrentPlanEntry() {
		if (!this.sapper.getPlannedBlocks().isEmpty()) {
			this.sapper.getPlannedBlocks().remove(0);
		}

		if (!this.plannedStandPositions.isEmpty()) {
			this.plannedStandPositions.remove(0);
		}
	}

	private void clearPlan() {
		this.sapper.getPlannedBlocks().clear();
		this.plannedStandPositions.clear();
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

		this.world.playSound(
				null,
				pos,
				SoundEvents.BLOCK_GRAVEL_PLACE,
				SoundCategory.BLOCKS,
				1.0F,
				0.8F
		);
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
