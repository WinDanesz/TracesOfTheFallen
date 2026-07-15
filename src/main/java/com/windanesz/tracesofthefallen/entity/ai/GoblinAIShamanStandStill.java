package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpell;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells;
import net.minecraft.entity.ai.EntityAIBase;

public class GoblinAIShamanStandStill extends EntityAIBase {
	private final EntityGoblinShaman shaman;

	public GoblinAIShamanStandStill(EntityGoblinShaman shaman) {
		this.shaman = shaman;
		this.setMutexBits(1); // Block movement while casting spells that stop movement (Bit 1 only, leaving Bit 2 for looking/casting)
	}

	@Override
	public boolean shouldExecute() {
		if (this.shaman.getSpellCastingTimer() <= 0 || this.shaman.getSpellCastType() <= 0) {
			return false;
		}
		ShamanSpell spell = ShamanSpells.getSpellById(this.shaman.getSpellCastType());
		return spell != null && spell.stopsMovement();
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
