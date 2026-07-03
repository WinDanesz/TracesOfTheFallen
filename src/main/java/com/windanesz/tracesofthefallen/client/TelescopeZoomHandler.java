package com.windanesz.tracesofthefallen.client;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityTelescopeSeat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID, value = Side.CLIENT)
public final class TelescopeZoomHandler {

	private static final float[] ZOOM_STEPS = {1.0F, 2.0F, 4.0F, 8.0F, 16.0F, 32.0F};
	private static final float ZOOM_INTERPOLATION = 0.3F;
	private static final float EXTRA_ZOOM_STRENGTH = 2.0F;
	private static final float MIN_TELESCOPE_FOV = 0.5F;
	private static final ResourceLocation TELESCOPE_OVERLAY = new ResourceLocation(TracesOfTheFallen.MODID, "textures/blocks/telescope_overlay.png");
	private static final float OVERLAY_SCALE = 0.82F;

	private static int zoomStepIndex;
	private static float currentZoom = ZOOM_STEPS[0];
	private static float targetZoom = ZOOM_STEPS[0];

	private TelescopeZoomHandler() {
	}

	@SubscribeEvent
	public static void onMouseInput(MouseEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!isScrollZoomActive(minecraft)) {
			return;
		}

		int wheelDelta = event.getDwheel();
		if (wheelDelta == 0) {
			return;
		}

		if (wheelDelta > 0) {
			zoomStepIndex = Math.min(zoomStepIndex + 1, ZOOM_STEPS.length - 1);
		} else {
			zoomStepIndex = Math.max(zoomStepIndex - 1, 0);
		}

		targetZoom = ZOOM_STEPS[zoomStepIndex];
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onFovModifier(EntityViewRenderEvent.FOVModifier event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (event.getEntity() != minecraft.player || !isMountedInFirstPerson(minecraft)) {
			return;
		}

		float effectiveZoom = 1.0F + (Math.max(currentZoom, 1.0F) - 1.0F) * EXTRA_ZOOM_STRENGTH;
		event.setFOV(Math.max(event.getFOV() / effectiveZoom, MIN_TELESCOPE_FOV));
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		if (!isMountedInFirstPerson(minecraft)) {
			return;
		}

		renderOverlay(minecraft, event.getResolution());
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		if (!isMountedInFirstPerson(minecraft)) {
			resetZoom();
			return;
		}

		if (Math.abs(targetZoom - currentZoom) < 0.01F) {
			currentZoom = targetZoom;
			return;
		}

		currentZoom += (targetZoom - currentZoom) * ZOOM_INTERPOLATION;
	}

	private static boolean isScrollZoomActive(Minecraft minecraft) {
		return isMountedInFirstPerson(minecraft) && minecraft.currentScreen == null;
	}

	private static boolean isMountedInFirstPerson(Minecraft minecraft) {
		EntityPlayer player = minecraft.player;
		return player != null
				&& minecraft.gameSettings.thirdPersonView == 0
				&& player.getRidingEntity() instanceof EntityTelescopeSeat;
	}

	private static void resetZoom() {
		zoomStepIndex = 0;
		currentZoom = ZOOM_STEPS[0];
		targetZoom = ZOOM_STEPS[0];
	}

	private static void renderOverlay(Minecraft minecraft, ScaledResolution resolution) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		int size = Math.round(Math.min(width, height) * OVERLAY_SCALE);
		int left = (width - size) / 2;
		int top = (height - size) / 2;
		int right = left + size;
		int bottom = top + size;

		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlpha();

		GlStateManager.disableTexture2D();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		addBlackQuad(buffer, 0, 0, width, top);
		addBlackQuad(buffer, 0, bottom, width, height);
		addBlackQuad(buffer, 0, top, left, bottom);
		addBlackQuad(buffer, right, top, width, bottom);
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TELESCOPE_OVERLAY);
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(left, bottom, -90.0D).tex(0.0D, 1.0D).endVertex();
		buffer.pos(right, bottom, -90.0D).tex(1.0D, 1.0D).endVertex();
		buffer.pos(right, top, -90.0D).tex(1.0D, 0.0D).endVertex();
		buffer.pos(left, top, -90.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static void addBlackQuad(BufferBuilder buffer, int left, int top, int right, int bottom) {
		if (left >= right || top >= bottom) {
			return;
		}

		buffer.pos(left, bottom, -90.0D).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
		buffer.pos(right, bottom, -90.0D).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
		buffer.pos(right, top, -90.0D).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
		buffer.pos(left, top, -90.0D).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
	}
}
