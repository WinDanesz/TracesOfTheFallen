package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.entity.EntityFetidDagger;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderFetidDagger extends Render<EntityFetidDagger> {

	public RenderFetidDagger(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.15F;
	}

	@Override
	public void doRender(EntityFetidDagger entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x, (float)y, (float)z);

		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

		if (!entity.isInGround()) {
			float spin = ((float)entity.ticksExisted + partialTicks) * 45.0F;
			GlStateManager.rotate(spin - 45.0F, 0.0F, 0.0F, 1.0F);
		} else {
			GlStateManager.translate(0.15F, -0.05F, 0.0F);
			GlStateManager.rotate(-45.0F, 0.0F, 0.0F, 1.0F);
		}

		GlStateManager.scale(0.85F, 0.85F, 0.85F);

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}

		ItemStack stack = entity.getDaggerStack();
		if (stack == null || stack.isEmpty()) {
			stack = new ItemStack(ModItems.fetid_dagger);
		}
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);

		if (this.renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityFetidDagger entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
}
