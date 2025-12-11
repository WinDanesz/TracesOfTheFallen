package com.windanesz.tracesofthefallen.client;

import com.windanesz.tracesofthefallen.CommonProxy;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.TileEntityGraveMarker;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.client.renderer.*;
import com.windanesz.tracesofthefallen.entity.EntityFamiliarSpecter;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityModPainting;
import com.windanesz.tracesofthefallen.entity.EntitySpecter;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.packet.PacketPlayerSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		registerEntityRenderers();
		registerTileEntityRenderers();
	}

	@Override
	public void registerColorHandlers() {
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
			// Only apply for tintindex 0
			if (tintIndex == 0 && world != null && pos != null) {
				return BiomeColorHelper.getGrassColorAtPos(world, pos);
			}
			return 0xFFFFFF;
		}, ModBlocks.skeleton_crate, ModBlocks.bush_crate);
	}

	@SubscribeEvent
	public static void registerItemModels(ModelRegistryEvent event) {

		for (Item item : Item.REGISTRY) {
			if (item.getRegistryName().getNamespace().equals(TracesOfTheFallen.MODID)) {
				registerItemModel(item); // Standard item model
			}
		}
	}

	private void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecter.class, RenderSpecter::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFamiliarSpecter.class, RenderFamiliarSpecter::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityModPainting.class, RenderModPainting::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblin.class, RenderGoblin::new);
	}

	private void registerTileEntityRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGraveMarker.class, new TileEntityGraveMarkerRenderer());
	}

	/**
	 * Registers an item model, using the item's registry name as the model name (this convention makes it easier to
	 * keep track of everything). Variant defaults to "normal". Registers the model for all metadata values.
	 * Author: Electroblob
	 */
	private static void registerItemModel(Item item) {
		// Changing the last parameter from null to "inventory" fixed the item/block model weirdness. No idea why!
		ModelBakery.registerItemVariants(item, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		// Assigns the model for all metadata values
		ModelLoader.setCustomMeshDefinition(item, s -> new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	@Override
	public void handlePlayerSyncPacket(PacketPlayerSync.Message message) {
		HauntingCapability data = HauntingCapability.get(Minecraft.getMinecraft().player);

		if (data != null) {
			data.hauntingProgress = message.hauntedProgress;
		}
	}

	@Override
	public void renderFur() {
		Minecraft.getMinecraft().entityRenderer.displayItemActivation(new ItemStack(ModItems.mysterious_fur));
	}
}
