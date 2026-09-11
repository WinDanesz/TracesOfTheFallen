package com.windanesz.tracesofthefallen.packet;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSpawnCrumbs implements IMessage {
	private double x, y, z;
	private int count;
	private double spreadX, spreadY, spreadZ;

	public PacketSpawnCrumbs() {}

	public PacketSpawnCrumbs(double x, double y, double z, int count, double spreadX, double spreadY, double spreadZ) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.count = count;
		this.spreadX = spreadX;
		this.spreadY = spreadY;
		this.spreadZ = spreadZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.count = buf.readInt();
		this.spreadX = buf.readDouble();
		this.spreadY = buf.readDouble();
		this.spreadZ = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
		buf.writeInt(this.count);
		buf.writeDouble(this.spreadX);
		buf.writeDouble(this.spreadY);
		buf.writeDouble(this.spreadZ);
	}

	public static class Handler implements IMessageHandler<PacketSpawnCrumbs, IMessage> {
		@Override
		public IMessage onMessage(PacketSpawnCrumbs message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> TracesOfTheFallen.proxy.spawnCrumbsParticlesFromPacket(
					message.x, message.y, message.z, message.count, message.spreadX, message.spreadY, message.spreadZ));
			return null;
		}
	}
}
