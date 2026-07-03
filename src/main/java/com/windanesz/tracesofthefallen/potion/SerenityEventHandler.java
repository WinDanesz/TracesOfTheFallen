package com.windanesz.tracesofthefallen.potion;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class SerenityEventHandler {

	private static final Map<UUID, Float> TICK_START_EXHAUSTION = new HashMap<>();

	private SerenityEventHandler() {}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player.world.isRemote) {
			return;
		}

		EntityPlayer player = event.player;
		FoodStats foodStats = player.getFoodStats();

		if (event.phase == TickEvent.Phase.START) {
			TICK_START_EXHAUSTION.put(player.getUniqueID(), foodStats.foodExhaustionLevel);
			return;
		}

		Float startingExhaustion = TICK_START_EXHAUSTION.get(player.getUniqueID());

		if (startingExhaustion == null || !player.isPotionActive(ModPotions.serenity)) {
			return;
		}

		float currentExhaustion = foodStats.foodExhaustionLevel;
		float postUpdateBaseline = getPostUpdateBaseline(startingExhaustion);

		if (currentExhaustion <= postUpdateBaseline) {
			return;
		}

		PotionEffect serenity = player.getActivePotionEffect(ModPotions.serenity);
		float exhaustionDelta = currentExhaustion - postUpdateBaseline;
		foodStats.foodExhaustionLevel = postUpdateBaseline + (exhaustionDelta * getExhaustionMultiplier(serenity.getAmplifier()));
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		TICK_START_EXHAUSTION.remove(event.player.getUniqueID());
	}

	private static float getExhaustionMultiplier(int amplifier) {
		switch (Math.min(amplifier, 2)) {
			case 0:
				return 2.0F / 3.0F;
			case 1:
				return 0.5F;
			default:
				return 0.25F;
		}
	}

	private static float getPostUpdateBaseline(float startingExhaustion) {
		return startingExhaustion > 4.0F ? startingExhaustion - 4.0F : startingExhaustion;
	}
}
