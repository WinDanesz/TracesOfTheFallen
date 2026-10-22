package com.windanesz.tracesofthefallen.client;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.compat.baubles.TotfBaublesIntegration;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.packet.PacketActivateRelic;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID, value = Side.CLIENT)
public final class RelicKeyHandler {

	private RelicKeyHandler() {
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		if (RelicKeyBindings.activateRelic.isPressed() && TotfBaublesIntegration.enabled()) {
			PacketHandler.net.sendToServer(new PacketActivateRelic());
		}
	}
}
