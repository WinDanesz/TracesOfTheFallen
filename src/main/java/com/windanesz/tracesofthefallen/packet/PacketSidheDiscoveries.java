package com.windanesz.tracesofthefallen.packet;

import com.windanesz.tracesofthefallen.SidheDiscoveries;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <b>[Server -> Client]</b> The full list of Sidhe trades the receiving player has discovered.
 */
public class PacketSidheDiscoveries implements IMessage {

	private List<String> keys = new ArrayList<>();

	public PacketSidheDiscoveries() {}

	public PacketSidheDiscoveries(Collection<String> keys) {
		this.keys = new ArrayList<>(keys);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			keys.add(ByteBufUtils.readUTF8String(buf));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(keys.size());
		for (String key : keys) {
			ByteBufUtils.writeUTF8String(buf, key);
		}
	}

	public static class Handler implements IMessageHandler<PacketSidheDiscoveries, IMessage> {
		@Override
		public IMessage onMessage(PacketSidheDiscoveries message, MessageContext ctx) {
			if (ctx.side.isClient()) {
				Minecraft.getMinecraft().addScheduledTask(() -> SidheDiscoveries.setClientDiscovered(message.keys));
			}
			return null;
		}
	}
}
