package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.crafting.RecipeTallieCollectorCombine;
import com.windanesz.tracesofthefallen.crafting.RecipeTallieCollectorSplit;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class ModRecipes {

	private ModRecipes() {
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		IForgeRegistry<IRecipe> registry = event.getRegistry();
		registry.register(new RecipeTallieCollectorCombine()
				.setRegistryName(TracesOfTheFallen.MODID, "tally_collector_combine"));
		registry.register(new RecipeTallieCollectorSplit()
				.setRegistryName(TracesOfTheFallen.MODID, "tally_collector_split"));
	}
}
