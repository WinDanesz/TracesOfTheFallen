package com.windanesz.tracesofthefallen.client.render.entity.layers;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.render.entity.RenderSidhe;
import com.windanesz.tracesofthefallen.entity.EntitySidhe;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerSidheEyes implements LayerRenderer<EntitySidhe> {
	private static final ResourceLocation EYES = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/sidhe_eyes.png");
	private static final ResourceLocation EYES_ANGRY = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/sidhe_angry.png");
	private final RenderSidhe sidheRenderer;

	public LayerSidheEyes(RenderSidhe sidheRendererIn) {
		this.sidheRenderer = sidheRendererIn;
	}

	@Override
	public void doRenderLayer(EntitySidhe entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.sidheRenderer.bindTexture(entitylivingbaseIn.isAngry() ? EYES_ANGRY : EYES);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		if (entitylivingbaseIn.isInvisible()) {
			GlStateManager.depthMask(false);
		} else {
			GlStateManager.depthMask(true);
		}

		int i = entitylivingbaseIn.getBrightnessForRender();
		int blockLight = i % 65536;
		int skyLight = i / 65536;

		// 50% minimum brightness for the eyes (15 * 16 * 0.5 = 120)
		blockLight = Math.max(blockLight, 120);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)blockLight, (float)skyLight);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.sidheRenderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		
		// Restore normal lightmap
		i = entitylivingbaseIn.getBrightnessForRender();
		blockLight = i % 65536;
		skyLight = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)blockLight, (float)skyLight);
		this.sidheRenderer.setLightmap(entitylivingbaseIn);
		GlStateManager.disableBlend();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
