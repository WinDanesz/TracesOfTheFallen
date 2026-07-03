package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelMinecrawler;
import com.windanesz.tracesofthefallen.entity.EntityMinecrawler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderMinecrawler extends RenderLiving<EntityMinecrawler> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/minecrawler.png");
	private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/minecrawler_eyes.png");
private static final float EYES_BRIGHTNESS_MULTIPLIER = 0.7F;

	public RenderMinecrawler(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelMinecrawler(), 0.5F);
		this.addLayer(new LayerMinecrawlerEyes(this));
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityMinecrawler entity) {
		return TEXTURE;
	}

	private static final class LayerMinecrawlerEyes implements LayerRenderer<EntityMinecrawler> {

		private final RenderMinecrawler renderer;

		private LayerMinecrawlerEyes(RenderMinecrawler renderer) {
			this.renderer = renderer;
		}

		@Override
		public void doRenderLayer(EntityMinecrawler entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
				float netHeadYaw, float headPitch, float scale) {
			renderer.bindTexture(EYES_TEXTURE);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			GlStateManager.depthMask(!entity.isInvisible());

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			GlStateManager.color(EYES_BRIGHTNESS_MULTIPLIER, EYES_BRIGHTNESS_MULTIPLIER, EYES_BRIGHTNESS_MULTIPLIER, 1.0F);
			renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

			int packedLight = entity.getBrightnessForRender();
			int blockLight = packedLight % 65536;
			int skyLight = packedLight / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);

			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}
}
