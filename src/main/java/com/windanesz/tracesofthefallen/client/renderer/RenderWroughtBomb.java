package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelWroughtBomb;
import com.windanesz.tracesofthefallen.entity.EntityGoblinEngineer;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class RenderWroughtBomb extends Render<EntityWroughtBomb> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/wrought_bomb.png");
	private final ModelWroughtBomb model = new ModelWroughtBomb();

	public RenderWroughtBomb(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.5F;
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityWroughtBomb entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityWroughtBomb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x, (float)y, (float)z);

		if (entity.getRidingEntity() instanceof EntityGoblinEngineer) {
			EntityGoblinEngineer eng = (EntityGoblinEngineer) entity.getRidingEntity();
			float deltaYaw = eng.renderYawOffset - eng.prevRenderYawOffset;
			while (deltaYaw < -180.0F) deltaYaw += 360.0F;
			while (deltaYaw >= 180.0F) deltaYaw -= 360.0F;
			float yaw = eng.prevRenderYawOffset + deltaYaw * partialTicks;
			GlStateManager.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -0.05F, 0.35F);
		}

		if (entity.getFuse() > 0 && (float)entity.getFuse() - partialTicks + 1.0F < 10.0F) {
			float f = 1.0F - ((float)entity.getFuse() - partialTicks + 1.0F) / 10.0F;
			f = MathHelper.clamp(f, 0.0F, 1.0F);
			f = f * f;
			f = f * f;
			float scale = 1.0F + f * 0.3F;
			GlStateManager.scale(scale, scale, scale);
		}

		float f2 = (1.0F - ((float)entity.getFuse() - partialTicks + 1.0F) / 100.0F) * 0.8F;
		this.bindEntityTexture(entity);
		GlStateManager.translate(0.0F, 1.5F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

		if (entity.getFuse() > 0 && entity.getFuse() / 5 % 2 == 0) {
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
			GlStateManager.color(1.0F, 1.0F, 1.0F, f2);
			GlStateManager.doPolygonOffset(-3.0F, -3.0F);
			GlStateManager.enablePolygonOffset();
			this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GlStateManager.doPolygonOffset(0.0F, 0.0F);
			GlStateManager.disablePolygonOffset();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
		}

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
}
