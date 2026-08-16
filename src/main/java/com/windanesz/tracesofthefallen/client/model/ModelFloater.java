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

		// RenderItem automatically translates the model by (-0.5, -0.5, -0.5) to center it.
		// We only need to translate it vertically.
		// Player waist is around 10-12 pixels down from the neck.
		// Since RenderItem shifts Y by -8 pixels, we translate down by 18 pixels (1.125F)
		// so that the model (which goes from 0 to 3 pixels in Y) ends up at Y=10 to 13.
		GlStateManager.translate(0.0F, 1.125F, 0.0F);
		GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

		Minecraft.getMinecraft().getRenderItem().renderItem(this.stack, ItemCameraTransforms.TransformType.NONE);

		GlStateManager.popMatrix();
	}
}
