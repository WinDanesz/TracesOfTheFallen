package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import net.minecraft.entity.ai.EntityAIBase;

public class GoblinAIShamanStandStill extends EntityAIBase {
	private final EntityGoblinShaman shaman;

	public GoblinAIShamanStandStill(EntityGoblinShaman shaman) {
		this.shaman = shaman;
		this.setMutexBits(3); // Block movement and looking
	}

	@Override
	public boolean shouldExecute() {
		return this.shaman.getSpellCastingTimer() > 0 && (this.shaman.getSpellCastType() == 2 || this.shaman.getSpellCastType() == 4);
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public void startExecuting() {
		this.shaman.getNavigator().clearPath();
		if (this.shaman.onGround) {
			this.shaman.motionX = 0.0D;
			this.shaman.motionZ = 0.0D;
		}
	}

	@Override
	public void updateTask() {
		this.shaman.getNavigator().clearPath();
		if (this.shaman.onGround) {
			this.shaman.motionX = 0.0D;
			this.shaman.motionZ = 0.0D;
		}
		if (this.shaman.getAttackTarget() != null) {
			this.shaman.getLookHelper().setLookPositionWithEntity(this.shaman.getAttackTarget(), 30.0F, 30.0F);
		}
	}
}
