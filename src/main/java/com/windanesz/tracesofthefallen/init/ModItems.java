package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.item.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSlab;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(TracesOfTheFallen.MODID)
@Mod.EventBusSubscriber
public class ModItems {

	private ModItems() {
	}

	public static final Item cleaner = placeholder();
	public static final Item grave_rose = placeholder();
	public static final Item rune_of_skimming = placeholder();
	public static final Item goblin_idol = placeholder();
	public static final Item idol_of_blades = placeholder();
	public static final Item ancestral_claw_necklace = placeholder();
	public static final Item pemmican = placeholder();
	public static final Item furred_trout = placeholder();
	public static final Item mysterious_fur = placeholder();
	public static final Item veiled_mask = placeholder();
	public static final Item chitin_helmet = placeholder();
	public static final Item chitin_chestplate = placeholder();
	public static final Item chitin_leggings = placeholder();
	public static final Item chitin_boots = placeholder();
	public static final Item silk_rope = placeholder();
	public static final Item silk_spindle = placeholder();
	public static final Item incense = placeholder();
	public static final Item spirited_away_tea_leaves = placeholder();
	public static final Item forbidden_ivory = placeholder();
	public static final Item arcane_skull_maw = placeholder();
	public static final Item arcane_skull_shield = placeholder();
	public static final Item arcane_skull_swift = placeholder();
	public static final Item tally_collector = placeholder();
	public static final Item fetid_dagger = placeholder();
	public static final Item crooked_bone = placeholder();
	public static final Item primitive_mace = placeholder();
	public static final Item brass_club = placeholder();
	public static final Item bone_rattle = placeholder();
	public static final Item chilled_gel = placeholder();
	public static final Item zap_charge = placeholder();
	public static final Item zap_charge_empty = placeholder();
	public static final Item living_silver_speck = placeholder();
	public static final Item living_silver_nugget = placeholder();

	public static final Item painting_in_the_woods = placeholder();
	public static final Item painting_portrait = placeholder();
	public static final Item painting_the_bloodcurling = placeholder();
	public static final Item painting_wheel = placeholder();
	public static final Item painting_wizardry = placeholder();
	public static final Item painting_subterfuge = placeholder();
	public static final Item painting_resting_mischief = placeholder();
	public static final Item stone_chest = placeholder();
	public static final Item floater_brown = placeholder();
	public static final Item floater_white = placeholder();
	public static final Item item_frame_wooden = placeholder();
	public static final Item bonepile = placeholder();
	public static final Item hay_bed = placeholder();
	public static final Item subtorch = placeholder();
	public static final Item goblin_nest = placeholder();
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
		registerItem(registry, "ward_of_blades", new ItemWardOfBlades().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "ancestral_claw_necklace", new ItemAncestralClawNecklace().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "pemmican", new ItemFood(10, 0.8f, true).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "furred_trout", new ItemFood(2, 0.1f, false).setCreativeTab(ModCreativeTab.TOTF_TAB));

