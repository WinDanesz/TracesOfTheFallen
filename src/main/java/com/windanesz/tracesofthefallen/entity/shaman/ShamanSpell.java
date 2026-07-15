package com.windanesz.tracesofthefallen.entity.shaman;

import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import net.minecraft.entity.EntityLivingBase;

/**
 * Abstract base class for all spells castable by an {@link EntityGoblinShaman}.
 * <p>
 * Encapsulates the spell ID, associated {@link SpellSchool}, required mastery tier,
 * and casting duration. Subclasses implement specific condition checks, particle effects,
 * audio, and spell releases.
 * </p>
 */
public abstract class ShamanSpell {
	private final String spellName;
	private final int spellId;
	private final SpellSchool school;
	private final int requiredMastery;
	private final int castDuration;

	public ShamanSpell(String spellName, int spellId, SpellSchool school, int requiredMastery, int castDuration) {
		this.spellName = spellName;
		this.spellId = spellId;
		this.school = school;
		this.requiredMastery = requiredMastery;
		this.castDuration = castDuration;
	}

	public ShamanSpell(int spellId, SpellSchool school, int requiredMastery, int castDuration) {
		this("Spell " + spellId, spellId, school, requiredMastery, castDuration);
	}

	public String getSpellName() {
		return this.spellName;
	}

	public int getSpellId() {
		return this.spellId;
	}

	public SpellSchool getSchool() {
		return this.school;
	}

	public int getRequiredMastery() {
		return this.requiredMastery;
	}

	public int getCastDuration() {
		return this.castDuration;
	}

	/**
	 * Checks whether the given shaman satisfies the school mastery requirement (via active feathers) to know this spell.
	 */
	public boolean canShamanCast(EntityGoblinShaman shaman) {
		return shaman.getMasteryForSchool(this.school) >= this.requiredMastery;
	}

	/**
	 * Calculates standardized cooldown duration based on spell tier and school.
	 * Lower tier spells have lower cooldowns. Support spells have longer cooldowns than base spells.
	 */
	public int getTierCooldown(EntityGoblinShaman shaman) {
		int tier = this.getRequiredMastery();
		if (this.getSchool() == SpellSchool.SUPPORT) {
			if (tier <= 1) {
				return 360 + shaman.getRNG().nextInt(80); // Support Tier 1 (e.g. Bone Rattle): 18 to 22 seconds
			} else if (tier == 2) {
				return 560 + shaman.getRNG().nextInt(120); // Support Tier 2 (e.g. Blood Totem): 28 to 34 seconds
			} else {
				return 800 + shaman.getRNG().nextInt(160); // Support Tier 3: 40 to 48 seconds
			}
		} else {
			if (tier <= 1) {
				return 60 + shaman.getRNG().nextInt(40); // Base Tier 1: 5 to 7 seconds
			} else if (tier == 2) {
				return 160 + shaman.getRNG().nextInt(40); // Base Tier 2: 8 to 10 seconds
			} else {
				return 240 + shaman.getRNG().nextInt(60); // Base Tier 3: 12 to 15 seconds
			}
		}
	}

	/**
	 * Checks if the spell is off cooldown, learned, and the target is valid, regardless of probabilistic RNG checks.
	 */
	public boolean canStartCastingIgnoringRng(EntityGoblinShaman shaman, EntityLivingBase target) {
		if (shaman.globalSpellCooldown > 0 || shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
			return false;
		}
		if (target == null || !target.isEntityAlive() || ShamanSpells.isAlly(shaman, target)) {
			return false;
		}
		double distSq = shaman.getDistanceSq(target);
		return distSq < 256.0D && shaman.getEntitySenses().canSee(target);
	}

	/**
	 * Evaluates situational conditions (cooldown, range, line of sight, target state)
	 * to determine if the shaman should begin casting this spell during the current tick.
	 */
	public abstract boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target);

	/**
	 * Called immediately when casting begins (timer set to castDuration).
	 * Typically used to play casting sounds, set spell cooldowns, or equip focus items.
	 */
	public abstract void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target);

	/**
	 * Called every tick while the cast timer is counting down (> 0).
	 * Used for continuous casting particle effects or intermediate projectile bursts.
	 *
	 * @param castTimer The remaining ticks in the cast duration.
	 */
	public abstract void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer);

	/**
	 * Called when the cast timer reaches exactly 1 tick remaining, triggering the spell's main release action.
	 */
	public abstract void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target);

	/**
	 * Returns whether the shaman should stop moving and stand still while chanting this spell.
	 * Defaults to false (mobile casting). Override to true for stationary rituals/waves.
	 */
	public boolean stopsMovement() {
		return false;
	}

	/**
	 * Returns the utility score for choosing this spell when multiple spells can be cast.
	 * Higher scores indicate higher priority.
	 */
	public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
		return this.shouldStartCasting(shaman, target) ? 1.0F : 0.0F;
	}
}
