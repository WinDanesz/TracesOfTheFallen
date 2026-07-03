package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GoblinAIBroodDeliverBomb extends EntityAIBase {
	private final EntityGoblin goblin;
	private final PathNavigate navigator;
	private EntityWroughtBomb deliveredBomb;
	private int deliveryTimer;

	public GoblinAIBroodDeliverBomb(EntityGoblin goblin) {
		this.goblin = goblin;
		this.navigator = goblin.getNavigator();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.goblin.getClass() != EntityGoblin.class) {
			return false;
		}
		BlockPos target = this.goblin.getBombDeliveryTarget();
		if (target == null) {
			return false;
		}
		if (!this.goblin.isCarryingBomb()) {
			this.goblin.setBombDeliveryTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public void startExecuting() {
		this.deliveryTimer = 0;
		EntityWroughtBomb carried = this.getCarryingBomb();
		if (carried != null && carried.getFuse() <= 0) {
			this.navigator.clearPath();
			return;
		}
		BlockPos target = this.goblin.getBombDeliveryTarget();
		if (target != null) {
			Path path = this.navigator.getPathToPos(target);
			if (path != null && path.getFinalPathPoint() != null) {
				PathPoint pt = path.getFinalPathPoint();
				this.navigator.tryMoveToXYZ(pt.x + 0.5D, pt.y, pt.z + 0.5D, 1.4D);
			} else {
				this.navigator.tryMoveToXYZ(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.4D);
			}
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.deliveredBomb != null) {
			return !this.deliveredBomb.isDead && this.goblin.getDistanceSq(this.deliveredBomb) < 144.0D;
		}
		return this.goblin.getBombDeliveryTarget() != null && this.goblin.isCarryingBomb();
	}

	@Override
	public void updateTask() {
		if (this.deliveredBomb != null) {
			if (!this.deliveredBomb.isDead) {
				if (this.deliveredBomb.getFuse() <= 0) {
					if (!this.navigator.noPath()) {
						this.navigator.clearPath();
					}
					this.goblin.getLookHelper().setLookPositionWithEntity(this.deliveredBomb, 30.0F, 30.0F);
					return;
				}

				if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
					Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.goblin, 16, 7, this.deliveredBomb.getPositionVector());
					if (fleePos != null) {
						this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
					}
				}
			}
			return;
		}

		BlockPos target = this.goblin.getBombDeliveryTarget();
		if (target == null) return;

		EntityWroughtBomb carried = this.getCarryingBomb();
		if (carried != null && carried.getFuse() <= 0) {
			if (!this.navigator.noPath()) {
				this.navigator.clearPath();
			}
			this.goblin.getLookHelper().setLookPosition(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 10.0F, (float) this.goblin.getVerticalFaceSpeed());
			return;
		}

		this.deliveryTimer++;
		this.goblin.getLookHelper().setLookPosition(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 10.0F, (float) this.goblin.getVerticalFaceSpeed());

		if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
			Path path = this.navigator.getPathToPos(target);
			if (path != null && path.getFinalPathPoint() != null) {
				PathPoint pt = path.getFinalPathPoint();
				this.navigator.tryMoveToXYZ(pt.x + 0.5D, pt.y, pt.z + 0.5D, 1.4D);
			} else {
				this.navigator.tryMoveToXYZ(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.4D);
			}
		}

		double dx = (target.getX() + 0.5D) - this.goblin.posX;
		double dz = (target.getZ() + 0.5D) - this.goblin.posZ;
		double horizDistSq = dx * dx + dz * dz;

		if ((horizDistSq <= 4.5D && Math.abs(this.goblin.posY - target.getY()) <= 3.0D) || (this.navigator.noPath() && this.deliveryTimer > 30)) {
			EntityWroughtBomb bomb = this.getCarryingBomb();
			if (bomb != null && !this.goblin.world.isRemote) {
				bomb.dismountRidingEntity();
				Vec3d dirToWall = new Vec3d((target.getX() + 0.5D) - this.goblin.posX, 0, (target.getZ() + 0.5D) - this.goblin.posZ).normalize();
				bomb.setPosition(this.goblin.posX + dirToWall.x * 0.5D, this.goblin.posY, this.goblin.posZ + dirToWall.z * 0.5D);
				this.deliveredBomb = bomb;
			}
			this.goblin.setBombDeliveryTarget(null);

			// Flee from the delivered bomb spot once fuse is lit
			if (this.deliveredBomb != null && this.deliveredBomb.getFuse() > 0) {
				Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.goblin, 16, 7, this.deliveredBomb.getPositionVector());
				if (fleePos != null) {
					this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
				}
			} else {
				this.navigator.clearPath();
			}
		}
	}

	@Override
	public void resetTask() {
		this.goblin.setBombDeliveryTarget(null);
		this.deliveredBomb = null;
		this.navigator.clearPath();
	}

	private EntityWroughtBomb getCarryingBomb() {
		for (Entity passenger : this.goblin.getPassengers()) {
			if (passenger instanceof EntityWroughtBomb) {
				return (EntityWroughtBomb) passenger;
			}
		}
		return null;
	}
}
