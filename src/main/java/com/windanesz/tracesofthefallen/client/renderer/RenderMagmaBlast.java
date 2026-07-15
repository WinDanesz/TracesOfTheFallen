package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelMagmaBlast;
import com.windanesz.tracesofthefallen.entity.EntityMagmaBlast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderMagmaBlast extends Render<EntityMagmaBlast> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/magma.png");
	private final ModelMagmaBlast model = new ModelMagmaBlast();

	public RenderMagmaBlast(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.65F;
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityMagmaBlast entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityMagmaBlast entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);

		float spin = ((float) entity.ticksExisted + partialTicks) * 20.0F;
		GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(spin * 0.7F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(spin * 0.4F, 0.0F, 0.0F, 1.0F);

		this.bindEntityTexture(entity);
		GlStateManager.translate(0.0F, 0.5F, 0.0F);
		// Scale by 2.2F on the 8x8 core sphere to make a large 3x size boulder
		GlStateManager.scale(-2.2F, -2.2F, 2.2F);

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

		this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

		int packedLight = entity.getBrightnessForRender();
		int blockLight = packedLight % 65536;
		int skyLight = packedLight / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
}
