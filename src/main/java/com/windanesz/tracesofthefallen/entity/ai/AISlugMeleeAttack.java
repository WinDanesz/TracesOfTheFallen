package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntitySlug;
import net.minecraft.entity.ai.EntityAIAttackMelee;

/**
 * Ordinary ground melee chase, used once a slug has been hurt and given up on stealth.
 */
public class AISlugMeleeAttack extends EntityAIAttackMelee {

	private final EntitySlug slug;

	public AISlugMeleeAttack(EntitySlug slug, double speedIn) {
		super(slug, speedIn, false);
		this.slug = slug;
	}

	@Override
	public boolean shouldExecute() {
		return this.slug.isRevealed() && !this.slug.isAttachedActive() && super.shouldExecute();
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.slug.isRevealed() && !this.slug.isAttachedActive() && super.shouldContinueExecuting();
	}
}
