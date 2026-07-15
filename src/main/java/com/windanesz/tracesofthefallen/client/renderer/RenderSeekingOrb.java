package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelSeekingOrb;
import com.windanesz.tracesofthefallen.entity.EntitySeekingOrb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderSeekingOrb extends Render<EntitySeekingOrb> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/seeking_orb.png");
	private final ModelSeekingOrb model = new ModelSeekingOrb();

	public RenderSeekingOrb(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.2F;
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntitySeekingOrb entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntitySeekingOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);

		this.bindEntityTexture(entity);
		GlStateManager.translate(0.0F, 1.5F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		GlStateManager.disableCull();

		this.model.render(entity, 0.0F, 0.0F, (float) entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);

		GlStateManager.enableCull();
		int packedLight = entity.getBrightnessForRender();
		int blockLight = packedLight % 65536;
		int skyLight = packedLight / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
}
