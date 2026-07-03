package com.windanesz.tracesofthefallen.potion;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class RageEventHandler {

	private RageEventHandler() {
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		EntityLivingBase attacker = event.getSource().getTrueSource() instanceof EntityLivingBase
				? (EntityLivingBase) event.getSource().getTrueSource() : null;

		if (attacker != null) {
			PotionEffect rage = attacker.getActivePotionEffect(ModPotions.rage);
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
