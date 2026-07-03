package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class GoblinAIFleeTickingBomb extends EntityAIBase {
	private final EntityGoblin goblin;
	private final PathNavigate navigator;
	private EntityWroughtBomb tickingBomb;

	public GoblinAIFleeTickingBomb(EntityGoblin goblin) {
		this.goblin = goblin;
		this.navigator = goblin.getNavigator();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.goblin.isCarryingBomb()) {
			return false; // Don't flee from a bomb you are carrying to deliver
		}

		List<EntityWroughtBomb> bombs = this.goblin.world.getEntitiesWithinAABB(EntityWroughtBomb.class, this.goblin.getEntityBoundingBox().grow(12.0D));
		EntityWroughtBomb closest = null;
		double closestDistSq = 144.0D;

		for (EntityWroughtBomb bomb : bombs) {
			if (!bomb.isDead && bomb.getFuse() > 0 && bomb.getRidingEntity() != this.goblin) {
				double distSq = this.goblin.getDistanceSq(bomb);
				if (distSq < closestDistSq) {
					closestDistSq = distSq;
					closest = bomb;
				}
			}
		}

		if (closest == null) {
			return false;
		}

		this.tickingBomb = closest;
		Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.goblin, 16, 7, this.tickingBomb.getPositionVector());
		if (fleePos == null) {
			return false;
		}

		this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
		return true;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.tickingBomb != null && !this.tickingBomb.isDead && this.tickingBomb.getFuse() > 0 && this.goblin.getDistanceSq(this.tickingBomb) < 144.0D;
	}

	@Override
	public void updateTask() {
		if (this.tickingBomb != null && !this.tickingBomb.isDead) {
			if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
				Vec3d fleePos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.goblin, 16, 7, this.tickingBomb.getPositionVector());
				if (fleePos != null) {
					this.navigator.tryMoveToXYZ(fleePos.x, fleePos.y, fleePos.z, 1.4D);
				}
			}
		}
	}
}
