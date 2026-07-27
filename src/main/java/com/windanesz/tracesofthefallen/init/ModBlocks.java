package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(TracesOfTheFallen.MODID)
@Mod.EventBusSubscriber
public class ModBlocks {

	private ModBlocks() {
	}

	public static final Block lost_cargo = placeholder();
	public static final Block skeleton_crate = placeholder();
	public static final Block lost_crate_potions = placeholder();
	public static final Block bush_crate = placeholder();
	public static final Block loot_scene_dummy = placeholder();
	public static final Block stone_circle = placeholder();
	public static final Block rose = placeholder();
	public static final Block grave_rose = placeholder();
	public static final Block grave_marker = placeholder();
	public static final Block forest_painting = placeholder();
	public static final Block tent = placeholder();
	public static final Block tent_abandoned = placeholder();
	public static final Block tent_abandoned_idol = placeholder();

	public static final Block armillary = placeholder();
	public static final Block salvaged_scaffold = placeholder();

	// New decorative blocks
	public static final Block ancestral_sifter_golden = placeholder();
	public static final Block ancestral_sifter_red = placeholder();
	public static final Block balance = placeholder();
	public static final Block censer = placeholder();
	public static final Block dioptra = placeholder();
	public static final Block telescope = placeholder();
	public static final Block glass_float_clear_blue = placeholder();
	public static final Block glass_float_wine_green = placeholder();
	public static final Block glass_float_longing_blue = placeholder();
	public static final Block glass_float_blood_red = placeholder();
	public static final Block glass_float_clear_blue_landed = placeholder();
	public static final Block glass_float_wine_green_landed = placeholder();
	public static final Block glass_float_longing_blue_landed = placeholder();
	public static final Block glass_float_blood_red_landed = placeholder();
	public static final Block incense_stick = placeholder();
	public static final Block incense_burner_gold = placeholder();
	public static final Block incense_burner_green = placeholder();
	public static final Block incense_burner_white = placeholder();
	public static final Block porcelain_set = placeholder();
	public static final Block porcelain_cup = placeholder();
	public static final Block porcelain_pot = placeholder();
	public static final Block rope_mossy = placeholder();
	public static final Block rope_mossy_hook = placeholder();
	public static final Block desk_bell = placeholder();
	public static final Block idol_vessel = placeholder();
	public static final Block technical_block = placeholder();
	public static final Block wrought_bomb = placeholder();
	public static final Block bricks_stone = placeholder();
	public static final Block bricks_stone_carved = placeholder();
	public static final Block bricks_stone_carved_dirty = placeholder();
	public static final Block bricks_stone_dirty = placeholder();
	public static final Block bricks_stone_mossy = placeholder();
	public static final Block bricks_stone_carved_mossy = placeholder();
	public static final Block bricks_stone_smooth = placeholder();
	public static final Block bricks_stone_pillar = placeholder();
	public static final Block stained_glass = placeholder();
	public static final Block dreamcatcher = placeholder();
	public static final Block stone_chest = placeholder();
	public static final Block stone_chest_open = placeholder();
	public static final Block item_frame_wooden = placeholder();
	public static final Block bonepile = placeholder();
	public static final Block hay_bed = placeholder();
	public static final Block mozaic_pink = placeholder();
	public static final Block mozaic_pink_washed = placeholder();
	public static final Block mozaic_red = placeholder();
	public static final Block mozaic_red_washed = placeholder();
	public static final Block mozaic_teal = placeholder();
	public static final Block mozaic_teal_washed = placeholder();
	public static final Block mozaic_yellow = placeholder();
	public static final Block mozaic_yellow_washed = placeholder();
	public static final Block bricks_stone_slab = placeholder();
	public static final Block bricks_stone_double_slab = placeholder();
	public static final Block bricks_stone_stair = placeholder();
	public static final Block bricks_stone_wall = placeholder();

