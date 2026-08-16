package com.windanesz.tracesofthefallen.network;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.packet.PacketPlayerSync;
import com.windanesz.tracesofthefallen.packet.PacketSpawnCrumbs;
import com.windanesz.tracesofthefallen.packet.PacketSpawnGodSlapParticle;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

	public static SimpleNetworkWrapper net;

	public static void initPackets() {
		net = NetworkRegistry.INSTANCE.newSimpleChannel(TracesOfTheFallen.MODID.toUpperCase());
		registerMessage(PacketPlayerSync.class, PacketPlayerSync.Message.class);
		registerMessage(PacketSpawnCrumbs.Handler.class, PacketSpawnCrumbs.class);
		registerMessage(PacketSpawnGodSlapParticle.Handler.class, PacketSpawnGodSlapParticle.class);
	}

	private static int nextPacketId = 0;

	private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
			Class<? extends IMessageHandler<REQ, REPLY>> packet, Class<REQ> message) {
		net.registerMessage(packet, message, nextPacketId, Side.CLIENT);
		net.registerMessage(packet, message, nextPacketId, Side.SERVER);
		nextPacketId++;
	}
}