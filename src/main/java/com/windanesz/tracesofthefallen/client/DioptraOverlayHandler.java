package com.windanesz.tracesofthefallen.client;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityDioptraSeat;
import com.windanesz.tracesofthefallen.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID, value = Side.CLIENT)
public final class DioptraOverlayHandler {

	private static float lastYaw;
	private static float lastPitch;
	private static int nextSoundTick;
	private static boolean wasMounted;

	private DioptraOverlayHandler() {}

	@SubscribeEvent
	public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
		Minecraft minecraft = Minecraft.getMinecraft();

		if (minecraft.gameSettings.thirdPersonView != 0 || !(event.getEntity() instanceof EntityPlayer)) {
			return;
		}

		EntityPlayer player = (EntityPlayer) event.getEntity();
		if (!(player.getRidingEntity() instanceof EntityDioptraSeat)) {
			return;
		}

		EntityDioptraSeat seat = (EntityDioptraSeat) player.getRidingEntity();
		Vec3d currentEyePos = player.getPositionEyes((float) event.getRenderPartialTicks());
		Vec3d targetEyePos = seat.getFirstPersonCameraPosition(player, (float) event.getRenderPartialTicks());
		Vec3d offset = targetEyePos.subtract(currentEyePos);
		GlStateManager.translate(-offset.x, -offset.y, -offset.z);
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.player;

		if (player == null || minecraft.gameSettings.hideGUI || !(player.getRidingEntity() instanceof EntityDioptraSeat)) {
			return;
		}

		EntityDioptraSeat seat = (EntityDioptraSeat) player.getRidingEntity();
		double distance = seat.getLookDistance(player, event.getPartialTicks());
		String text = Double.isNaN(distance)
				? I18n.format("totf.dioptra.distance_unknown")
				: I18n.format("totf.dioptra.distance", String.format(Locale.ROOT, "%.1f", distance));

		ScaledResolution resolution = event.getResolution();
		int x = resolution.getScaledWidth() - minecraft.fontRenderer.getStringWidth(text) - 8;
		int y = resolution.getScaledHeight() - 32;
		minecraft.fontRenderer.drawStringWithShadow(text, x, y, 0xFFFFFF);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.player;

		if (player == null || !(player.getRidingEntity() instanceof EntityDioptraSeat)) {
			wasMounted = false;
			return;
		}

		float yaw = player.rotationYaw;
		float pitch = player.rotationPitch;

		if (!wasMounted) {
			lastYaw = yaw;
			lastPitch = pitch;
			nextSoundTick = player.ticksExisted;
			wasMounted = true;
			return;
		}

		float yawDelta = Math.abs(yaw - lastYaw);
		float pitchDelta = Math.abs(pitch - lastPitch);
		if ((yawDelta > 0.25F || pitchDelta > 0.25F) && player.ticksExisted >= nextSoundTick) {
			player.playSound(ModSounds.DIOPTRA, 0.35F, 1.0F);
			nextSoundTick = player.ticksExisted + 7;
		}

		lastYaw = yaw;
		lastPitch = pitch;
	}
}