	public static final Block blue_caged_lamp = placeholder();
	public static final Block brass_gas_lamp = placeholder();
	public static final Block wrought_caged_lamp = placeholder();
	public static final Block brass_fabricator = placeholder();

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder() {
		return null;
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		registerBlock(registry, "lost_cargo", new BlockTOFT(Material.WOOD).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/lost_cargo")));
		registerBlock(registry, "lost_crate_potions", new BlockTOFT(Material.WOOD).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/lost_crate_potions")));
		registerBlock(registry, "skeleton_crate", new BlockRemains(Material.WOOD).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/grave")));
		registerBlock(registry, "bush_crate", new BlockTOFT(Material.WOOD).setSpawnGoblins(true).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/bush_crate")));
		//registerBlock(registry, "loot_scene_dummy", new BlockLootSceneDummy(Material.IRON));
		registerBlock(registry, "stone_circle", new BlockStoneCircle(Material.ROCK).setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 0.1, 1)));
		registerBlock(registry, "grave_marker", new BlockGraveMarker(Material.ROCK).setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 0.4, 1)).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/grave")));
		registerBlock(registry, "rose", new BlockRose());
		registerBlock(registry, "grave_rose", new BlockRose());
		registerBlock(registry, "tent", new BlockTent(true).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent")));
		registerBlock(registry, "tent_abandoned", new BlockTent(false).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent_abandoned")));
		registerBlock(registry, "tent_abandoned_idol", new BlockTent(false).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/tent_with_idol")));
		registerBlock(registry, "armillary", new BlockArmillary());
		registerBlock(registry, "salvaged_scaffold", new BlockSalvagedScaffold());

		// New decorative blocks
		registerBlock(registry, "ancestral_sifter_golden", new BlockAncestralSifter(Material.WOOD, BlockAncestralSifter.SifterVariant.GOLDEN).setBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D)));
		registerBlock(registry, "ancestral_sifter_red", new BlockAncestralSifter(Material.WOOD, BlockAncestralSifter.SifterVariant.RED).setBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D)));
		registerBlock(registry, "balance", new BlockBalance(Material.IRON).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 1.1D, 0.9D)));
		registerBlock(registry, "censer", new BlockCenser(Material.IRON).setBoundingBox(new AxisAlignedBB(0.2D, 0.0D, 0.2D, 0.8D, 1.0D, 0.8D)));
		registerBlock(registry, "dioptra", new BlockDioptra(Material.WOOD).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.9D, 0.9D)));
		registerBlock(registry, "telescope", new BlockTelescope(Material.WOOD).setBoundingBox(new AxisAlignedBB(0.225D, 0.0D, 0.225D, 0.775D, 1.9D, 0.775D)));
		registerBlock(registry, "glass_float_clear_blue", new BlockGlassFloat(Material.GLASS, BlockGlassFloat.FloatVariant.CLEAR_BLUE).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.75D, 0.9D)));
		registerBlock(registry, "glass_float_wine_green", new BlockGlassFloat(Material.GLASS, BlockGlassFloat.FloatVariant.WINE_GREEN).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.75D, 0.9D)));
		registerBlock(registry, "glass_float_longing_blue", new BlockGlassFloat(Material.GLASS, BlockGlassFloat.FloatVariant.LONGING_BLUE).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.75D, 0.9D)));
		registerBlock(registry, "glass_float_blood_red", new BlockGlassFloat(Material.GLASS, BlockGlassFloat.FloatVariant.BLOOD_RED).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.75D, 0.9D)));
		registerBlock(registry, "glass_float_clear_blue_landed", new BlockGlassFloatLanded(Material.GLASS, BlockGlassFloat.FloatVariant.CLEAR_BLUE).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.125D, 0.9D)));
		registerBlock(registry, "glass_float_wine_green_landed", new BlockGlassFloatLanded(Material.GLASS, BlockGlassFloat.FloatVariant.WINE_GREEN).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.125D, 0.9D)));
		registerBlock(registry, "glass_float_longing_blue_landed", new BlockGlassFloatLanded(Material.GLASS, BlockGlassFloat.FloatVariant.LONGING_BLUE).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.125D, 0.9D)));
		registerBlock(registry, "glass_float_blood_red_landed", new BlockGlassFloatLanded(Material.GLASS, BlockGlassFloat.FloatVariant.BLOOD_RED).setBoundingBox(new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.125D, 0.9D)));
		registerBlock(registry, "incense_stick", new BlockIncense(Material.WOOD).setBoundingBox(new AxisAlignedBB(0.375D, 0.0D, 0.25D, 0.625D, 0.1D, 0.75D)));
		registerBlock(registry, "incense_burner_gold", new BlockIncenseBurner(Material.IRON, new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 1.25D, 0.9D)));
		registerBlock(registry, "incense_burner_green", new BlockIncenseBurner(Material.IRON, new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D)));
		registerBlock(registry, "incense_burner_white", new BlockIncenseBurner(Material.IRON, new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D)));
		registerBlock(registry, "porcelain_set", new BlockPorcelainSet(Material.ROCK));
		registerBlock(registry, "porcelain_cup", new BlockPorcelainPiece(Material.ROCK, BlockPorcelainPiece.PieceType.CUP, new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.6875D, 0.25D, 0.6875D)));
		registerBlock(registry, "porcelain_pot", new BlockPorcelainPiece(Material.ROCK, BlockPorcelainPiece.PieceType.POT, new AxisAlignedBB(0.375D, 0.0D, 0.1875D, 0.75D, 0.4375D, 0.8125D)));
		registerBlock(registry, "rope_mossy", new BlockDecoration(Material.WOOD).setBoundingBox(new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D)));
		registerBlock(registry, "rope_mossy_hook", new BlockDecoration(Material.WOOD).setBoundingBox(new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D)));
		registerBlock(registry, "desk_bell", new BlockDeskBell(Material.IRON).setBoundingBox(new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.35625D, 0.6875D)));
		registerBlock(registry, "idol_vessel", new BlockIdolVessel(Material.ROCK).setBoundingBox(new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D)));
		registerBlockNoTab(registry, "technical_block", new BlockTechnicalBlock());
		registerBlock(registry, "wrought_bomb", new BlockWroughtBomb());
		registerBlock(registry, "bricks_stone", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_carved", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_carved_dirty", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_dirty", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_mossy", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_carved_mossy", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_smooth", new BlockTOTFStone());
		registerBlock(registry, "bricks_stone_pillar", new BlockTOTFPillar(Material.ROCK));
		registerBlock(registry, "stained_glass", new BlockTOTFGlass());
		registerBlock(registry, "dreamcatcher", new BlockDreamcatcher(Material.WOOD));
		registerBlock(registry, "stone_chest", new BlockStoneChest());
		registerBlock(registry, "stone_chest_open", new BlockStoneChest().setBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 0.5625D, 0.75D)));
		registerBlock(registry, "item_frame_wooden", new BlockWoodenItemFrame());
		registerBlock(registry, "bonepile", new BlockBonepile());
		registerBlock(registry, "hay_bed", new BlockHayBed(Material.WOOD).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "blocks/hay_bed")));
		registerBlock(registry, "mozaic_pink", new BlockTOTFStone());
		registerBlock(registry, "mozaic_pink_washed", new BlockTOTFStone());
		registerBlock(registry, "mozaic_red", new BlockTOTFStone());
		registerBlock(registry, "mozaic_red_washed", new BlockTOTFStone());
		registerBlock(registry, "mozaic_teal", new BlockTOTFStone());
		registerBlock(registry, "mozaic_teal_washed", new BlockTOTFStone());
		registerBlock(registry, "mozaic_yellow", new BlockTOTFStone());
		registerBlock(registry, "mozaic_yellow_washed", new BlockTOTFStone());

		Block customBricksStoneSlab = new BlockTOTFHalfSlab(Material.ROCK).setHardness(2.0F).setResistance(10.0F);
		registerBlock(registry, "bricks_stone_slab", customBricksStoneSlab);
		registerBlockNoTab(registry, "bricks_stone_double_slab", new BlockTOTFDoubleSlab(Material.ROCK, customBricksStoneSlab).setHardness(2.0F).setResistance(10.0F));
		
		registerBlock(registry, "bricks_stone_stair", new BlockTOTFStairs(Blocks.STONEBRICK.getDefaultState()).setHardness(2.0F).setResistance(10.0F));
		
		registerBlock(registry, "bricks_stone_wall", new BlockTOTFWall(Blocks.STONEBRICK).setHardness(2.0F).setResistance(10.0F));

		registerBlock(registry, "blue_caged_lamp", new BlockCagedLamp());
		registerBlock(registry, "brass_gas_lamp", new BlockBrassGasLamp());
		registerBlock(registry, "wrought_caged_lamp", new BlockWroughtCagedLamp());
		registerBlock(registry, "brass_fabricator", new BlockBrassFabricator());
	}

	public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block) {
		block.setRegistryName(TracesOfTheFallen.MODID, name);
		block.setTranslationKey(block.getRegistryName().toString());
		block.setCreativeTab(ModCreativeTab.TOTF_TAB);
		registry.register(block);
	}

	public static void registerBlockNoTab(IForgeRegistry<Block> registry, String name, Block block) {
		block.setRegistryName(TracesOfTheFallen.MODID, name);
		block.setTranslationKey(block.getRegistryName().toString());
		registry.register(block);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getWorld().isRemote) {
			return;
		}

		if (event.getHand() != EnumHand.MAIN_HAND) {
			return;
		}

		ItemStack heldItem = event.getItemStack();
		if (!heldItem.isEmpty() && heldItem.getItem() == Items.SHEARS) {
			net.minecraft.block.state.IBlockState state = event.getWorld().getBlockState(event.getPos());
			if (state.getBlock() == Blocks.DOUBLE_PLANT && state.getValue(BlockDoublePlant.VARIANT) == BlockDoublePlant.EnumPlantType.ROSE) {
				// Drop two red flowers
				Block.spawnAsEntity(event.getWorld(), event.getPos(), new ItemStack(ModBlocks.rose, 2));
				heldItem.damageItem(2, event.getEntityPlayer());
				event.getWorld().setBlockState(event.getPos(), Blocks.AIR.getDefaultState(), 3);
				event.setCanceled(true);
			}
		}

	}

	public static void registerTileEntities() {
		// Nope, these still don't have their own registry...
		GameRegistry.registerTileEntity(TileEntityLostLoot.class, new ResourceLocation(TracesOfTheFallen.MODID, "lost_loot"));
		GameRegistry.registerTileEntity(TileEntityGraveMarker.class, new ResourceLocation(TracesOfTheFallen.MODID, "grave_marker"));
		GameRegistry.registerTileEntity(TileEntityDioptra.class, new ResourceLocation(TracesOfTheFallen.MODID, "dioptra"));
		GameRegistry.registerTileEntity(TileEntityTelescope.class, new ResourceLocation(TracesOfTheFallen.MODID, "telescope"));
		GameRegistry.registerTileEntity(TileEntityBalance.class, new ResourceLocation(TracesOfTheFallen.MODID, "balance"));
		GameRegistry.registerTileEntity(TileEntityRemains.class, new ResourceLocation(TracesOfTheFallen.MODID, "remains"));
		GameRegistry.registerTileEntity(TileEntityStoneCircle.class, new ResourceLocation(TracesOfTheFallen.MODID, "stone_circle"));
		GameRegistry.registerTileEntity(TileEntityTent.class, new ResourceLocation(TracesOfTheFallen.MODID, "tent"));
		GameRegistry.registerTileEntity(TileEntityIncense.class, new ResourceLocation(TracesOfTheFallen.MODID, "incense"));
		GameRegistry.registerTileEntity(TileEntityCenser.class, new ResourceLocation(TracesOfTheFallen.MODID, "censer"));
		GameRegistry.registerTileEntity(TileEntityIncenseBurner.class, new ResourceLocation(TracesOfTheFallen.MODID, "incense_burner"));
		GameRegistry.registerTileEntity(TileEntityPorcelainVessel.class, new ResourceLocation(TracesOfTheFallen.MODID, "porcelain_vessel"));
		GameRegistry.registerTileEntity(TileEntityPorcelainSet.class, new ResourceLocation(TracesOfTheFallen.MODID, "porcelain_set"));
		GameRegistry.registerTileEntity(TileEntityIdolVessel.class, new ResourceLocation(TracesOfTheFallen.MODID, "idol_vessel"));
		GameRegistry.registerTileEntity(TileEntityStoneChest.class, new ResourceLocation(TracesOfTheFallen.MODID, "stone_chest"));
		GameRegistry.registerTileEntity(TileEntityDreamcatcher.class, new ResourceLocation(TracesOfTheFallen.MODID, "dreamcatcher"));
		GameRegistry.registerTileEntity(TileEntityWoodenItemFrame.class, new ResourceLocation(TracesOfTheFallen.MODID, "item_frame_wooden"));
		GameRegistry.registerTileEntity(TileEntityBrassGasLamp.class, new ResourceLocation(TracesOfTheFallen.MODID, "brass_gas_lamp"));
		GameRegistry.registerTileEntity(TileEntityWroughtCagedLamp.class, new ResourceLocation(TracesOfTheFallen.MODID, "wrought_caged_lamp"));
		GameRegistry.registerTileEntity(TileEntityBrassFabricator.class, new ResourceLocation(TracesOfTheFallen.MODID, "brass_fabricator"));
	}
}