		registerItem(registry, "painting_in_the_woods", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_IN_THE_WOODS).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_portrait", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_PORTRAIT).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_the_bloodcurling", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_THE_BLOODCURLING).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_wheel", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_WHEEL).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_wizardry", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_WIZARDRY).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_subterfuge", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_SUBTERFUGE).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "painting_resting_mischief", new ItemModPainting(ItemModPainting.EnumPainting.PAINTING_RESTING_MISCHIEF).setCreativeTab(ModCreativeTab.TOTF_TAB));

		registerItem(registry, "mysterious_fur", new ItemMysteriousFur().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "silk_rope", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "incense", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "spirited_away_tea_leaves", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "forbidden_ivory", new ItemArcaneRelic(ItemArcaneRelic.RelicEffect.FORBIDDEN_IVORY).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "arcane_skull_maw", new ItemArcaneRelic(ItemArcaneRelic.RelicEffect.MAW).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "arcane_skull_shield", new ItemArcaneRelic(ItemArcaneRelic.RelicEffect.SHIELD).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "arcane_skull_swift", new ItemArcaneRelic(ItemArcaneRelic.RelicEffect.SWIFT).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "silk_spindle", new ItemSilkSpindle().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "bundle_of_lost_letters", new ItemBundleOfLostLetters().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "veiled_mask", new ItemVeiledMask().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "chitin", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "chitin_helmet", new ChitinArmor(EntityEquipmentSlot.HEAD).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "chitin_chestplate", new ChitinArmor(EntityEquipmentSlot.CHEST).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "chitin_leggings", new ChitinArmor(EntityEquipmentSlot.LEGS).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "chitin_boots", new ChitinArmor(EntityEquipmentSlot.FEET).setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "wheel", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "wonder_fertilizer", new ItemWonderFertilizer().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "old_world_tinkers_kit", new ItemOldWorldTinkersKit().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "tally_collector", new ItemTallieCollector().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "cleaner", new ItemCleaner().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "fetid_dagger", new ItemFetidDagger().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "crooked_bone", new ItemCrookedBone().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "primitive_mace", new ItemPrimitiveMace().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "brass_club", new ItemBrassClub().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "bone_rattle", new ItemBoneRattle().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "goblin_nest", new ItemGoblinNest().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "chilled_gel", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "zap_charge", new ItemZapCharge().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "zap_charge_empty", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "living_silver_speck", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "living_silver_nugget", new Item().setCreativeTab(ModCreativeTab.TOTF_TAB));
		registerItem(registry, "floater_brown", new ItemFloater(ModBlocks.floater_brown, "floater_brown").setCreativeTab(ModCreativeTab.TOTF_TAB));
		if (Settings.miscSettings.enableWhiteFloater) {
			registerItem(registry, "floater_white", new ItemFloater(ModBlocks.floater_white, "floater_white").setCreativeTab(ModCreativeTab.TOTF_TAB));
		}

		// Register ItemBlocks
		registerItemBlock(registry, ModBlocks.lost_cargo);
		registerItemBlock(registry, ModBlocks.skeleton_crate);
		registerItemBlock(registry, ModBlocks.lost_crate_potions);
		registerItemBlock(registry, ModBlocks.bush_crate);
		registerItemBlock(registry, ModBlocks.stone_circle);
		registerItemBlock(registry, ModBlocks.stone_circle_clean);
		registerItemBlock(registry, ModBlocks.grave_marker);
		registerItemBlock(registry, ModBlocks.rose);
		registerItemBlock(registry, ModBlocks.tent);
		registerItemBlock(registry, ModBlocks.tent_abandoned);
		registerItemBlock(registry, ModBlocks.tent_abandoned_idol);
		registerItemBlock(registry, ModBlocks.salvaged_scaffold);
		
		// Register armillary with custom ItemBlock for tooltip
		ItemBlock armillaryItem = new ItemBlockArmillary(ModBlocks.armillary);
		armillaryItem.setRegistryName(ModBlocks.armillary.getRegistryName());
		ModBlocks.armillary.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(armillaryItem);

		// Register new decorative block ItemBlocks
		registerAncestralSifterItemBlock(registry, ModBlocks.ancestral_sifter_golden);
		registerAncestralSifterItemBlock(registry, ModBlocks.ancestral_sifter_red);
		ItemBlock balanceItem = new ItemBlockBalance(ModBlocks.balance);
		balanceItem.setRegistryName(ModBlocks.balance.getRegistryName());
		registry.register(balanceItem);
		ItemBlock censerItem = new ItemBlockCenser(ModBlocks.censer);
		censerItem.setRegistryName(ModBlocks.censer.getRegistryName());
		registry.register(censerItem);
		registerItemBlock(registry, ModBlocks.dioptra);
		registerItemBlock(registry, ModBlocks.telescope);
		registerGlassFloatItemBlock(registry, ModBlocks.glass_float_clear_blue);
		registerGlassFloatItemBlock(registry, ModBlocks.glass_float_wine_green);
		registerGlassFloatItemBlock(registry, ModBlocks.glass_float_longing_blue);
		registerGlassFloatItemBlock(registry, ModBlocks.glass_float_blood_red);
		registerItemBlock(registry, ModBlocks.glass_float_clear_blue_landed);
		registerItemBlock(registry, ModBlocks.glass_float_wine_green_landed);
		registerItemBlock(registry, ModBlocks.glass_float_longing_blue_landed);
		registerItemBlock(registry, ModBlocks.glass_float_blood_red_landed);
		registerItemBlock(registry, ModBlocks.incense_stick);
		registerItemBlock(registry, ModBlocks.incense_burner_gold);
		registerItemBlock(registry, ModBlocks.incense_burner_green);
		registerItemBlock(registry, ModBlocks.incense_burner_white);
		ItemBlock porcelainCupItem = new ItemBlockPorcelainPiece(ModBlocks.porcelain_cup);
		porcelainCupItem.setRegistryName(ModBlocks.porcelain_cup.getRegistryName());
		registry.register(porcelainCupItem);
		ItemBlock porcelainPotItem = new ItemBlockPorcelainPiece(ModBlocks.porcelain_pot);
		porcelainPotItem.setRegistryName(ModBlocks.porcelain_pot.getRegistryName());
		registry.register(porcelainPotItem);
		registerItemBlock(registry, ModBlocks.rope_mossy);
		registerItemBlock(registry, ModBlocks.rope_mossy_hook);
		registerItemBlock(registry, ModBlocks.desk_bell);

		ItemBlock idolVesselItem = new ItemBlockIdolVessel(ModBlocks.idol_vessel);
		idolVesselItem.setRegistryName(ModBlocks.idol_vessel.getRegistryName());
		registry.register(idolVesselItem);
		ItemBlock wroughtBombItem = new ItemBlockWroughtBomb(ModBlocks.wrought_bomb);
		wroughtBombItem.setRegistryName(ModBlocks.wrought_bomb.getRegistryName());
		ModBlocks.wrought_bomb.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(wroughtBombItem);
		registerItemBlock(registry, ModBlocks.stained_glass);
		registerItemBlock(registry, ModBlocks.stained_glass_pane);
		registerItemBlock(registry, ModBlocks.dreamcatcher);
		registerItemBlock(registry, ModBlocks.item_frame_wooden);
		ItemBlock bonepileItem = new ItemBlockBonepile(ModBlocks.bonepile);
		bonepileItem.setRegistryName(ModBlocks.bonepile.getRegistryName());
		ModBlocks.bonepile.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(bonepileItem);
		registerItemBlock(registry, ModBlocks.hay_bed);

		registerItemBlock(registry, ModBlocks.bricks_stone);
		registerItemBlock(registry, ModBlocks.bricks_stone_carved);
		registerItemBlock(registry, ModBlocks.bricks_stone_carved_dirty);
		registerItemBlock(registry, ModBlocks.bricks_stone_dirty);
		registerItemBlock(registry, ModBlocks.bricks_stone_mossy);
		registerItemBlock(registry, ModBlocks.bricks_stone_carved_mossy);
		registerItemBlock(registry, ModBlocks.bricks_stone_smooth);
		registerItemBlock(registry, ModBlocks.bricks_stone_pillar);

		// New slabs, stairs, walls
		registerSlabItemBlock(registry, ModBlocks.bricks_stone_slab, (BlockSlab)ModBlocks.bricks_stone_slab, (BlockSlab)ModBlocks.bricks_stone_double_slab);
		registerItemBlock(registry, ModBlocks.bricks_stone_stair);
		registerItemBlock(registry, ModBlocks.bricks_stone_wall);

		registerItemBlock(registry, ModBlocks.mozaic_pink);
		registerItemBlock(registry, ModBlocks.mozaic_pink_washed);
		registerItemBlock(registry, ModBlocks.mozaic_red);
		registerItemBlock(registry, ModBlocks.mozaic_red_washed);
		registerItemBlock(registry, ModBlocks.mozaic_teal);
		registerItemBlock(registry, ModBlocks.mozaic_teal_washed);
		registerItemBlock(registry, ModBlocks.mozaic_yellow);
		registerItemBlock(registry, ModBlocks.mozaic_yellow_washed);

		registerItemBlock(registry, ModBlocks.stone_chest);
		registerItemBlock(registry, ModBlocks.stone_compartment);
		registerItemBlock(registry, ModBlocks.stone_pressure_plate);
		registerItemBlock(registry, ModBlocks.stone_receiver);

		registerItemBlock(registry, ModBlocks.brass_gas_lamp);
		registerItemBlock(registry, ModBlocks.blue_caged_lamp);
		registerItemBlock(registry, ModBlocks.wrought_caged_lamp);
		registerItemBlock(registry, ModBlocks.brass_fabricator);
		registerItemBlock(registry, ModBlocks.wrought_bars);
		registerItemBlock(registry, ModBlocks.guillotine);
		registerItemBlock(registry, ModBlocks.dancoil);
		registerItemBlock(registry, ModBlocks.spinning_wheel);
		registerItemBlock(registry, ModBlocks.subtorch);
	}

	public static void registerOreDictionary() {
		OreDictionary.registerOre("cleaner", cleaner);
		OreDictionary.registerOre("soap", cleaner);
		OreDictionary.registerOre("nuggetSilver", living_silver_nugget);
	}

	// Helper for registering ItemBlocks
	private static void registerItemBlock(IForgeRegistry<Item> registry, Block block) {
		ItemBlock itemBlock = new ItemBlock(block);
		itemBlock.setRegistryName(block.getRegistryName());
		block.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(itemBlock);
	}

	private static void registerSlabItemBlock(IForgeRegistry<Item> registry, Block block, BlockSlab halfSlab, BlockSlab doubleSlab) {
		ItemSlab itemBlock = new ItemSlab(block, halfSlab, doubleSlab);
		itemBlock.setRegistryName(block.getRegistryName());
		block.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(itemBlock);
	}

	private static void registerAncestralSifterItemBlock(IForgeRegistry<Item> registry, Block block) {
		ItemBlock itemBlock = new ItemBlockAncestralSifter(block);
		itemBlock.setRegistryName(block.getRegistryName());
		block.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(itemBlock);
	}

	private static void registerGlassFloatItemBlock(IForgeRegistry<Item> registry, Block block) {
		ItemBlock itemBlock = new ItemBlockGlassFloat(block);
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

