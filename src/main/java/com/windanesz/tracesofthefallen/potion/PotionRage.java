package com.windanesz.tracesofthefallen.potion;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public class PotionRage extends PotionTotf {

	public PotionRage(String name, boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(name, isBadEffectIn, liquidColorIn, texture);
		registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"5e3b2d31-9e22-43b9-9f61-67e6e1afad6f", 0.2D, 2);
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		Entity source = event.getSource().getTrueSource();
		if (source instanceof EntityLivingBase) {
			PotionEffect rage = ((EntityLivingBase) source).getActivePotionEffect(ModPotions.rage);
			if (rage != null) {
				event.setAmount(event.getAmount() * getOutgoingDamageMultiplier(rage.getAmplifier()));
			}
		}

		PotionEffect rage = event.getEntityLiving().getActivePotionEffect(ModPotions.rage);
		if (rage != null) {
			event.setAmount(event.getAmount() * getIncomingDamageMultiplier(rage.getAmplifier()));
		}
	}

	private static float getOutgoingDamageMultiplier(int amplifier) {
		return 1.35F + (0.15F * Math.min(amplifier, 2));
	}

	private static float getIncomingDamageMultiplier(int amplifier) {
		return 1.25F + (0.10F * Math.min(amplifier, 2));
	}
}
