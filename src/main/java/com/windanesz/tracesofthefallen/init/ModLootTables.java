package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModLootTables {

	private ModLootTables() {
	}

	/**
	 * Called from the preInit method in the main mod class to register the custom dungeon loot.
	 */
	public static void register() {
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/lost_cargo"));
		
		// Block loot tables
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent_abandoned"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent_with_idol"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/lost_crate_potions"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/bush_crate"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/grave"));
		
		// Entity loot tables
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/goblin"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/minecrawler"));
	}
}