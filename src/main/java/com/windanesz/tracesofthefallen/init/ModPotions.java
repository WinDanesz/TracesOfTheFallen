package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.potion.PotionBliss;
import com.windanesz.tracesofthefallen.potion.PotionRage;
import com.windanesz.tracesofthefallen.potion.PotionSerenity;
import com.windanesz.tracesofthefallen.potion.PotionStaticVulnerability;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(TracesOfTheFallen.MODID)
@Mod.EventBusSubscriber
public class ModPotions {

	public static final Potion bliss = placeholder();
	public static final Potion serenity = placeholder();
	public static final Potion rage = placeholder();
	public static final Potion static_vulnerability = placeholder();

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder() {
		return null;
	}

	public static void registerPotion(IForgeRegistry<Potion> registry, String name, Potion potion) {
		potion.setRegistryName(TracesOfTheFallen.MODID, name);
		potion.setPotionName("potion." + potion.getRegistryName().toString());
		registry.register(potion);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Potion> event) {

		IForgeRegistry<Potion> registry = event.getRegistry();
		registerPotion(registry, "bliss", new PotionBliss("bliss",false, 0xf799e9, new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/potion_bliss.png")));
		registerPotion(registry, "serenity", new PotionSerenity("serenity", false, 0x7fd8d8, new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/potion_serenity.png")));
		registerPotion(registry, "rage", new PotionRage("rage", false, 0xff8a1f, new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/potion_rage.png")));
		registerPotion(registry, "static_vulnerability", new PotionStaticVulnerability("static_vulnerability", true, 0x00ffff, new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/potion_static_vulnerability.png")));

		// Interestingly, setting the colour to black stops th e particles from rendering.
	}
}