package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelMagmaPool;
import com.windanesz.tracesofthefallen.entity.EntityMagmaPool;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderMagmaPool extends Render<EntityMagmaPool> {
	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/magma.png");
	private final ModelMagmaPool model = new ModelMagmaPool();

	public RenderMagmaPool(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.5F;
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityMagmaPool entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityMagmaPool entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);

		this.bindEntityTexture(entity);
		GlStateManager.translate(0.0F, 1.5F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

		float ageInTicks = (float) entity.ticksExisted + partialTicks;
		this.model.render(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F, 0.0625F);

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
}
