package com.windanesz.tracesofthefallen.potion;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;

public class PotionRage extends PotionTotf {

	public PotionRage(String name, boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(name, isBadEffectIn, liquidColorIn, texture);
		registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"5e3b2d31-9e22-43b9-9f61-67e6e1afad6f", 0.2D, 2);
	}
}
