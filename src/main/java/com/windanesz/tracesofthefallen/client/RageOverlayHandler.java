package com.windanesz.tracesofthefallen.client;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID, value = Side.CLIENT)
public final class RageOverlayHandler {

	private RageOverlayHandler() {
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.player;
		if (player == null || minecraft.gameSettings.hideGUI) {
			return;
		}

		PotionEffect rage = player.getActivePotionEffect(ModPotions.rage);
		if (rage == null) {
			return;
		}

		renderOverlay(event.getResolution(), getOverlayAlpha(rage));
	}

	private static float getOverlayAlpha(PotionEffect rage) {
		return 0.16F + (0.04F * Math.min(rage.getAmplifier(), 2));
	}

	private static void renderOverlay(ScaledResolution resolution, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();

		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlpha();
		GlStateManager.disableTexture2D();

		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(0, height, -90.0D).color(0.9F, 0.03F, 0.03F, alpha).endVertex();
		buffer.pos(width, height, -90.0D).color(0.9F, 0.03F, 0.03F, alpha).endVertex();
		buffer.pos(width, 0, -90.0D).color(0.9F, 0.03F, 0.03F, alpha).endVertex();
		buffer.pos(0, 0, -90.0D).color(0.9F, 0.03F, 0.03F, alpha).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
