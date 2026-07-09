package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.block.BlockWoodenItemFrame;
import com.windanesz.tracesofthefallen.block.TileEntityWoodenItemFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityWoodenItemFrameRenderer extends TileEntitySpecialRenderer<TileEntityWoodenItemFrame> {

	@Override
	public void render(TileEntityWoodenItemFrame te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		ItemStack stack = te.getDisplayedItem();
		if (stack.isEmpty()) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		if (!(state.getBlock() instanceof BlockWoodenItemFrame)) {
			return;
		}

		EnumFacing facing = state.getValue(BlockWoodenItemFrame.FACING);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

		switch (facing) {
			case NORTH:
				break;
			case SOUTH:
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				break;
			case EAST:
				GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
				break;
			case WEST:
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				break;
			case UP:
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				break;
			case DOWN:
				GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
				break;
		}

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, te.getWorld(), null);
		boolean is3D = model != null && model.isGui3d();

		double zOffset = is3D ? 0.30D : 0.415D;
		float scale = is3D ? 0.45F : 0.60F;

		GlStateManager.translate(0.0D, 0.0D, zOffset);
		GlStateManager.rotate(-45.0F * te.getItemRotation(), 0.0F, 0.0F, 1.0F);
		GlStateManager.scale(scale, scale, scale);

		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();
	}
}
