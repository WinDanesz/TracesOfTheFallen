package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.block.TileEntityGraveMarker;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityGraveMarkerRenderer extends TileEntitySpecialRenderer<TileEntityGraveMarker> {
	@Override
	public void render(TileEntityGraveMarker te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		ItemStack flowerStack = te.getFlowerItemStack();

		if (!flowerStack.isEmpty() && flowerStack.getItem() instanceof ItemBlock) {
			Block flowerBlock = ((ItemBlock) flowerStack.getItem()).getBlock();
			IBlockState flowerState = flowerBlock.getStateFromMeta(flowerStack.getMetadata());

			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			GlStateManager.scale(0.6, 0.6, 0.6);
			GlStateManager.translate(0.3, 0.55, 1.2);
			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.disableLighting();
			Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(flowerState, 1.0f);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();

		}
	}
}