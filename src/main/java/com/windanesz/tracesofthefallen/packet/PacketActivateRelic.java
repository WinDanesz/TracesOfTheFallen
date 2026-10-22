package com.windanesz.tracesofthefallen.packet;

import com.windanesz.tracesofthefallen.compat.baubles.TotfBaublesIntegration;
import com.windanesz.tracesofthefallen.item.ItemArcaneRelic;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Sent by the client when the relic keybind is pressed; activates every arcane relic worn in a Baubles charm slot. */
public class PacketActivateRelic implements IMessage {

	public PacketActivateRelic() {}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}

	public static class Handler implements IMessageHandler<PacketActivateRelic, IMessage> {
		@Override
		public IMessage onMessage(PacketActivateRelic message, MessageContext ctx) {
			if (!ctx.side.isServer()) {
				return null;
			}

			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				if (!TotfBaublesIntegration.enabled()) {
					return;
				}

				for (ItemStack stack : TotfBaublesIntegration.getEquippedRelics(player)) {
					((ItemArcaneRelic) stack.getItem()).activate(player, stack);
				}
			});
			return null;
		}
	}
}
