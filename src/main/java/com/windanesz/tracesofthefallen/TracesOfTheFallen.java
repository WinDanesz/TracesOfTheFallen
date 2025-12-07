package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.command.CommandGetHauntingProgress;
import com.windanesz.tracesofthefallen.command.CommandSetHauntingProgress;
import com.windanesz.tracesofthefallen.totf.Tags;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModLootTables;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.world.WorldGenLostCargo;
import com.windanesz.tracesofthefallen.world.WorldGenStoneCircle;
import com.windanesz.tracesofthefallen.world.WorldGenRemains;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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
		proxy.preInit(event);
		ModBlocks.registerTileEntities();
		ModLootTables.register();
		HauntingCapability.register();
	}


	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerWorldGenerator(new WorldGenLostCargo(), 0);
		GameRegistry.registerWorldGenerator(new WorldGenStoneCircle(), 1);
		GameRegistry.registerWorldGenerator(new WorldGenRemains(), 2);
		proxy.registerColorHandlers();
		PacketHandler.initPackets();
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