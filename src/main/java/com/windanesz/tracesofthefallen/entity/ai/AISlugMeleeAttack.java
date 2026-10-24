package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.living.EntitySlug;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;

/**
 * Ordinary ground melee chase, used once a slug has been hurt and given up on stealth.
 */
public class AISlugMeleeAttack extends EntityAIAttackMelee {
	private static final int WINDUP_TICKS = 9;

	private final EntitySlug slug;
	private int windupTicks;
	private boolean windingUp;

	public AISlugMeleeAttack(EntitySlug slug) {
		// Navigation takes a multiplier; the slug's movement attribute already contains its base speed.
		super(slug, 1.0D, false);
		this.slug = slug;
	}

	@Override
	public boolean shouldExecute() {
		return this.slug.isRevealed() && !this.slug.isAttachedActive() && super.shouldExecute();
	}

	@Override
	public boolean shouldContinueExecuting() {
		// A stationary wind-up deliberately clears navigation. Vanilla's normal continuation check treats
		// an empty path as a reason to stop, which would restart this task every tick before it can strike.
		if (this.windingUp) {
			EntityLivingBase target = this.slug.getAttackTarget();
			return this.slug.isRevealed() && !this.slug.isAttachedActive()
					&& target != null && target.isEntityAlive();
		}
		return this.slug.isRevealed() && !this.slug.isAttachedActive() && super.shouldContinueExecuting();
	}

	@Override
	protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
		if (distToEnemySqr > this.getAttackReachSqr(enemy)) {
			this.windingUp = false;
			this.windupTicks = 0;
			return;
		}

		if (!this.windingUp && this.attackTick <= 0) {
			this.windingUp = true;
			this.windupTicks = WINDUP_TICKS;
			this.slug.startAttackAnimation();
			this.slug.getNavigator().clearPath();
			return;
		}

		if (this.windingUp) {
			this.slug.getNavigator().clearPath();
			this.slug.getLookHelper().setLookPositionWithEntity(enemy, 30.0F, 30.0F);
			if (this.windupTicks-- > 0) {
				return;
			}
			this.windingUp = false;
			this.attackTick = 20;
			this.slug.attackEntityAsMob(enemy);
		}
	}

	@Override
	public void resetTask() {
		super.resetTask();
		this.windingUp = false;
		this.windupTicks = 0;
	}
}
