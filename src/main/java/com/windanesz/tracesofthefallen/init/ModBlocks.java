package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.*;
import com.windanesz.tracesofthefallen.block.TileEntityGraveMarker;
import com.windanesz.tracesofthefallen.block.TileEntityLostLoot;
import com.windanesz.tracesofthefallen.block.TileEntityTent;
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
	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder() {
		return null;
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		registerBlock(registry, "lost_cargo", new BlockTOFT(Material.WOOD).setLootTable(new ResourceLocation(TracesOfTheFallen.MODID, "chests/lost_cargo")));
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

	}

	public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block) {
		block.setRegistryName(TracesOfTheFallen.MODID, name);
		block.setTranslationKey(block.getRegistryName().toString());
		block.setCreativeTab(ModCreativeTab.TOTF_TAB);
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
		GameRegistry.registerTileEntity(TileEntityRemains.class, new ResourceLocation(TracesOfTheFallen.MODID, "remains"));
		GameRegistry.registerTileEntity(TileEntityStoneCircle.class, new ResourceLocation(TracesOfTheFallen.MODID, "stone_circle"));
		GameRegistry.registerTileEntity(TileEntityTent.class, new ResourceLocation(TracesOfTheFallen.MODID, "tent"));
	}
}
