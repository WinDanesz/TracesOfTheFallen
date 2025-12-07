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
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "chests/lost_cargo"));
		//LootTableList.register(new ResourceLocation(LostLoot.MOD_ID, "chests/skeleton_crate"));
	}
}