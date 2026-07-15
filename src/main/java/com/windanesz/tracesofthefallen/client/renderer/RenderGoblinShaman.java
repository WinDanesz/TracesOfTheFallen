package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelGoblinShaman;
import com.windanesz.tracesofthefallen.client.model.ModelMagmaBlast;
import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderGoblinShaman extends RenderBiped<EntityGoblinShaman> {
	private static final ResourceLocation[] TEXTURES = new ResourceLocation[10];
	private final ModelMagmaBlast magmaBlastModel = new ModelMagmaBlast();

	static {
		for (int i = 0; i < 10; i++) {
			TEXTURES[i] = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/shaman_" + i + ".png");
		}
	}

	public RenderGoblinShaman(RenderManager renderManager) {
		super(renderManager, new ModelGoblinShaman(), 0.2F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGoblinShaman entity) {
		int variant = entity.getShamanVariant();
		if (variant < 0 || variant >= 10) {
			variant = 0;
		}
		return TEXTURES[variant];
	}

	@Override
	public void doRender(EntityGoblinShaman entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		if (entity.getSpellCastingTimer() > 0 && entity.getSpellCastType() == 8 && !entity.isInvisible()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x, (float) y, (float) z);

			float bodyYaw = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks;
			GlStateManager.rotate(-bodyYaw, 0.0F, 1.0F, 0.0F);

			float age = (float) entity.ticksExisted + partialTicks;
			float timerExact = (float) entity.getSpellCastingTimer() - partialTicks;

			float blobY;
			float blobZ;
			float blobScale;

			if (timerExact > 8.0F) {
				// Overhead charging
				float bob = MathHelper.sin(age * 0.45F) * 0.05F;
				blobY = entity.height + 0.85F + bob;
				blobZ = 0.25F;
				float elapsed = Math.max(0.0F, 50.0F - timerExact);
				float growth = MathHelper.clamp(elapsed / 40.0F, 0.05F, 1.0F);
				blobScale = growth * 2.2F;
			} else {
				// Forward hurling trajectory (+0.4-0.5s duration right into the hands releasing)
				float throwProgress = MathHelper.clamp((8.0F - timerExact) / 7.0F, 0.0F, 1.0F);
				blobY = entity.height + 0.85F - 0.5F * throwProgress;
				blobZ = 0.25F + 0.7F * throwProgress;
				blobScale = 2.2F;
			}

			GlStateManager.translate(0.0F, blobY, blobZ);

			float spin = age * 25.0F;
			GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(spin * 0.7F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(spin * 0.4F, 0.0F, 0.0F, 1.0F);

			GlStateManager.scale(-blobScale, -blobScale, blobScale);

			this.bindTexture(RenderMagmaBlast.TEXTURE);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

			this.magmaBlastModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

			int packedLight = entity.getBrightnessForRender();
			int blockLight = packedLight % 65536;
			int skyLight = packedLight / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
