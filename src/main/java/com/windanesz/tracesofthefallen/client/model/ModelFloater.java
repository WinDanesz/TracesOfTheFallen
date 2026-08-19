package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ModelFloater extends ModelBiped {

	private ItemStack stack = ItemStack.EMPTY;

	public ModelFloater() {
		super(0.0F, 0.0F, 64, 32);
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (this.stack == null || this.stack.isEmpty()) return;

		GlStateManager.pushMatrix();

		if (this.isChild) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
		}
		if (this.isSneak) {
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
		}

		this.bipedBody.postRender(scale);

		// We need a final position of Y=10 to 13 pixels down from the neck.
		// Before rotation, the block was at Y=-8 to -5, so a translation of 18 pixels (1.125F) worked.
		// After 180-degree rotation around X, the block is at Y=5 to 8 pixels.
		// So we only need to translate it down by 5 pixels (0.3125F).
		GlStateManager.translate(0.0F, 0.3125F, 0.0F);
		GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

		Minecraft.getMinecraft().getRenderItem().renderItem(this.stack, ItemCameraTransforms.TransformType.NONE);

		GlStateManager.popMatrix();
	}
}
