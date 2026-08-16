package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.command.CommandGetHauntingProgress;
import com.windanesz.tracesofthefallen.command.CommandSetHauntingProgress;
import com.windanesz.tracesofthefallen.entity.EntityFrostling;
import com.windanesz.tracesofthefallen.entity.EntityGnossic;
import com.windanesz.tracesofthefallen.entity.EntitySidhe;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.init.ModLootTables;
import com.windanesz.tracesofthefallen.init.ModWorldGen;
import com.windanesz.tracesofthefallen.network.ModGuiHandler;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.totf.Tags;
import com.windanesz.tracesofthefallen.world.*;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class TracesOfTheFallen implements ForgeChunkManager.LoadingCallback {

	/**
	 * title-cased modname
	 */
	public static final String MODNAME = "TracesOfTheFallen";
	public static final String MODID = Tags.MOD_ID;
	public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
	public static Settings settings = new Settings();

	@Mod.Instance(Tags.MOD_ID)
	public static TracesOfTheFallen instance;

	@SidedProxy(clientSide = "com.windanesz.tracesofthefallen.client.ClientProxy", serverSide = "com.windanesz.tracesofthefallen.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, this);
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new ModGuiHandler());
		proxy.preInit(event);
		ModBlocks.registerTileEntities();
		ModLootTables.register();
		HauntingCapability.register();
	}


	private boolean isValidBiome(ResourceLocation biomeName, List<ResourceLocation> whitelist, List<ResourceLocation> blacklist) {
		if (biomeName == null) return false;
		if (blacklist.contains(biomeName)) return false;
		if (!whitelist.isEmpty() && !whitelist.contains(biomeName)) return false;
		return true;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		ModWorldGen.registerWorldGenerators();

		GameRegistry.registerWorldGenerator(new WorldGenStoneCircle(), 0);
		GameRegistry.registerWorldGenerator(new WorldGenLostCargo(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenRemains(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenPotionCrate(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenTent(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenAbandonedTent(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenAbandonedTentWithTotem(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenBushCrate(), 1);

		proxy.registerColorHandlers();
		PacketHandler.initPackets();
		ModItems.registerOreDictionary();
		
		List<ResourceLocation> sidheWhite = Arrays.asList(Settings.toResourceLocations(Settings.mobSettings.sidheBiomeWhitelist));
		List<ResourceLocation> sidheBlack = Arrays.asList(Settings.toResourceLocations(Settings.mobSettings.sidheBiomeBlacklist));
		List<ResourceLocation> gnossicWhite = Arrays.asList(Settings.toResourceLocations(Settings.mobSettings.gnossicBiomeWhitelist));
		List<ResourceLocation> gnossicBlack = Arrays.asList(Settings.toResourceLocations(Settings.mobSettings.gnossicBiomeBlacklist));
		List<ResourceLocation> frostlingWhite = Arrays.asList(Settings.toResourceLocations(Settings.mobSettings.frostlingBiomeWhitelist));
		List<ResourceLocation> frostlingBlack = Arrays.asList(Settings.toResourceLocations(Settings.mobSettings.frostlingBiomeBlacklist));

		for (Biome biome : ForgeRegistries.BIOMES) {
			if (biome != null && !biome.getSpawnableList(EnumCreatureType.MONSTER).isEmpty()) {
				ResourceLocation biomeName = biome.getRegistryName();
				
				if (Settings.mobSettings.sidheSpawnWeight > 0 && isValidBiome(biomeName, sidheWhite, sidheBlack)) {
					EntityRegistry.addSpawn(EntitySidhe.class, Settings.mobSettings.sidheSpawnWeight, Settings.mobSettings.sidheSpawnMinGroup, Settings.mobSettings.sidheSpawnMaxGroup, EnumCreatureType.MONSTER, biome);
				}
				if (Settings.mobSettings.gnossicSpawnWeight > 0 && isValidBiome(biomeName, gnossicWhite, gnossicBlack)) {
					EntityRegistry.addSpawn(EntityGnossic.class, Settings.mobSettings.gnossicSpawnWeight, Settings.mobSettings.gnossicSpawnMinGroup, Settings.mobSettings.gnossicSpawnMaxGroup, EnumCreatureType.MONSTER, biome);
				}
				if (Settings.mobSettings.frostlingSpawnWeight > 0 && (biome.getTempCategory() == Biome.TempCategory.COLD || biome.isSnowyBiome()) && isValidBiome(biomeName, frostlingWhite, frostlingBlack)) {
					EntityRegistry.addSpawn(EntityFrostling.class, Settings.mobSettings.frostlingSpawnWeight, Settings.mobSettings.frostlingSpawnMinGroup, Settings.mobSettings.frostlingSpawnMaxGroup, EnumCreatureType.MONSTER, biome);
				}
			}
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandSetHauntingProgress());
		event.registerServerCommand(new CommandGetHauntingProgress());
	}

	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
		for (ForgeChunkManager.Ticket ticket : tickets) {
			ForgeChunkManager.releaseTicket(ticket);
		}
	}
}