package com.windanesz.tracesofthefallen.potion;

import net.minecraft.util.ResourceLocation;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public class PotionSerenity extends PotionTotf {

	// Only holds players that had the effect at the start of the current tick; emptied again at the end of it.
	private static final Map<UUID, Float> TICK_START_EXHAUSTION = new HashMap<>();

	public PotionSerenity(String name, boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(name, isBadEffectIn, liquidColorIn, texture);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		EntityPlayer player = event.player;
		if (player.world.isRemote) {
			return;
		}

		if (event.phase == TickEvent.Phase.START) {
			if (player.isPotionActive(ModPotions.serenity)) {
				TICK_START_EXHAUSTION.put(player.getUniqueID(), player.getFoodStats().foodExhaustionLevel);
			}
			return;
		}

		Float startingExhaustion = TICK_START_EXHAUSTION.remove(player.getUniqueID());
		PotionEffect serenity = startingExhaustion == null ? null : player.getActivePotionEffect(ModPotions.serenity);
		if (serenity == null) {
			return;
		}

		FoodStats foodStats = player.getFoodStats();
		float postUpdateBaseline = getPostUpdateBaseline(startingExhaustion);
		float currentExhaustion = foodStats.foodExhaustionLevel;
		if (currentExhaustion <= postUpdateBaseline) {
			return;
		}

		float exhaustionDelta = currentExhaustion - postUpdateBaseline;
		foodStats.foodExhaustionLevel = postUpdateBaseline + (exhaustionDelta * getExhaustionMultiplier(serenity.getAmplifier()));
	}

	private static float getExhaustionMultiplier(int amplifier) {
		return switch (Math.min(amplifier, 2)) {
			case 0 -> 2.0F / 3.0F;
			case 1 -> 0.5F;
			default -> 0.25F;
		};
	}

	// FoodStats#onUpdate subtracts 4 from the exhaustion once it passes that mark, which must not be scaled.
	private static float getPostUpdateBaseline(float startingExhaustion) {
		return startingExhaustion > 4.0F ? startingExhaustion - 4.0F : startingExhaustion;
	}
}
