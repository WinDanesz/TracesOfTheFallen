package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelFamiliarSpecter;
import com.windanesz.tracesofthefallen.entity.EntityFamiliarSpecter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class RenderFamiliarSpecter extends RenderLiving<EntityFamiliarSpecter> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/specter_familiar.png");

	public RenderFamiliarSpecter(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelFamiliarSpecter(), 0F);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityFamiliarSpecter entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityFamiliarSpecter entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		float alpha = 0.4F + (MathHelper.sin((entity.ticksExisted + partialTicks) / 10.0F) + 1.0F) * 0.25F;
		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
	}

}