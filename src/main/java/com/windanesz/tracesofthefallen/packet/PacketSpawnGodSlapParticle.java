package com.windanesz.tracesofthefallen.packet;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSpawnGodSlapParticle implements IMessage {
	private double x, y, z;

	public PacketSpawnGodSlapParticle() {}

	public PacketSpawnGodSlapParticle(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
	}

	public static class Handler implements IMessageHandler<PacketSpawnGodSlapParticle, IMessage> {
		@Override
		public IMessage onMessage(PacketSpawnGodSlapParticle message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() ->
					TracesOfTheFallen.proxy.spawnGodSlapParticleFromPacket(message.x, message.y, message.z));
			return null;
		}
	}
}
