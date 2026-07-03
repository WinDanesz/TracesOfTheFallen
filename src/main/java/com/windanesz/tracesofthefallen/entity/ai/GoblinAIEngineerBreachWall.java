package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityGoblinEngineer;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoblinAIEngineerBreachWall extends EntityAIBase {

	private static final int STATE_APPROACH_BROOD = 0;
	private static final int STATE_POINT_AND_IGNITE = 1;
	private static final int STATE_SOLO_APPROACH_WALL = 2;
	private static final int STATE_RETREAT = 3;
	private static final int STATE_HANDOVER = 4;

	// 20 ticks = 1 second
	private static final int HANDOVER_TOTAL_TICKS = 50; // 2.5 seconds
	private static final int POINT_TOTAL_TICKS = 90;   // 12 seconds
	private static final int POINT_IGNITE_TICK = 60;    // ignite after 3 seconds of pointing

	private final EntityGoblinEngineer engineer;
	private final PathNavigate navigator;

	private BlockPos targetWallPos;
	private BlockPos targetPlantPos;

	private EntityGoblin targetBrood;
	private EntityWroughtBomb mountedBomb;
	private EntityWroughtBomb plantedBomb;

	private int state;
	private int timer;
	private int nextScanTick;

	public GoblinAIEngineerBreachWall(EntityGoblinEngineer engineer) {
		this.engineer = engineer;
		this.navigator = engineer.getNavigator();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.engineer.ticksExisted < this.nextScanTick) {
			return false;
		}

		if (this.engineer.getHealth() <= this.engineer.getMaxHealth() / 2.0F) {
			return false;
		}

		this.mountedBomb = this.getMountedBomb();

		if (this.mountedBomb == null) {
			return false;
		}

		EntityPlayer target = this.getValidPlayerTarget();

		if (target == null) {
			return false;
		}

		if (this.isPlayerEasilyReachable(target)) {
			return false;
		}

		BreachTarget breachTarget = this.findBestBreachTarget(target);

		if (breachTarget == null) {
			this.nextScanTick = this.engineer.ticksExisted + 20;
			return false;
		}

		this.targetWallPos = breachTarget.wallPos;
		this.targetPlantPos = breachTarget.plantPos;

		this.targetBrood = this.findNearbyUnarmedBrood(this.targetPlantPos);

		if (this.targetBrood != null) {
			this.state = STATE_APPROACH_BROOD;
		} else {
			this.state = STATE_SOLO_APPROACH_WALL;
		}

		this.timer = 0;
		return true;
	}

	@Override
	public void startExecuting() {
		if (this.state == STATE_APPROACH_BROOD && this.targetBrood != null) {
			this.navigator.tryMoveToEntityLiving(this.targetBrood, 1.3D);
		} else if (this.state == STATE_SOLO_APPROACH_WALL && this.targetPlantPos != null) {
			this.moveToPlantPos();
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.engineer.getHealth() <= this.engineer.getMaxHealth() / 2.0F) {
			return false;
		}

		if (this.state == STATE_RETREAT) {
			return this.plantedBomb != null
					&& !this.plantedBomb.isDead
					&& this.engineer.getDistanceSq(this.plantedBomb) < 144.0D;
		}

		if (this.state == STATE_HANDOVER) {
			return this.timer < HANDOVER_TOTAL_TICKS
					&& this.targetBrood != null
					&& !this.targetBrood.isDead
					&& this.mountedBomb != null
					&& !this.mountedBomb.isDead
					&& this.mountedBomb.getRidingEntity() == this.engineer;
		}

		if (this.state == STATE_POINT_AND_IGNITE) {
			return this.timer < POINT_TOTAL_TICKS
					&& this.plantedBomb != null
					&& !this.plantedBomb.isDead;
		}

		EntityPlayer target = this.getValidPlayerTarget();

		if (target != null && this.engineer.ticksExisted % 10 == 0) {
			if (this.isPlayerEasilyReachable(target)) {
				return false;
			}
		}

		if (this.state == STATE_APPROACH_BROOD) {
			return this.targetBrood != null
					&& !this.targetBrood.isDead
					&& !this.targetBrood.isCarryingBomb()
					&& this.mountedBomb != null
					&& !this.mountedBomb.isDead
					&& this.mountedBomb.getRidingEntity() == this.engineer;
		}

		if (this.state == STATE_SOLO_APPROACH_WALL) {
			return this.targetPlantPos != null
					&& this.mountedBomb != null
					&& !this.mountedBomb.isDead
					&& this.mountedBomb.getRidingEntity() == this.engineer;
		}

		return false;
	}

	@Override
	public void updateTask() {
		this.timer++;

		if (this.state == STATE_APPROACH_BROOD && this.targetBrood != null) {
			this.updateApproachBrood();
		} else if (this.state == STATE_HANDOVER) {
			this.updateHandoverPause();
		} else if (this.state == STATE_POINT_AND_IGNITE) {
			this.updatePointAndIgnite();
		} else if (this.state == STATE_SOLO_APPROACH_WALL && this.targetPlantPos != null) {
			this.updateSoloPlanting();
		} else if (this.state == STATE_RETREAT) {
			this.updateRetreat();
		}
	}

	private void updateApproachBrood() {
		this.engineer.getLookHelper().setLookPositionWithEntity(
				this.targetBrood,
				10.0F,
				(float) this.engineer.getVerticalFaceSpeed()
		);

		if (this.navigator.noPath() || this.engineer.ticksExisted % 10 == 0) {
			this.navigator.tryMoveToEntityLiving(this.targetBrood, 1.3D);
		}

		if (this.engineer.getDistanceSq(this.targetBrood) <= 9.0D) {
			this.beginHandoverPause();
			return;
		}

		// Do not teleport the bomb to a brood that could not be reached.
		// Fall back to solo planting instead.
		if ((this.navigator.noPath() && this.timer > 40) || this.timer > 120) {
			this.targetBrood = null;
			this.state = STATE_SOLO_APPROACH_WALL;
			this.timer = 0;
			this.navigator.clearPath();
			this.moveToPlantPos();
		}
	}

	private void beginHandoverPause() {
		this.state = STATE_HANDOVER;
		this.timer = 0;
		this.navigator.clearPath();

		this.engineer.setPointing(false);

		if (this.targetBrood != null) {
			this.engineer.getLookHelper().setLookPositionWithEntity(
					this.targetBrood,
					20.0F,
					(float) this.engineer.getVerticalFaceSpeed()
			);
		}
	}

	private void updateHandoverPause() {
		if (this.targetBrood == null || this.targetBrood.isDead) {
			this.targetBrood = null;
			this.state = STATE_SOLO_APPROACH_WALL;
			this.timer = 0;
			this.engineer.setPointing(false);
			this.moveToPlantPos();
			return;
		}

		this.navigator.clearPath();

		this.engineer.getLookHelper().setLookPositionWithEntity(
				this.targetBrood,
				20.0F,
				(float) this.engineer.getVerticalFaceSpeed()
		);

		this.targetBrood.getLookHelper().setLookPositionWithEntity(
				this.engineer,
				20.0F,
				(float) this.targetBrood.getVerticalFaceSpeed()
		);

		// Visible "give/order" gesture during the second half of the handover.
		this.engineer.setPointing(this.timer >= HANDOVER_TOTAL_TICKS / 2);

		if (this.timer >= HANDOVER_TOTAL_TICKS) {
			this.engineer.setPointing(false);
			this.handoverBombToBrood();
		}
	}

	private void handoverBombToBrood() {
		if (this.mountedBomb == null || this.engineer.world.isRemote) {
			return;
		}

		this.mountedBomb.dismountRidingEntity();
		this.mountedBomb.setPosition(this.targetBrood.posX, this.targetBrood.posY, this.targetBrood.posZ);

		boolean mounted = this.mountedBomb.startRiding(this.targetBrood, true);

		if (!mounted) {
			this.mountedBomb.setPosition(this.engineer.posX, this.engineer.posY, this.engineer.posZ);
			this.mountedBomb.startRiding(this.engineer, true);

			this.targetBrood = null;
			this.state = STATE_SOLO_APPROACH_WALL;
			this.timer = 0;
			this.navigator.clearPath();
			this.moveToPlantPos();
			return;
		}

		this.plantedBomb = this.mountedBomb;

		// The brood goes to the air block beside the wall, not the solid wall block.
		this.targetBrood.setBombDeliveryTarget(this.targetPlantPos);

		this.engineer.setPointing(true);
		this.state = STATE_POINT_AND_IGNITE;
		this.timer = 0;
		this.navigator.clearPath();
		this.faceDesiredBombLocation();
	}

	private void updatePointAndIgnite() {
		this.navigator.clearPath();
		this.faceDesiredBombLocation();

		this.engineer.setPointing(true);

		if (this.targetBrood != null && this.targetPlantPos != null && this.timer % 10 == 0) {
			this.targetBrood.setBombDeliveryTarget(this.targetPlantPos);
		}

		if (this.timer == POINT_IGNITE_TICK) {
			if (this.plantedBomb != null && !this.engineer.world.isRemote) {
				this.plantedBomb.setFuse(Settings.miscSettings.wroughtBombFuseTime);

				this.engineer.world.playSound(
						null,
						this.plantedBomb.posX,
						this.plantedBomb.posY,
						this.plantedBomb.posZ,
						SoundEvents.ENTITY_TNT_PRIMED,
						SoundCategory.BLOCKS,
						1.0F,
						1.0F
				);
			}

			if (this.targetBrood != null && this.targetPlantPos != null) {
				this.targetBrood.setBombDeliveryTarget(this.targetPlantPos);
			}
		}

		if (this.timer >= POINT_TOTAL_TICKS) {
			this.engineer.setPointing(false);
			this.retreat();
		}
	}

	private void faceDesiredBombLocation() {
		if (this.targetPlantPos != null) {
			double dx = (this.targetPlantPos.getX() + 0.5D) - this.engineer.posX;
			double dz = (this.targetPlantPos.getZ() + 0.5D) - this.engineer.posZ;
			float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
			this.engineer.rotationYaw = yaw;
			this.engineer.rotationYawHead = yaw;
			this.engineer.renderYawOffset = yaw;

			this.engineer.getLookHelper().setLookPosition(
					this.targetPlantPos.getX() + 0.5D,
					this.targetPlantPos.getY() + 0.5D,
					this.targetPlantPos.getZ() + 0.5D,
					30.0F,
					(float) this.engineer.getVerticalFaceSpeed()
			);
		}
	}

	private void updateSoloPlanting() {
		if (this.engineer.ticksExisted % 10 == 0) {
			EntityGoblin brood = this.findNearbyUnarmedBrood(this.targetPlantPos);
			if (brood != null) {
				this.targetBrood = brood;
				this.state = STATE_APPROACH_BROOD;
				return;
			}
		}

		if (this.targetWallPos != null) {
			this.engineer.getLookHelper().setLookPosition(
					this.targetWallPos.getX() + 0.5D,
					this.targetWallPos.getY() + 0.5D,
					this.targetWallPos.getZ() + 0.5D,
					10.0F,
					(float) this.engineer.getVerticalFaceSpeed()
			);
		}

		if (this.navigator.noPath() || this.engineer.ticksExisted % 10 == 0) {
			this.moveToPlantPos();
		}

		double dx = (this.targetPlantPos.getX() + 0.5D) - this.engineer.posX;
		double dz = (this.targetPlantPos.getZ() + 0.5D) - this.engineer.posZ;
		double horizDistSq = dx * dx + dz * dz;

		boolean closeEnough = horizDistSq <= 2.5D && Math.abs(this.engineer.posY - this.targetPlantPos.getY()) <= 1.5D;
		boolean gaveUpNearPathEnd = this.navigator.noPath() && this.timer > 80;

		if (closeEnough || gaveUpNearPathEnd) {
			this.plantMountedBombSolo();
		}
	}

	private void plantMountedBombSolo() {
		if (this.mountedBomb != null && !this.engineer.world.isRemote) {
			this.mountedBomb.dismountRidingEntity();

			Vec3d dirToWall = new Vec3d(
					(this.targetWallPos.getX() + 0.5D) - this.engineer.posX,
					0,
					(this.targetWallPos.getZ() + 0.5D) - this.engineer.posZ
			).normalize();

			this.mountedBomb.setPosition(
					this.targetPlantPos.getX() + 0.5D + dirToWall.x * 0.2D,
					this.targetPlantPos.getY(),
					this.targetPlantPos.getZ() + 0.5D + dirToWall.z * 0.2D
			);

			this.mountedBomb.setFuse(Settings.miscSettings.wroughtBombFuseTime);

			this.engineer.world.playSound(
					null,
					this.mountedBomb.posX,
					this.mountedBomb.posY,
					this.mountedBomb.posZ,
					SoundEvents.ENTITY_TNT_PRIMED,
					SoundCategory.BLOCKS,
					1.0F,
					1.0F
			);
		}

		this.plantedBomb = this.mountedBomb;
		this.retreat();
	}

	private void updateRetreat() {
		if (this.plantedBomb != null && !this.plantedBomb.isDead) {
			if (this.navigator.noPath() || this.engineer.ticksExisted % 10 == 0) {
				Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
						this.engineer,
						16,
						7,
						this.plantedBomb.getPositionVector()
				);

				if (fleePos != null) {
					this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
				}
			}
		}
	}

	private void retreat() {
		this.state = STATE_RETREAT;
		this.timer = 0;

		if (this.plantedBomb != null) {
			Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
					this.engineer,
					16,
					7,
					this.plantedBomb.getPositionVector()
			);

			if (fleePos != null) {
				this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
			}
		}
	}

	@Override
	public void resetTask() {
		this.engineer.setPointing(false);
		this.navigator.clearPath();

		this.targetWallPos = null;
		this.targetPlantPos = null;
		this.targetBrood = null;
		this.plantedBomb = null;
		this.timer = 0;
	}

	private EntityPlayer getValidPlayerTarget() {
		if (this.engineer.getAttackTarget() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) this.engineer.getAttackTarget();

			if (!player.isCreative() && !player.isSpectator()) {
				return player;
			}
		}

		return null;
	}

	private EntityWroughtBomb getMountedBomb() {
		for (Entity passenger : this.engineer.getPassengers()) {
			if (passenger instanceof EntityWroughtBomb) {
				return (EntityWroughtBomb) passenger;
			}
		}

		return null;
	}

	private boolean isPlayerEasilyReachable(EntityPlayer target) {
		Path pathToPlayer = this.navigator.getPathToEntityLiving(target);

		if (pathToPlayer != null && pathToPlayer.getFinalPathPoint() != null) {
			PathPoint endPoint = pathToPlayer.getFinalPathPoint();

			double distSq = target.getDistanceSq(
					endPoint.x + 0.5D,
					endPoint.y,
					endPoint.z + 0.5D
			);

			if (distSq > 4.0D) {
				return false;
			}

			double straightDist = this.engineer.getDistance(target);

			return pathToPlayer.getCurrentPathLength() <= straightDist + 6.0D;
		}

		return false;
	}

	private BreachTarget findBestBreachTarget(EntityPlayer target) {
		List<EntityWroughtBomb> nearbyBombs = this.engineer.world.getEntitiesWithinAABB(
				EntityWroughtBomb.class,
				this.engineer.getEntityBoundingBox().grow(32.0D)
		);

		List<EntityGoblin> nearbyGoblins = this.engineer.world.getEntitiesWithinAABB(
				EntityGoblin.class,
				this.engineer.getEntityBoundingBox().grow(32.0D)
		);

		BreachTarget rayTarget = this.findRaytraceBreachTarget(target, nearbyBombs, nearbyGoblins);
		BreachTarget scanTarget = this.findScannedBreachTarget(target, nearbyBombs, nearbyGoblins);

		if (rayTarget == null) {
			return scanTarget;
		}

		if (scanTarget == null) {
			return rayTarget;
		}

		return rayTarget.score <= scanTarget.score ? rayTarget : scanTarget;
	}

	private BreachTarget findRaytraceBreachTarget(EntityPlayer target, List<EntityWroughtBomb> nearbyBombs, List<EntityGoblin> nearbyGoblins) {
		Vec3d start = new Vec3d(
				this.engineer.posX,
				this.engineer.posY + this.engineer.getEyeHeight(),
				this.engineer.posZ
		);

		Vec3d[] ends = new Vec3d[] {
				new Vec3d(target.posX, target.posY + 0.4D, target.posZ),
				new Vec3d(target.posX, target.posY + 1.0D, target.posZ),
				new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ)
		};

		Set<BlockPos> checked = new HashSet<BlockPos>();
		BreachTarget best = null;

		for (Vec3d end : ends) {
			RayTraceResult result = this.engineer.world.rayTraceBlocks(start, end, false, true, false);

			if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
				continue;
			}

			BlockPos hitPos = result.getBlockPos();

			if (!checked.add(hitPos)) {
				continue;
			}

			BreachTarget candidate = this.evaluateWallColumn(
					hitPos,
					target,
					nearbyBombs,
					nearbyGoblins,
					-250.0D
			);

			if (candidate != null && (best == null || candidate.score < best.score)) {
				best = candidate;
			}
		}

		return best;
	}

	private BreachTarget findScannedBreachTarget(EntityPlayer target, List<EntityWroughtBomb> nearbyBombs, List<EntityGoblin> nearbyGoblins) {
		BreachTarget best = null;

		BlockPos playerPos = new BlockPos(target);

		for (int x = -10; x <= 10; x++) {
			for (int y = -4; y <= 3; y++) {
				for (int z = -10; z <= 10; z++) {
					BlockPos pos = playerPos.add(x, y, z);

					if (pos.distanceSq(this.engineer.posX, this.engineer.posY, this.engineer.posZ) > 900.0D) {
						continue;
					}

					if (!this.isBetweenEngineerAndPlayer(pos, target)) {
						continue;
					}

					BreachTarget candidate = this.evaluateWallColumn(
							pos,
							target,
							nearbyBombs,
							nearbyGoblins,
							0.0D
					);

					if (candidate != null && (best == null || candidate.score < best.score)) {
						best = candidate;
					}
				}
			}
		}

		return best;
	}

	/**
	 * Takes an approximate wall block and searches vertically around it.
	 * This keeps normal breach AI usable when the player stands on top of a wall/tower.
	 */
	private BreachTarget evaluateWallColumn(BlockPos approximateWallPos, EntityPlayer target, List<EntityWroughtBomb> nearbyBombs, List<EntityGoblin> nearbyGoblins, double baseScore) {
		BreachTarget best = null;

		for (int dy = 2; dy >= -6; dy--) {
			BlockPos wallPos = approximateWallPos.add(0, dy, 0);

			if (!this.isValidWallBlock(wallPos)) {
				continue;
			}

			if (this.hasOtherBombNearby(wallPos, nearbyBombs)) {
				continue;
			}

			if (this.isAlreadyTargeted(wallPos, nearbyGoblins)) {
				continue;
			}

			PlantChoice plantChoice = this.findPlantPosForWall(wallPos, target);

			if (plantChoice == null) {
				continue;
			}

			if (this.hasOtherBombNearby(plantChoice.plantPos, nearbyBombs)) {
				continue;
			}

			if (!this.canNavigatorReach(this.navigator, plantChoice.plantPos)) {
				continue;
			}

			double score = baseScore;
			score += this.getLineDistanceScore(wallPos, target) * 40.0D;
			score += target.getDistanceSq(wallPos.getX() + 0.5D, target.posY, wallPos.getZ() + 0.5D) * 2.0D;
			score += Math.abs(target.posY - wallPos.getY()) * 4.0D;
			score += this.engineer.getDistanceSq(
					plantChoice.plantPos.getX() + 0.5D,
					plantChoice.plantPos.getY(),
					plantChoice.plantPos.getZ() + 0.5D
			) * 0.15D;
			score += plantChoice.sidePenalty;

			Block block = this.engineer.world.getBlockState(wallPos).getBlock();
			String regName = block.getRegistryName() != null ? block.getRegistryName().toString().toLowerCase() : "";

			if (regName.contains("cobble")
					|| regName.contains("brick")
					|| regName.contains("plank")
					|| regName.contains("door")
					|| regName.contains("fence")
					|| regName.contains("wall")
					|| regName.contains("glass")
					|| regName.contains("concrete")
					|| regName.contains("iron")) {
				score -= 50.0D;
			}

			BreachTarget candidate = new BreachTarget(wallPos, plantChoice.plantPos, score);

			if (best == null || candidate.score < best.score) {
				best = candidate;
			}
		}

		return best;
	}

	private PlantChoice findPlantPosForWall(BlockPos wallPos, EntityPlayer target) {
		EnumFacing primary = this.getFacingTowardEngineer(wallPos);

		EnumFacing[] facings = new EnumFacing[] {
				primary,
				primary.rotateY(),
				primary.rotateYCCW(),
				primary.getOpposite()
		};

		PlantChoice best = null;

		for (int i = 0; i < facings.length; i++) {
			EnumFacing facing = facings[i];

			if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
				continue;
			}

			BlockPos plantPos = wallPos.offset(facing);

			if (!this.isValidPlantPos(plantPos)) {
				continue;
			}

			// Do not make the goblin walk directly underneath the player.
			if (this.isBadUnderPlayerTarget(plantPos, target)) {
				continue;
			}

			double sidePenalty = i * 25.0D;

			if (facing == primary.getOpposite()) {
				sidePenalty += 150.0D;
			}

			PlantChoice choice = new PlantChoice(plantPos, sidePenalty);

			if (best == null || choice.sidePenalty < best.sidePenalty) {
				best = choice;
			}
		}

		return best;
	}

	private EnumFacing getFacingTowardEngineer(BlockPos wallPos) {
		double dx = this.engineer.posX - (wallPos.getX() + 0.5D);
		double dz = this.engineer.posZ - (wallPos.getZ() + 0.5D);

		if (Math.abs(dx) > Math.abs(dz)) {
			return dx > 0 ? EnumFacing.EAST : EnumFacing.WEST;
		} else {
			return dz > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
		}
	}

	private boolean isValidWallBlock(BlockPos pos) {
		IBlockState state = this.engineer.world.getBlockState(pos);

		if (state.getMaterial() == Material.AIR || !state.getMaterial().blocksMovement()) {
			return false;
		}

		if (state.getBlockHardness(this.engineer.world, pos) < 0.0F) {
			return false;
		}

		if (this.engineer.world.getTileEntity(pos) != null) {
			return false;
		}

		return true;
	}

	private boolean isValidPlantPos(BlockPos pos) {
		IBlockState feet = this.engineer.world.getBlockState(pos);
		IBlockState head = this.engineer.world.getBlockState(pos.up());
		IBlockState ground = this.engineer.world.getBlockState(pos.down());

		if (feet.getMaterial().blocksMovement()) {
			return false;
		}

		if (head.getMaterial().blocksMovement()) {
			return false;
		}

		if (!ground.getMaterial().blocksMovement()) {
			return false;
		}

		if (feet.getMaterial() == Material.WATER || feet.getMaterial() == Material.LAVA) {
			return false;
		}

		if (head.getMaterial() == Material.WATER || head.getMaterial() == Material.LAVA) {
			return false;
		}

		return true;
	}

	private boolean isBetweenEngineerAndPlayer(BlockPos pos, EntityPlayer target) {
		Vec3d engineerToPlayer = new Vec3d(
				target.posX - this.engineer.posX,
				0,
				target.posZ - this.engineer.posZ
		);

		Vec3d engineerToBlock = new Vec3d(
				pos.getX() + 0.5D - this.engineer.posX,
				0,
				pos.getZ() + 0.5D - this.engineer.posZ
		);

		double playerDistSq = engineerToPlayer.lengthSquared();
		double blockDistSq = engineerToBlock.lengthSquared();

		if (playerDistSq < 0.01D || blockDistSq < 0.01D) {
			return false;
		}

		if (engineerToBlock.dotProduct(engineerToPlayer) <= 0.0D) {
			return false;
		}

		// Slightly allow blocks near or just behind the player.
		// This matters when the player stands on top of the wall.
		if (blockDistSq > playerDistSq + 16.0D) {
			return false;
		}

		return this.getLineDistanceScore(pos, target) <= 16.0D;
	}

	private double getLineDistanceScore(BlockPos pos, EntityPlayer target) {
		Vec3d engineerFlat = new Vec3d(this.engineer.posX, 0, this.engineer.posZ);

		Vec3d engineerToPlayer = new Vec3d(
				target.posX - this.engineer.posX,
				0,
				target.posZ - this.engineer.posZ
		);

		Vec3d engineerToBlock = new Vec3d(
				pos.getX() + 0.5D - this.engineer.posX,
				0,
				pos.getZ() + 0.5D - this.engineer.posZ
		);

		double playerDistSq = engineerToPlayer.lengthSquared();

		if (playerDistSq < 0.01D) {
			return Double.MAX_VALUE;
		}

		double t = engineerToBlock.dotProduct(engineerToPlayer) / playerDistSq;
		t = Math.max(0.0D, Math.min(1.0D, t));

		Vec3d closestPointOnLine = engineerFlat.add(engineerToPlayer.scale(t));
		Vec3d blockFlat = new Vec3d(pos.getX() + 0.5D, 0, pos.getZ() + 0.5D);

		return blockFlat.squareDistanceTo(closestPointOnLine);
	}

	private boolean isBadUnderPlayerTarget(BlockPos pos, EntityPlayer target) {
		double dx = (pos.getX() + 0.5D) - target.posX;
		double dz = (pos.getZ() + 0.5D) - target.posZ;
		double horizontalDistSq = dx * dx + dz * dz;

		boolean horizontallyUnderPlayer = horizontalDistSq < 1.75D;
		boolean belowPlayerFeet = pos.getY() < Math.floor(target.posY);

		return horizontallyUnderPlayer && belowPlayerFeet;
	}

	private boolean hasOtherBombNearby(BlockPos pos, List<EntityWroughtBomb> bombs) {
		AxisAlignedBB box = new AxisAlignedBB(pos).grow(5.0D);

		for (EntityWroughtBomb bomb : bombs) {
			if (bomb == null || bomb.isDead) {
				continue;
			}

			if (bomb == this.mountedBomb || bomb.getRidingEntity() == this.engineer) {
				continue;
			}

			if (box.contains(bomb.getPositionVector())) {
				return true;
			}
		}

		return false;
	}

	private boolean isAlreadyTargeted(BlockPos wallPos, List<EntityGoblin> goblins) {
		for (EntityGoblin goblin : goblins) {
			if (goblin == this.engineer || goblin.isDead) {
				continue;
			}

			BlockPos otherTarget = goblin.getBombDeliveryTarget();

			if (otherTarget != null && otherTarget.distanceSq(wallPos) < 16.0D) {
				return true;
			}
		}

		return false;
	}

	private boolean canNavigatorReach(PathNavigate navigator, BlockPos pos) {
		Path path = navigator.getPathToPos(pos);

		if (path == null || path.getFinalPathPoint() == null) {
			return false;
		}

		PathPoint end = path.getFinalPathPoint();

		return pos.distanceSq(end.x, end.y, end.z) <= 6.0D;
	}

	private void moveToPlantPos() {
		if (this.targetPlantPos == null) {
			return;
		}

		this.navigator.tryMoveToXYZ(
				this.targetPlantPos.getX() + 0.5D,
				this.targetPlantPos.getY(),
				this.targetPlantPos.getZ() + 0.5D,
				1.3D
		);
	}

	private EntityGoblin findNearbyUnarmedBrood(BlockPos plantPos) {
		EntityGoblin best = null;
		double bestScore = Double.MAX_VALUE;

		List<EntityGoblin> broods = this.engineer.world.getEntitiesWithinAABB(
				EntityGoblin.class,
				this.engineer.getEntityBoundingBox().grow(16.0D)
		);

		for (EntityGoblin brood : broods) {
			if (brood == this.engineer) {
				continue;
			}

			if (brood.getClass() != EntityGoblin.class) {
				continue;
			}

			if (brood.isDead || brood.isCarryingBomb() || brood.isHoldingIdol()) {
				continue;
			}

			double distToEngineer = brood.getDistanceSq(this.engineer);
			double distToPlant = brood.getDistanceSq(
					plantPos.getX() + 0.5D,
					plantPos.getY(),
					plantPos.getZ() + 0.5D
			);

			double score = distToPlant + distToEngineer * 0.4D;

			if (score < bestScore) {
				bestScore = score;
				best = brood;
			}
		}

		return best;
	}

	private static class BreachTarget {
		private final BlockPos wallPos;
		private final BlockPos plantPos;
		private final double score;

		private BreachTarget(BlockPos wallPos, BlockPos plantPos, double score) {
			this.wallPos = wallPos;
			this.plantPos = plantPos;
			this.score = score;
		}
	}

	private static class PlantChoice {
		private final BlockPos plantPos;
		private final double sidePenalty;

		private PlantChoice(BlockPos plantPos, double sidePenalty) {
			this.plantPos = plantPos;
			this.sidePenalty = sidePenalty;
		}
	}
}