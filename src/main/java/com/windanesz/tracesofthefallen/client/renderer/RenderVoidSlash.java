package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelVoidSlash;
import com.windanesz.tracesofthefallen.entity.EntityVoidSlash;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderVoidSlash extends Render<EntityVoidSlash> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/void_slash.png");
	private final ModelVoidSlash model = new ModelVoidSlash();

	public RenderVoidSlash(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.3F;
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityVoidSlash entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityVoidSlash entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);

		this.bindEntityTexture(entity);

		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
		float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

		GlStateManager.rotate(yaw - 180.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);

		GlStateManager.scale(1.25F, 1.25F, 1.25F);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

		GlStateManager.matrixMode(org.lwjgl.opengl.GL11.GL_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		int[] frames = new int[]{0, 1, 2, 3, 4, 5, 3, 4, 3, 5, 3, 4, 3, 5, 3, 4, 3, 5, 3, 4, 3, 5, 3, 5};
		int frameIndex = (int) ((entity.ticksExisted + partialTicks) * 0.5F) % frames.length;
		if (frameIndex < 0) frameIndex = 0;
		float vOffset = (float) frames[frameIndex] * (32.0F / 192.0F);
		GlStateManager.translate(0.0F, vOffset, 0.0F);
		GlStateManager.matrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);

		GlStateManager.disableCull();
		this.model.render(entity, 0.0F, 0.0F, (float) entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);
		GlStateManager.enableCull();

		GlStateManager.matrixMode(org.lwjgl.opengl.GL11.GL_TEXTURE);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);

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
