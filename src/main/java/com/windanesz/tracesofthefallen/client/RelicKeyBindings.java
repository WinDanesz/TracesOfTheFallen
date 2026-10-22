package com.windanesz.tracesofthefallen.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class RelicKeyBindings {

	public static final KeyBinding activateRelic = new KeyBinding("key.totf.activate_relic", Keyboard.KEY_R, "key.categories.totf");

	private RelicKeyBindings() {
	}

	public static void register() {
		ClientRegistry.registerKeyBinding(activateRelic);
	}
}
