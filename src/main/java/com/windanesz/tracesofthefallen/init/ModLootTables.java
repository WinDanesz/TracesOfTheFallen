package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockClayPot;
import com.windanesz.tracesofthefallen.block.TileEntityStoneChest;
import com.windanesz.tracesofthefallen.entity.EntitySpecter;
import com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
		LootTableList.register(BlockClayPot.LOOT_TABLE);
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent_abandoned"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent_with_idol"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/lost_crate_potions"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/bush_crate"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/grave"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/hay_bed"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/minecrawler_cocoon"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/skeleton_corpse"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/skeleton_flower"));
		LootTableList.register(TileEntityStoneChest.LOOT_COMMON);
		LootTableList.register(TileEntityStoneChest.LOOT_RICH);
		
		// Entity loot tables
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/goblin"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/minecrawler"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/lost"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/dungeon_slug"));
		LootTableList.register(new ResourceLocation(TracesOfTheFallen.MODID, "entities/ink_slug"));
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (Loader.isModLoaded("mod_lavacow")) {
			if (event.getEntityLiving() instanceof EntitySpecter) {
				float chance = event.getEntityLiving() instanceof EntitySpecterGrasper ? 0.3F : 0.1F;
				chance += event.getLootingLevel() * 0.05F;
				
				if (event.getEntityLiving().world.rand.nextFloat() < chance) {
					Item ectoplasm = Item.REGISTRY.getObject(new ResourceLocation("mod_lavacow", "ectoplasm"));
					if (ectoplasm != null) {
						event.getDrops().add(new EntityItem(
								event.getEntityLiving().world, 
								event.getEntityLiving().posX, 
								event.getEntityLiving().posY, 
								event.getEntityLiving().posZ, 
								new ItemStack(ectoplasm)
						));
					}
				}
			}
		}
	}
}
