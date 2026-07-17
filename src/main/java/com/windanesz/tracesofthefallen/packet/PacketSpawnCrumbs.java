package com.windanesz.tracesofthefallen.packet;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
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
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = Minecraft.getMinecraft().world;
				if (world != null) {
					for (int i = 0; i < message.count; i++) {
						double px = message.x + (world.rand.nextDouble() - 0.5D) * 2.0D * message.spreadX;
						double py = message.y + (world.rand.nextDouble() - 0.5D) * 2.0D * message.spreadY;
						double pz = message.z + (world.rand.nextDouble() - 0.5D) * 2.0D * message.spreadZ;
						double mx = (world.rand.nextDouble() - 0.5D) * 0.15D;
						double my = 0.05D + world.rand.nextDouble() * 0.1D;
						double mz = (world.rand.nextDouble() - 0.5D) * 0.15D;
						TracesOfTheFallen.proxy.spawnCrumbsParticle(world, px, py, pz, mx, my, mz);
					}
				}
			});
			return null;
		}
	}
}
