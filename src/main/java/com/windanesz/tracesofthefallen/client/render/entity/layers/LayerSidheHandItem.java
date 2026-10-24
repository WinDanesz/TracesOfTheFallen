package com.windanesz.tracesofthefallen.client.render.entity.layers;

import com.windanesz.tracesofthefallen.client.model.ModelSidhe;
import com.windanesz.tracesofthefallen.client.render.entity.RenderSidhe;
import com.windanesz.tracesofthefallen.entity.living.EntitySidhe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;

/** Renders the reward a Sidhe is holding in its hand before dropping it. */
public class LayerSidheHandItem implements LayerRenderer<EntitySidhe> {
	private final RenderSidhe sidheRenderer;

	public LayerSidheHandItem(RenderSidhe sidheRendererIn) {
		this.sidheRenderer = sidheRendererIn;
	}

	@Override
	public void doRenderLayer(EntitySidhe sidhe, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack stack = sidhe.getHandItem();
		if (stack.isEmpty() || !(this.sidheRenderer.getMainModel() instanceof ModelSidhe)) return;

		GlStateManager.pushMatrix();
		((ModelSidhe) this.sidheRenderer.getMainModel()).postRenderRightHand(0.0625F);

		// To the paw at the end of the leg (leg-local model space, 16 units per block)
		GlStateManager.translate(0.0F, 0.5F, -0.1F);
		GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(0.5F, -0.5F, -0.5F);

		Minecraft.getMinecraft().getItemRenderer().renderItem(sidhe, stack, ItemCameraTransforms.TransformType.GROUND);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
