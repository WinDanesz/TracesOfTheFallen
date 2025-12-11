package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.item.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(TracesOfTheFallen.MODID)
@Mod.EventBusSubscriber
public class ModItems {

	private ModItems() {
	}

	public static final Item grave_rose = placeholder();
	public static final Item rune_of_skimming = placeholder();
	public static final Item goblin_idol = placeholder();
	public static final Item pemmican = placeholder();
	public static final Item mysterious_fur = placeholder();
	public static final Item veiled_mask = placeholder();

	public static final Item painting_in_the_woods = placeholder();
	public static final Item painting_portrait = placeholder();
	public static final Item painting_the_bloodcurling = placeholder();
	public static final Item painting_wheel = placeholder();
	public static final Item painting_wizardry = placeholder();
	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder() {
		return null;
	}

	// New method for ItemBlock registration
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registerItem(registry, "grave_rose", new ItemGraveRose(ModBlocks.grave_rose).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "rune_of_skimming", new ItemRuneOfSkimming().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "goblin_idol", new ItemGoblinIdol().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "pemmican", new ItemFood(10, 0.8f, true).setCreativeTab(ModCreativeTab.TOTF_TAB));

		registerItem(registry, "painting_in_the_woods", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_IN_THE_WOODS).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_portrait", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_PORTRAIT).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_the_bloodcurling", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_THE_BLOODCURLING).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_wheel", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_WHEEL).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_wizardry", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_WIZARDRY).setCreativeTab(ModCreativeTab.TOTF_TAB));

		registerItem(registry, "mysterious_fur", new ItemMysteriousFur().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "silk_rope", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "silk_spindle", new ItemSilkSpindle().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "bundle_of_lost_letters", new ItemBundleOfLostLetters().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "veiled_mask", new ItemVeiledMask().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "wheel", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "wonder_fertilizer", new ItemWonderFertilizer().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "old_world_tinkers_kit", new ItemOldWorldTinkersKit().setCreativeTab(ModCreativeTab.TOTF_TAB));

		// Register ItemBlocks
		registerItemBlock(registry, ModBlocks.lost_cargo);
		registerItemBlock(registry, ModBlocks.skeleton_crate);
		registerItemBlock(registry, ModBlocks.lost_crate_potions);
		registerItemBlock(registry, ModBlocks.bush_crate);
		registerItemBlock(registry, ModBlocks.stone_circle);
		registerItemBlock(registry, ModBlocks.grave_marker);
		registerItemBlock(registry, ModBlocks.rose);
		registerItemBlock(registry, ModBlocks.tent);
		registerItemBlock(registry, ModBlocks.tent_abandoned);
		registerItemBlock(registry, ModBlocks.tent_abandoned_idol);
		
		// Register armillary with custom ItemBlock for tooltip
		ItemBlock armillaryItem = new ItemBlockArmillary(ModBlocks.armillary);
		armillaryItem.setRegistryName(ModBlocks.armillary.getRegistryName());
		ModBlocks.armillary.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(armillaryItem);
	}

	// Helper for registering ItemBlocks
	private static void registerItemBlock(IForgeRegistry<Item> registry, Block block) {
		ItemBlock itemBlock = new ItemBlock(block);
		itemBlock.setRegistryName(block.getRegistryName());
		block.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(itemBlock);
	}

	public static void registerItem(IForgeRegistry<Item> registry, String name, Item item) {
		registerItem(registry, name, item, false);
	}

	public static void registerItem(IForgeRegistry<Item> registry, String name, Item item, boolean setTabIcon) {
		item.setRegistryName(TracesOfTheFallen.MODID, name);
		item.setTranslationKey(item.getRegistryName().toString());
		registry.register(item);
	}

}
