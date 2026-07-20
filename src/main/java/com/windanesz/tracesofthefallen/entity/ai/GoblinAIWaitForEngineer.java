package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityGoblinEngineer;
import com.windanesz.tracesofthefallen.entity.EntityGoblinSapper;
import com.windanesz.tracesofthefallen.entity.EntityGoblinTunneler;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GoblinAIWaitForEngineer extends EntityAIBase {
	private final EntityGoblin goblin;
	private final PathNavigate navigator;
	private EntityGoblin nearbyLeader;

	public GoblinAIWaitForEngineer(EntityGoblin goblin) {
		this.goblin = goblin;
		this.navigator = goblin.getNavigator();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.goblin.getClass() != EntityGoblin.class) {
			return false;
		}
		if (this.goblin.isCarryingBomb() || this.goblin.isHoldingIdol() || this.goblin.getBombDeliveryTarget() != null) {
			return false;
		}
		if (this.goblin.getAttackTarget() != null && this.goblin.getDistanceSq(this.goblin.getAttackTarget()) < 12.25D) {
			return false;
		}

		List<EntityGoblin> leaders = this.goblin.world.getEntitiesWithinAABB(
				EntityGoblin.class,
				this.goblin.getEntityBoundingBox().grow(64.0D)
		);

		EntityGoblin closestLeader = null;
		double closestDistSq = Double.MAX_VALUE;

		for (EntityGoblin leader : leaders) {
			if (leader == this.goblin || leader.isDead || leader.getHealth() <= leader.getMaxHealth() / 2.0F) {
				continue;
			}
			boolean isValidLeader = false;
			if (leader instanceof EntityGoblinTunneler || leader instanceof EntityGoblinSapper) {
				isValidLeader = true;
			} else if (leader instanceof EntityGoblinEngineer && leader.isCarryingBomb()) {
				isValidLeader = true;
			}
			if (!isValidLeader) {
				continue;
			}
			double distSq = this.goblin.getDistanceSq(leader);
			if (distSq < closestDistSq) {
				closestDistSq = distSq;
				closestLeader = leader;
			}
		}

		if (closestLeader == null) {
			return false;
		}

		this.nearbyLeader = closestLeader;
		return true;
	}

	@Override
	public void startExecuting() {
		this.navigator.clearPath();
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.goblin.isCarryingBomb() || this.goblin.isHoldingIdol() || this.goblin.getBombDeliveryTarget() != null) {
			return false;
		}
		if (this.nearbyLeader == null || this.nearbyLeader.isDead) {
			return false;
		}
		if (this.nearbyLeader instanceof EntityGoblinEngineer && !this.nearbyLeader.isCarryingBomb()) {
			return false;
		}
		if (this.goblin.getDistanceSq(this.nearbyLeader) > 4096.0D) {
			return false;
		}
		if (this.goblin.getAttackTarget() != null && this.goblin.getDistanceSq(this.goblin.getAttackTarget()) < 12.25D) {
			return false;
		}
		return true;
	}

	@Override
	public void updateTask() {
		if (this.nearbyLeader == null) {
			return;
		}

		this.goblin.getLookHelper().setLookPositionWithEntity(
				this.nearbyLeader,
				10.0F,
				(float) this.goblin.getVerticalFaceSpeed()
		);

		double distSq = this.goblin.getDistanceSq(this.nearbyLeader);
		if (distSq > 3.0D) {
			if (this.nearbyLeader.posY > this.goblin.posY + 1.0D) {
				BlockPos shaftUp = findNearbyShaftUp();
				if (shaftUp != null) {
					if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
						this.navigator.tryMoveToXYZ(shaftUp.getX() + 0.5D, shaftUp.getY(), shaftUp.getZ() + 0.5D, 1.25D);
					}
					double hdx = (shaftUp.getX() + 0.5D) - this.goblin.posX;
					double hdz = (shaftUp.getZ() + 0.5D) - this.goblin.posZ;
					double hDist = Math.sqrt(hdx * hdx + hdz * hdz);
					if (hDist < 3.0D && hDist > 0.05D) {
						this.goblin.getMoveHelper().setMoveTo(shaftUp.getX() + 0.5D, this.goblin.posY, shaftUp.getZ() + 0.5D, 0.5D);
					}
				} else if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
					double angle = this.goblin.getEntityId() * 1.5;
					double rx = this.nearbyLeader.posX + Math.cos(angle) * 1.5;
					double rz = this.nearbyLeader.posZ + Math.sin(angle) * 1.5;
					this.navigator.tryMoveToXYZ(rx, this.nearbyLeader.posY, rz, 1.15D);
				}
			} else if (this.nearbyLeader.posY < this.goblin.posY - 1.0D) {
				if (this.goblin.isOnLadder()) {
					this.navigator.clearPath();
					BlockPos p = this.goblin.getActiveLadderPos();
					if (p == null) p = new BlockPos(this.goblin);
					double cx = p.getX() + 0.5D;
					double cz = p.getZ() + 0.5D;
					this.goblin.getMoveHelper().setMoveTo(cx, this.goblin.posY, cz, 0.5D);
				} else {
					BlockPos shaftDown = findNearbyShaftDown();
					if (shaftDown != null) {
						if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
							this.navigator.tryMoveToXYZ(shaftDown.getX() + 0.5D, shaftDown.getY(), shaftDown.getZ() + 0.5D, 1.25D);
						}
						double hdx = (shaftDown.getX() + 0.5D) - this.goblin.posX;
						double hdz = (shaftDown.getZ() + 0.5D) - this.goblin.posZ;
						double hDist = Math.sqrt(hdx * hdx + hdz * hdz);
						if (hDist < 3.0D && hDist > 0.05D) {
							this.goblin.getMoveHelper().setMoveTo(shaftDown.getX() + 0.5D, this.goblin.posY, shaftDown.getZ() + 0.5D, 0.5D);
						}
					} else if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
						double angle = this.goblin.getEntityId() * 1.5;
						double rx = this.nearbyLeader.posX + Math.cos(angle) * 1.5;
						double rz = this.nearbyLeader.posZ + Math.sin(angle) * 1.5;
						this.navigator.tryMoveToXYZ(rx, this.nearbyLeader.posY, rz, 1.15D);
					}
				}
			} else {
				if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
					double angle = this.goblin.getEntityId() * 1.5;
					double rx = this.nearbyLeader.posX + Math.cos(angle) * 1.5;
					double rz = this.nearbyLeader.posZ + Math.sin(angle) * 1.5;
					this.navigator.tryMoveToXYZ(rx, this.nearbyLeader.posY, rz, 1.15D);
				}
			}
		} else {
			if (!this.navigator.noPath()) {
				this.navigator.clearPath();
			}
		}
	}

	private BlockPos findNearbyShaftDown() {
		BlockPos center = new BlockPos(this.goblin);
		BlockPos closest = null;
		double closestDistSq = Double.MAX_VALUE;
		for (int dx = -12; dx <= 12; dx++) {
			for (int dz = -12; dz <= 12; dz++) {
				for (int dy = -2; dy <= 1; dy++) {
					BlockPos pos = center.add(dx, dy, dz);
					if (isAirOrLadder(pos) && isAirOrLadder(pos.down()) && isAirOrLadder(pos.down(2))) {
						double dSq = this.goblin.getDistanceSqToCenter(pos);
						if (dSq < closestDistSq) {
							closestDistSq = dSq;
							closest = pos;
						}
					}
				}
			}
		}
		return closest;
	}

	private BlockPos findNearbyShaftUp() {
		BlockPos center = new BlockPos(this.goblin);
		BlockPos closest = null;
		double closestDistSq = Double.MAX_VALUE;
		for (int dx = -12; dx <= 12; dx++) {
			for (int dz = -12; dz <= 12; dz++) {
				for (int dy = -1; dy <= 3; dy++) {
					BlockPos pos = center.add(dx, dy, dz);
					if (this.goblin.world.getBlockState(pos).getBlock() == Blocks.LADDER) {
						double dSq = this.goblin.getDistanceSqToCenter(pos);
						if (dSq < closestDistSq) {
							closestDistSq = dSq;
							closest = pos;
						}
					}
				}
			}
		}
		return closest;
	}

	private boolean isAirOrLadder(BlockPos pos) {
		return this.goblin.world.isAirBlock(pos) || this.goblin.world.getBlockState(pos).getBlock() == Blocks.LADDER;
	}


	@Override
	public void resetTask() {
		this.nearbyLeader = null;
		this.navigator.clearPath();
	}
}
