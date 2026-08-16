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
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/hay_bed"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/goblin_nest"));
		
		// Entity loot tables
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/goblin"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/minecrawler"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/lost"));
	}

	@net.minecraftforge.fml.common.eventhandler.SubscribeEvent
	public static void onLivingDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
		if (net.minecraftforge.fml.common.Loader.isModLoaded("mod_lavacow")) {
			if (event.getEntityLiving() instanceof com.windanesz.tracesofthefallen.entity.EntitySpecter) {
				float chance = event.getEntityLiving() instanceof com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper ? 0.3F : 0.1F;
				chance += event.getLootingLevel() * 0.05F;
				
				if (event.getEntityLiving().world.rand.nextFloat() < chance) {
					net.minecraft.item.Item ectoplasm = net.minecraft.item.Item.REGISTRY.getObject(new ResourceLocation("mod_lavacow", "ectoplasm"));
					if (ectoplasm != null) {
						event.getDrops().add(new net.minecraft.entity.item.EntityItem(
								event.getEntityLiving().world, 
								event.getEntityLiving().posX, 
								event.getEntityLiving().posY, 
								event.getEntityLiving().posZ, 
								new net.minecraft.item.ItemStack(ectoplasm)
						));
					}
				}
			}
		}
	}
}