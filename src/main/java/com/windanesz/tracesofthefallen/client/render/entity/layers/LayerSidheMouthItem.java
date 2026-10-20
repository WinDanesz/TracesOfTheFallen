package com.windanesz.tracesofthefallen.client.render.entity.layers;

import com.windanesz.tracesofthefallen.client.model.ModelSidhe;
import com.windanesz.tracesofthefallen.client.render.entity.RenderSidhe;
import com.windanesz.tracesofthefallen.entity.EntitySidhe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;

/** Renders the item a Sidhe is currently holding in its mouth. */
public class LayerSidheMouthItem implements LayerRenderer<EntitySidhe> {
	private final RenderSidhe sidheRenderer;

	public LayerSidheMouthItem(RenderSidhe sidheRendererIn) {
		this.sidheRenderer = sidheRendererIn;
	}

	@Override
	public void doRenderLayer(EntitySidhe sidhe, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack stack = sidhe.getMouthItem();
		if (stack.isEmpty() || !(this.sidheRenderer.getMainModel() instanceof ModelSidhe)) return;

		GlStateManager.pushMatrix();
		((ModelSidhe) this.sidheRenderer.getMainModel()).postRenderHead(0.0625F);

		// Move to the front of the snout (head-local model space, 16 units per block)
		GlStateManager.translate(0.0F, -0.0625F + 0.0625F / 3.0F, -0.25F);
		GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(0.75F, -0.75F, -0.75F);
		// Lay the sprite flat, tilted ~20 degrees so the end poking out of the mouth hangs lower than the end inside it
		GlStateManager.rotate(70.0F, 1.0F, 0.0F, 0.0F);

		Minecraft.getMinecraft().getItemRenderer().renderItem(sidhe, stack, ItemCameraTransforms.TransformType.GROUND);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
