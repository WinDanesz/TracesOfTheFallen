package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockDecoration;
import com.windanesz.tracesofthefallen.block.TileEntitySpinningWheel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntitySpinningWheelRenderer extends TileEntitySpecialRenderer<TileEntitySpinningWheel> {

	private static final ModelResourceLocation SMALL_WHEEL_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":spinning_wheel_small_wheel", "inventory");
	private static final ModelResourceLocation LARGE_WHEEL_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":spinning_wheel_large_wheel", "inventory");

	@Override
	public void render(TileEntitySpinningWheel te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		float angle = te.getSpinAngle();
		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockDecoration.FACING);
		float yaw = 0.0F;
		switch (facing) {
			case EAST: yaw = 270.0F; break;
			case SOUTH: yaw = 180.0F; break;
			case WEST: yaw = 90.0F; break;
			case NORTH:
			default: yaw = 0.0F; break;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		BlockPos pos = te.getPos();

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// Render Small Wheel
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D); 
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);

		double smallCenterX = 0.3634D;
		double smallCenterY = 1.09278D;
		double smallCenterZ = 1.2785D;
		GlStateManager.translate(smallCenterX, smallCenterY, smallCenterZ);
		GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-smallCenterX, -smallCenterY, -smallCenterZ);

		IBakedModel smallModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(SMALL_WHEEL_MODEL);
		if (smallModel != null) {
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), smallModel, te.getWorld().getBlockState(te.getPos()), pos, buffer, false);
			buffer.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
		}
		GlStateManager.popMatrix();

		// Render Large Wheel
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D); 
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);

		double largeCenterX = 0.50398D;
		double largeCenterY = 1.1186D;
		double largeCenterZ = -0.1551D;
		GlStateManager.translate(largeCenterX, largeCenterY, largeCenterZ);
		
		// The large wheel has 16 segments (22.5 degree symmetry). 
		// The small wheel snaps to 90 degrees.
		// Using a 1:4 gear ratio (0.25F) ensures that when small snaps to 90, large snaps to 22.5, keeping the string perfectly aligned!
		GlStateManager.rotate(angle * 0.25F, 1.0F, 0.0F, 0.0F); 
		
		GlStateManager.translate(-largeCenterX, -largeCenterY, -largeCenterZ);

		IBakedModel largeModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(LARGE_WHEEL_MODEL);
		if (largeModel != null) {
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), largeModel, te.getWorld().getBlockState(te.getPos()), pos, buffer, false);
			buffer.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
		}
		GlStateManager.popMatrix();

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}
}
