package com.windanesz.tracesofthefallen.packet;

import com.windanesz.tracesofthefallen.SidheDiscoveries;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>[Server -> Client]</b> The full list of Sidhe trades the receiving player has discovered, with the outcome they saw.
 */
public class PacketSidheDiscoveries implements IMessage {

	/** Discovery key to the action seen; the action is null if unknown. */
	private Map<String, String> discovered = new HashMap<>();

	public PacketSidheDiscoveries() {}

	public PacketSidheDiscoveries(Map<String, String> discovered) {
		this.discovered = new HashMap<>(discovered);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			String key = ByteBufUtils.readUTF8String(buf);
			String action = ByteBufUtils.readUTF8String(buf);
			discovered.put(key, action.isEmpty() ? null : action);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(discovered.size());
		for (Map.Entry<String, String> entry : discovered.entrySet()) {
			ByteBufUtils.writeUTF8String(buf, entry.getKey());
			ByteBufUtils.writeUTF8String(buf, entry.getValue() == null ? "" : entry.getValue());
		}
	}

	public static class Handler implements IMessageHandler<PacketSidheDiscoveries, IMessage> {
		@Override
		public IMessage onMessage(PacketSidheDiscoveries message, MessageContext ctx) {
			if (ctx.side.isClient()) {
				Minecraft.getMinecraft().addScheduledTask(() -> SidheDiscoveries.setClientDiscovered(message.discovered));
			}
			return null;
		}
	}
}
