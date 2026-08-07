package com.windanesz.tracesofthefallen.client;

import com.windanesz.tracesofthefallen.CommonProxy;
import com.windanesz.tracesofthefallen.PorcelainLiquids;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.*;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.client.model.*;
import com.windanesz.tracesofthefallen.client.particle.*;
import com.windanesz.tracesofthefallen.client.render.entity.RenderGnossic;
import com.windanesz.tracesofthefallen.client.renderer.*;
import com.windanesz.tracesofthefallen.entity.*;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.item.ItemBlockGlassFloat;
import com.windanesz.tracesofthefallen.packet.PacketPlayerSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	/** The wrap width for standard multi-line descriptions. */
	private static final int TOOLTIP_WRAP_WIDTH = 140;
	private static final ResourceLocation INCENSE_LINE_SPRITE = new ResourceLocation(TracesOfTheFallen.MODID, "particle/incense_line");
	private static final ResourceLocation PUDDLE_BLOOD_SPRITE = new ResourceLocation(TracesOfTheFallen.MODID, "particle/puddle_blood");
	private static final ResourceLocation FROST_SPRITE = new ResourceLocation(TracesOfTheFallen.MODID, "particle/frost");
	private static final ResourceLocation WIND_SPRITE = new ResourceLocation(TracesOfTheFallen.MODID, "particle/wind");
	private static final ResourceLocation CRUMBS_SPRITE = new ResourceLocation(TracesOfTheFallen.MODID, "particle/crumbs");
	private static TextureAtlasSprite incenseLineSprite;
	private static TextureAtlasSprite puddleBloodSprite;
	private static TextureAtlasSprite frostSprite;
	private static TextureAtlasSprite windSprite;
	private static TextureAtlasSprite crumbsSprite;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		net.minecraftforge.client.model.obj.OBJLoader.INSTANCE.addDomain(TracesOfTheFallen.MODID);
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

		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
			if (tintIndex != 1 || world == null || pos == null) {
				return 0xFFFFFF;
			}

			if (world.getTileEntity(pos) instanceof TileEntityPorcelainVessel) {
				TileEntityPorcelainVessel vessel = (TileEntityPorcelainVessel) world.getTileEntity(pos);
				return PorcelainLiquids.getColor(vessel.getCupContent());
			}
			return 0xFFFFFF;
		}, ModBlocks.porcelain_cup);

		Item porcelainCupItem = Item.getItemFromBlock(ModBlocks.porcelain_cup);
		if (porcelainCupItem != null) {
			Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
				if (tintIndex != 1) {
					return 0xFFFFFF;
				}
				return PorcelainLiquids.getColor(TileEntityPorcelainVessel.getStoredCupContentType(stack));
			}, porcelainCupItem);
		}
	}

	@SubscribeEvent
	public static void registerItemModels(ModelRegistryEvent event) {

		for (Item item : Item.REGISTRY) {
			if (item.getRegistryName().getNamespace().equals(TracesOfTheFallen.MODID)) {
				registerItemModel(item); // Standard item model
			}
		}
	}

	@SubscribeEvent
	public static void registerSprites(TextureStitchEvent.Pre event) {
		incenseLineSprite = event.getMap().registerSprite(INCENSE_LINE_SPRITE);
		puddleBloodSprite = event.getMap().registerSprite(PUDDLE_BLOOD_SPRITE);
		frostSprite = event.getMap().registerSprite(FROST_SPRITE);
		windSprite = event.getMap().registerSprite(WIND_SPRITE);
		crumbsSprite = event.getMap().registerSprite(CRUMBS_SPRITE);
	}

	public static TextureAtlasSprite getIncenseLineSprite() {
		return incenseLineSprite;
	}

	public static TextureAtlasSprite getPuddleBloodSprite() {
		return puddleBloodSprite;
	}

	public static TextureAtlasSprite getFrostSprite() {
		return frostSprite != null ? frostSprite : windSprite;
	}

	public static TextureAtlasSprite getCrumbsSprite() {
		return crumbsSprite;
	}

	private void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityAncestralSifter.class, RenderAncestralSifter::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDioptraSeat.class, RenderDioptraSeat::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTelescopeSeat.class, RenderTelescopeSeat::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecter.class, RenderSpecter::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFamiliarSpecter.class, RenderFamiliarSpecter::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGlassFloat.class, RenderGlassFloat::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityModPainting.class, RenderModPainting::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblin.class, m -> new RenderGoblinVariant(m, new ModelGoblinBrood(), "goblin_brood"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinStarved.class, m -> new RenderGoblinVariant(m, new ModelGoblinStarved(), "goblin_starved"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinWayfarer.class, m -> new RenderGoblinVariant(m, new ModelGoblinWayfarer(), "goblin_wayfarer"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinEngineer.class, m -> new RenderGoblinVariant(m, new ModelGoblinEngineer(), "goblin_engineer"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinSapper.class, m -> new RenderGoblinVariant(m, new ModelGoblinEngineerSapper(), "goblin_sapper"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinTunneler.class, m -> new RenderGoblinVariant(m, new ModelGoblinEngineerSapper(), "goblin_sapper"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinTrapper.class, m -> new RenderGoblinVariant(m, new ModelGoblinTrapper(), "goblin_trapper"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinShaman.class, RenderGoblinShaman::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinSkeleton.class, RenderGoblinSkeleton::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTinybones.class, RenderTinybones::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinBrute.class, m -> new RenderGoblinVariant(m, new ModelGoblinBrute(), "goblin_brute"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinWarrior.class, m -> new RenderGoblinVariant(m, new ModelGoblinWarrior(), "goblin_warrior"));
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecrawler.class, RenderMinecrawler::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWroughtBomb.class, RenderWroughtBomb::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFetidDagger.class, RenderFetidDagger::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySidhe.class, com.windanesz.tracesofthefallen.client.render.entity.RenderSidhe::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFireOrb.class, RenderFireOrb::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityWillOWisp.class, RenderWillOWisp::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySeekingOrb.class, RenderSeekingOrb::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityVoidSlash.class, RenderVoidSlash::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMagmaBlast.class, RenderMagmaBlast::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMagmaPool.class, RenderMagmaPool::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityJawTrap.class, RenderJawTrap::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBloodTotem.class, RenderBloodTotem::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinNest.class, RenderGoblinNest::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLamphead.class, RenderLamphead::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFrostling.class, RenderFrostling::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityFrostlingMask.class, RenderFrostlingMask::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityZapLightning.class, RenderZapLightning::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityGnossic.class, RenderGnossic::new);
	}

	private void registerTileEntityRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBalance.class, new TileEntityBalanceRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDioptra.class, new TileEntityDioptraRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTelescope.class, new TileEntityTelescopeRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGraveMarker.class, new TileEntityGraveMarkerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPorcelainVessel.class, new TileEntityPorcelainVesselRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPorcelainSet.class, new TileEntityPorcelainSetRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodenItemFrame.class, new TileEntityWoodenItemFrameRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGuillotine.class, new TileEntityGuillotineRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpinningWheel.class, new TileEntitySpinningWheelRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArmillary.class, new TileEntityArmillaryRenderer());
	}

	/**
	 * Registers an item model, using the item's registry name as the model name (this convention makes it easier to
	 * keep track of everything). Variant defaults to "normal". Registers the model for all metadata values.
	 * Author: Electroblob
	 */
	private static void registerItemModel(Item item) {
		ModelResourceLocation inventoryModel = new ModelResourceLocation(item.getRegistryName(), "inventory");
		if (item == Item.getItemFromBlock(ModBlocks.dioptra)) {
			ModelBakery.registerItemVariants(item,
					inventoryModel,
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":dioptra_moving", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":dioptra_vertical_rotation", "inventory"));
		} else if (item == Item.getItemFromBlock(ModBlocks.telescope)) {
			ModelBakery.registerItemVariants(item,
					inventoryModel,
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":telescope_horizontal_rotation", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":telescope_vertical_rotation", "inventory"));
		} else if (item instanceof ItemBlockGlassFloat) {
			ModelBakery.registerItemVariants(item, inventoryModel, getGlassFloatWaterTopModel(item),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":glass_float_water_bottom", "inventory"));
		} else if (item == Item.getItemFromBlock(ModBlocks.balance)) {
			ModelBakery.registerItemVariants(item,
					inventoryModel,
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_base", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_beam", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_left_pan", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_right_pan", "inventory"));
		} else if (item == Item.getItemFromBlock(ModBlocks.guillotine)) {
			ModelBakery.registerItemVariants(item,
					inventoryModel,
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":guillotine_pole", "normal"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":guillotine_blade", "normal"));
		} else if (item == Item.getItemFromBlock(ModBlocks.spinning_wheel)) {
			ModelBakery.registerItemVariants(item, new ModelResourceLocation(item.getRegistryName(), "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":spinning_wheel_small_wheel", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":spinning_wheel_large_wheel", "inventory"));
		} else if (item == Item.getItemFromBlock(ModBlocks.armillary)) {
			ModelBakery.registerItemVariants(item, new ModelResourceLocation(item.getRegistryName(), "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":armillary_inner", "inventory"),
					new ModelResourceLocation(TracesOfTheFallen.MODID + ":armillary_outer", "inventory"));
		} else if (item == Item.getItemFromBlock(ModBlocks.ancestral_sifter_golden)) {
			ModelBakery.registerItemVariants(item, getSifterModels(inventoryModel, "ancestral_sifter_golden"));
		} else if (item == Item.getItemFromBlock(ModBlocks.ancestral_sifter_red)) {
			ModelBakery.registerItemVariants(item, getSifterModels(inventoryModel, "ancestral_sifter_red"));
		} else {
			ModelBakery.registerItemVariants(item, inventoryModel);
		}
		// Assigns the model for all metadata values
		ModelLoader.setCustomMeshDefinition(item, s -> inventoryModel);
	}

	private static ModelResourceLocation[] getSifterModels(ModelResourceLocation inventoryModel, String variantName) {
		String[] parts = {
				"body",
				"left_feather_1",
				"left_feather_2",
				"left_feather_3",
				"left_feather_4",
				"right_feather_1",
				"right_feather_2",
				"right_feather_3",
				"right_feather_4"
		};
		ModelResourceLocation[] models = new ModelResourceLocation[1 + (parts.length * 3)];
		models[0] = inventoryModel;
		for (int i = 0; i < parts.length; i++) {
			models[i + 1] = new ModelResourceLocation(TracesOfTheFallen.MODID + ":" + variantName + "_" + parts[i], "inventory");
			models[i + 1 + parts.length] = new ModelResourceLocation(TracesOfTheFallen.MODID + ":" + variantName + "_weathered_" + parts[i], "inventory");
			models[i + 1 + (parts.length * 2)] = new ModelResourceLocation(TracesOfTheFallen.MODID + ":" + variantName + "_broken_" + parts[i], "inventory");
		}
		return models;
	}

	private static ModelResourceLocation getGlassFloatWaterTopModel(Item item) {
		String path = item.getRegistryName().getPath();
		String waterModel = "glass_float_clear_blue".equals(path) ? "glass_float_water_top" : path + "_water_top";
		return new ModelResourceLocation(TracesOfTheFallen.MODID + ":" + waterModel, "inventory");
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
		renderItemActivation(new ItemStack(ModItems.mysterious_fur));
	}

	@Override
	public void renderItemActivation(ItemStack stack) {
		Minecraft.getMinecraft().entityRenderer.displayItemActivation(stack);
	}

	@Override
	public void spawnSifterCloudParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSifterCloud(world, x, y, z, motionX, motionY, motionZ));
	}

	@Override
	public void spawnIncenseSmokeParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleIncenseSmoke(world, x, y, z, motionX, motionY, motionZ, color));
	}

	@Override
	public void spawnIncenseFloorMistParticle(World world, double x, double y, double z, double motionX, double motionY,
			double motionZ, int color) {
		Minecraft.getMinecraft().effectRenderer
				.addEffect(new ParticleFloorMist(Minecraft.getMinecraft().getTextureManager(), world, x, y, z, motionX, motionY, motionZ, color));
	}

	@Override
	public void spawnBloodDropParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBloodDrop(world, x, y, z, motionX, motionY, motionZ));
	}

	@Override
	public void spawnBloodPuddleParticle(World world, double x, double y, double z) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBloodPuddle(world, x, y, z));
	}

	@Override
	public void spawnChillParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleChill(world, x, y, z, motionX, motionY, motionZ));
	}

	@Override
	public void spawnCyanCinderParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCyanCinder(world, x, y, z, motionX, motionY, motionZ));
	}

	@Override
	public void spawnLifestealParticle(World world, double x, double y, double z, int targetEntityId) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleLifestealStream(world, x, y, z, targetEntityId));
	}

	@Override
	public void spawnCrumbsParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCrumbs(world, x, y, z, motionX, motionY, motionZ, 0.06F));
	}

	@Override
	public void spawnHeavyCrumbsParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCrumbs(world, x, y, z, motionX, motionY, motionZ, 1.0F));
	}

	@Override
	public void spawnDigHoleParticle(World world, double x, double y, double z) {
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDigHole(world, x, y, z));
	}

	/**
	 * Helper method to add multi-line wrapped tooltip descriptions.
	 */
	public static void addMultiLineDescription(List<String> tooltip, String text) {
		tooltip.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, TOOLTIP_WRAP_WIDTH));
	}
}
