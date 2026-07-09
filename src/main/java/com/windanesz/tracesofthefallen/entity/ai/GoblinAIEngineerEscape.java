package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityGoblinEngineer;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class GoblinAIEngineerEscape extends EntityAIBase {
	private final EntityGoblinEngineer engineer;
	private final PathNavigate navigator;
	private EntityWroughtBomb targetBomb;
	private boolean fleeing;
	private double randPosX;
	private double randPosY;
	private double randPosZ;

	public GoblinAIEngineerEscape(EntityGoblinEngineer engineer) {
		this.engineer = engineer;
		this.navigator = engineer.getNavigator();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.engineer.getHealth() >= this.engineer.getMaxHealth() * 0.3F) {
			return false;
		}

		if (this.engineer.hasMountedBomb()) {
			EntityWroughtBomb mounted = this.engineer.getMountedBomb();
			if (mounted != null && !this.engineer.world.isRemote) {
				mounted.dismountRidingEntity();
				mounted.setPosition(this.engineer.posX, this.engineer.posY, this.engineer.posZ);
			}
		}

		this.targetBomb = this.findClosestDroppedBomb();
		this.fleeing = false;

		if (this.targetBomb != null) {
			if (this.engineer.getDistanceSq(this.targetBomb) <= 9.0D) {
				this.igniteAndFlee();
			}
			return true;
		}

		EntityLivingBase threat = this.engineer.getRevengeTarget();
		if (threat == null) {
			threat = this.engineer.getAttackTarget();
		}
		if (threat == null) {
			threat = this.engineer.world.getClosestPlayerToEntity(this.engineer, 16.0D);
		}
		if (threat == null) {
			return false;
		}

		this.startFleeingFrom(threat.getPositionVector());
		return true;
	}

	@Override
	public void startExecuting() {
		if (this.fleeing) {
			this.navigator.tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, 1.4D);
		} else if (this.targetBomb != null) {
			if (this.engineer.getDistanceSq(this.targetBomb) <= 9.0D) {
				this.igniteAndFlee();
			} else {
				this.navigator.tryMoveToXYZ(this.targetBomb.posX, this.targetBomb.posY, this.targetBomb.posZ, 1.3D);
			}
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.engineer.getHealth() >= this.engineer.getMaxHealth() * 0.3F) {
			return false;
		}
		if (this.fleeing) {
			return !this.navigator.noPath();
		}
		if (this.targetBomb == null || this.targetBomb.isDead || this.targetBomb.getFuse() > 0) {
			if (this.targetBomb != null) {
				this.igniteAndFlee();
				return !this.navigator.noPath();
			}
			return false;
		}
		return !this.navigator.noPath() || this.engineer.getDistanceSq(this.targetBomb) > 9.0D;
	}

	@Override
	public void updateTask() {
		if (this.fleeing) {
			if (this.navigator.noPath() || this.engineer.ticksExisted % 10 == 0) {
				Vec3d threatPos = this.targetBomb != null ? this.targetBomb.getPositionVector() : null;
				if (threatPos == null) {
					EntityLivingBase threat = this.engineer.getRevengeTarget();
					if (threat == null) {
						threat = this.engineer.getAttackTarget();
					}
					if (threat != null) {
						threatPos = threat.getPositionVector();
					}
				}
				if (threatPos != null) {
					Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.engineer, 16, 7, threatPos);
					if (fleePos != null) {
						this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
					}
				}
			}
			return;
		}

		if (this.targetBomb != null && !this.targetBomb.isDead) {
			this.engineer.getLookHelper().setLookPositionWithEntity(this.targetBomb, 30.0F, 30.0F);

			if (this.engineer.getDistanceSq(this.targetBomb) <= 9.0D) {
				this.igniteAndFlee();
			} else {
				if (this.navigator.noPath() || this.engineer.ticksExisted % 10 == 0) {
					this.navigator.tryMoveToXYZ(this.targetBomb.posX, this.targetBomb.posY, this.targetBomb.posZ, 1.3D);
				}
			}
		}
	}

	@Override
	public void resetTask() {
		this.targetBomb = null;
		this.fleeing = false;
		this.navigator.clearPath();
	}

	private void igniteAndFlee() {
		if (this.targetBomb != null && !this.targetBomb.isDead && this.targetBomb.getFuse() <= 0) {
			if (!this.engineer.world.isRemote) {
				this.targetBomb.setFuse(Settings.miscSettings.wroughtBombFuseTime);
				this.engineer.world.playSound(
						null,
						this.targetBomb.posX,
						this.targetBomb.posY,
						this.targetBomb.posZ,
						SoundEvents.ENTITY_TNT_PRIMED,
						SoundCategory.BLOCKS,
						1.0F,
						1.0F
				);
			}
		}

		this.fleeing = true;
		Vec3d threatPos = this.targetBomb != null ? this.targetBomb.getPositionVector() : null;
		if (threatPos == null) {
			EntityLivingBase threat = this.engineer.getRevengeTarget();
			if (threat == null) {
				threat = this.engineer.getAttackTarget();
			}
			if (threat != null) {
				threatPos = threat.getPositionVector();
			}
		}

		this.startFleeingFrom(threatPos);
	}

	private void startFleeingFrom(Vec3d threatPos) {
		this.fleeing = true;
		if (threatPos != null) {
			Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.engineer, 16, 7, threatPos);
			if (fleePos != null) {
				this.randPosX = fleePos.x;
				this.randPosY = fleePos.y;
				this.randPosZ = fleePos.z;
				this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
			}
		}
	}

	private EntityWroughtBomb findClosestDroppedBomb() {
		List<EntityWroughtBomb> bombs = this.engineer.world.getEntitiesWithinAABB(
				EntityWroughtBomb.class,
				this.engineer.getEntityBoundingBox().grow(32.0D)
		);

		EntityWroughtBomb closest = null;
		double closestDistSq = Double.MAX_VALUE;

		for (EntityWroughtBomb bomb : bombs) {
			if (!bomb.isDead && !bomb.isRiding() && bomb.getFuse() <= 0) {
				double distSq = this.engineer.getDistanceSq(bomb);
				if (distSq < closestDistSq) {
					closestDistSq = distSq;
					closest = bomb;
				}
			}
		}

		return closest;
	}
}
