package com.windanesz.tracesofthefallen.potion;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class PotionBliss extends PotionTotf {

	public PotionBliss(String name, boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(name, isBadEffectIn, liquidColorIn, texture);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		int k = 50 >> amplifier;
		if (k > 0) {
			return duration % k == 0;
		} else {
			return true;
		}
	}

	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn.getHealth() < entityLivingBaseIn.getMaxHealth()) {
            // Use the value from settings for healing.
            entityLivingBaseIn.heal((float) Settings.miscSettings.blissHealingAmount);
        }
    }
}
