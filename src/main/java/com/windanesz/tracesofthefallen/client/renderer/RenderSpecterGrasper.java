package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelSpecterGrasper;
import com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class RenderSpecterGrasper extends RenderLiving<EntitySpecterGrasper> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/tied_specter_grasper.png");

	public RenderSpecterGrasper(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelSpecterGrasper(), 0F);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntitySpecterGrasper entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntitySpecterGrasper entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		float alpha = 0.4F + (MathHelper.sin((entity.ticksExisted + partialTicks) / 10.0F) + 1.0F) * 0.25F;
		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
	}

}
