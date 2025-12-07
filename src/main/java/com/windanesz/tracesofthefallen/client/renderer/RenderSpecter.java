package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelSpecter;
import com.windanesz.tracesofthefallen.entity.EntitySpecter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class RenderSpecter extends RenderLiving<EntitySpecter> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/specter.png");

	public RenderSpecter(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelSpecter(), 0F);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntitySpecter entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntitySpecter entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		float alpha = 0.4F + (MathHelper.sin((entity.ticksExisted + partialTicks) / 10.0F) + 1.0F) * 0.25F;
		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
	}

}