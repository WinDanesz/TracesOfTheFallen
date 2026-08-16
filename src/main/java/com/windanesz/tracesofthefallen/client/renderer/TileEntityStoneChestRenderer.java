package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockStoneChest;
import com.windanesz.tracesofthefallen.block.TileEntityStoneChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

public class TileEntityStoneChestRenderer extends TileEntitySpecialRenderer<TileEntityStoneChest> {

	private IBakedModel lidModel;

	@Override
	public void render(TileEntityStoneChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (!te.hasWorld()) return;

		if (this.lidModel == null) {
			this.lidModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
					.getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":stone_chest_lid", "normal"));
		}

		float lidAngle = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
		
		float slideProgress = Math.min(1.0F, lidAngle * 2.0F); // 0 to 1 over first 50%
		// ease in-out for slide
		slideProgress = slideProgress * slideProgress * (3.0F - 2.0F * slideProgress);
		
		float fallProgress = Math.max(0.0F, (lidAngle - 0.25F) * 1.3333333F); // starts exactly halfway through slide
		fallProgress = Math.min(1.0F, fallProgress);
		// ease-in for fall (accelerates as it drops)
		fallProgress = fallProgress * fallProgress;

		float zOffset = slideProgress * 0.5F; // slide exactly 8 pixels back
		float rotX = fallProgress * 85.0F; // pivot downwards by 85 degrees

		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockStoneChest.FACING);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		// Center the rotation
		GlStateManager.translate(0.5, 0.5, 0.5);

		int rotation = 0;
		switch (facing) {
			case NORTH: rotation = 0; break;
			case EAST: rotation = 270; break;
			case SOUTH: rotation = 180; break;
			case WEST: rotation = 90; break;
		}
		GlStateManager.rotate((float) rotation, 0.0F, 1.0F, 0.0F);

		// Uncenter
		GlStateManager.translate(-0.5, -0.5, -0.5);

		// Apply slide translation
		GlStateManager.translate(0.0F, 0.0F, zOffset);
		
		// Rotate around the FRONT-BOTTOM edge of the lid (X=8, Y=9, Z=4)
		GlStateManager.translate(0.5F, 0.5625F, 0.25F);
		GlStateManager.rotate(rotX, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-0.5F, -0.5625F, -0.25F);

		// Setup lighting based on the block's world position
		int light = te.getWorld().getCombinedLight(te.getPos(), 0);
		int lightX = light % 65536;
		int lightY = light / 65536;
		net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, (float)lightX, (float)lightY);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// Render the baked model using immediate mode so it respects GlStateManager
		Minecraft.getMinecraft().renderEngine.bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
		BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		blockRendererDispatcher.getBlockModelRenderer().renderModelBrightnessColor(
				this.lidModel, 
				1.0F, 
				1.0F, 1.0F, 1.0F
		);

		GlStateManager.popMatrix();
	}
}
