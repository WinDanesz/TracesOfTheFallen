package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpell;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class GoblinAIShamanCastSpell extends EntityAIBase {
	private final EntityGoblinShaman shaman;
	private ShamanSpell chosenSpell;

	public GoblinAIShamanCastSpell(EntityGoblinShaman shaman) {
		this.shaman = shaman;
		this.setMutexBits(2); // Bit 2 (combat/look), blocking EntityAIAttackMelee while casting or preparing to cast, while allowing simultaneous navigation (Bit 1) unless activeSpell.stopsMovement()
	}

	@Override
	public boolean shouldExecute() {
		if (this.shaman.isDancing() || this.shaman.getSpellCastingTimer() > 0 || this.shaman.globalSpellCooldown > 0) {
			return false;
		}
		EntityLivingBase target = this.shaman.getAttackTarget();
		if (target == null || !target.isEntityAlive() || ShamanSpells.isAlly(this.shaman, target)) {
			return false;
		}

		ShamanSpell bestSpell = null;
		float bestScore = -1.0F;

		for (int i = 0; i < 6; i++) {
			ShamanSpell spell = this.shaman.getSlotSpell(i);
			if (spell != null && this.shaman.getSlotCooldown(i) <= 0) {
				if (spell.shouldStartCasting(this.shaman, target)) {
					float score = spell.getUtilityScore(this.shaman, target);
					if (score > bestScore) {
						bestScore = score;
						bestSpell = spell;
					}
				}
			}
		}

		if (bestSpell == null) {
			for (int i = 0; i < 6; i++) {
				ShamanSpell spell = this.shaman.getSlotSpell(i);
				if (spell != null && this.shaman.getSlotCooldown(i) <= 0) {
					if (spell.canStartCastingIgnoringRng(this.shaman, target)) {
						bestSpell = spell;
						break;
					}
				}
			}
		}

		if (bestSpell != null) {
			this.chosenSpell = bestSpell;
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase target = this.shaman.getAttackTarget();
		return this.shaman.getSpellCastingTimer() > 0 && this.shaman.isEntityAlive() && !this.shaman.isDancing() && target != null && target.isEntityAlive();
	}

	@Override
	public void startExecuting() {
		if (this.chosenSpell != null) {
			EntityLivingBase target = this.shaman.getAttackTarget();
			this.shaman.setSpellCastType(this.chosenSpell.getSpellId());
			this.shaman.setSpellCastingTimer(this.chosenSpell.getCastDuration());
			this.chosenSpell.onStartCasting(this.shaman, target);
			if (this.chosenSpell.stopsMovement()) {
				this.shaman.getNavigator().clearPath();
			}
		}
	}

	@Override
	public void updateTask() {
		int castTimer = this.shaman.getSpellCastingTimer();
		if (castTimer <= 0) {
			return;
		}

		EntityLivingBase target = this.shaman.getAttackTarget();
		if (target != null) {
			this.shaman.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
		}

		ShamanSpell activeSpell = ShamanSpells.getSpellById(this.shaman.getSpellCastType());
		if (activeSpell != null && activeSpell.stopsMovement()) {
			this.shaman.getNavigator().clearPath();
		}
	}

	@Override
	public void resetTask() {
		if (!this.shaman.world.isRemote && this.shaman.getSpellCastingTimer() <= 0) {
			this.shaman.setSpellCastType(0);
			this.shaman.globalSpellCooldown = 40;
		}
		if (!this.shaman.world.isRemote && !this.shaman.getHeldItemMainhand().isEmpty() && this.shaman.getHeldItemMainhand().getItem() == ModItems.bone_rattle && this.shaman.getSpellCastType() != ShamanSpells.BONE_RATTLE.getSpellId()) {
			this.shaman.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
		this.chosenSpell = null;
	}
}
