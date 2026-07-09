package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelFireOrb;
import com.windanesz.tracesofthefallen.entity.EntityFireOrb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderFireOrb extends Render<EntityFireOrb> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/fire_orb.png");
	private final ModelFireOrb model = new ModelFireOrb();

	public RenderFireOrb(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.15F;
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityFireOrb entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityFireOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);

		float spin = ((float) entity.ticksExisted + partialTicks) * 25.0F;
		GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(spin * 0.5F, 1.0F, 0.0F, 0.0F);

		this.bindEntityTexture(entity);
		GlStateManager.translate(0.0F, 1.5F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

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
