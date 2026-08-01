package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.command.CommandGetHauntingProgress;
import com.windanesz.tracesofthefallen.command.CommandSetHauntingProgress;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.init.ModLootTables;
import com.windanesz.tracesofthefallen.init.ModWorldGen;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.totf.Tags;
import com.windanesz.tracesofthefallen.world.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new com.windanesz.tracesofthefallen.network.ModGuiHandler());
		proxy.preInit(event);
		ModBlocks.registerTileEntities();
		ModLootTables.register();
		HauntingCapability.register();
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
		
		for (net.minecraft.world.biome.Biome biome : net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES) {
			if (biome != null && !biome.getSpawnableList(net.minecraft.entity.EnumCreatureType.MONSTER).isEmpty()) {
				net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(com.windanesz.tracesofthefallen.entity.EntitySidhe.class, 5, 1, 1, net.minecraft.entity.EnumCreatureType.MONSTER, biome);
				net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(com.windanesz.tracesofthefallen.entity.EntityGnossic.class, 5, 1, 1, net.minecraft.entity.EnumCreatureType.MONSTER, biome);
				if (biome.getTempCategory() == net.minecraft.world.biome.Biome.TempCategory.COLD || biome.isSnowyBiome()) {
					net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(com.windanesz.tracesofthefallen.entity.EntityFrostling.class, 15, 1, 3, net.minecraft.entity.EnumCreatureType.MONSTER, biome);
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