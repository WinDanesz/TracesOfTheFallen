package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockArmillary;
import com.windanesz.tracesofthefallen.block.TileEntityArmillary;
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
public class TileEntityArmillaryRenderer extends TileEntitySpecialRenderer<TileEntityArmillary> {

	private static final ModelResourceLocation INNER_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":armillary_inner", "inventory");
	private static final ModelResourceLocation OUTER_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":armillary_outer", "inventory");

	@Override
	public void render(TileEntityArmillary te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		float innerAngle = te.getInnerAngle();
		float outerAngle = te.getOuterAngle();
		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockArmillary.FACING);
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
		// Removed disableCull() to prevent Z-fighting on 0-thickness faces
		GlStateManager.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// Center of rotation for the armillary rings is at [8, 11, 8] which is 0.5, 0.6875, 0.5
		double centerX = 0.5D;
		double centerY = 0.6875D; // 11 / 16
		double centerZ = 0.5D;

		// Render Outer Ring (Rotates on Y axis)
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);

		GlStateManager.translate(centerX, centerY, centerZ);
		GlStateManager.rotate(outerAngle, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-centerX, -centerY, -centerZ);

		IBakedModel outerModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(OUTER_MODEL);
		if (outerModel != null) {
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), outerModel, te.getWorld().getBlockState(te.getPos()), pos, buffer, false);
			buffer.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
		}
		GlStateManager.popMatrix();

		// Render Inner Ring (Rotates on X axis)
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);

		GlStateManager.translate(centerX, centerY, centerZ);
		// Apply outer ring's rotation first so it acts as a bone
		GlStateManager.rotate(outerAngle, 0.0F, 1.0F, 0.0F);
		// Apply static 90-degree offset so it is perpendicular to the outer ring
		GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
		// Rotate on X axis
		GlStateManager.rotate(innerAngle, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-centerX, -centerY, -centerZ);

		IBakedModel innerModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(INNER_MODEL);
		if (innerModel != null) {
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), innerModel, te.getWorld().getBlockState(te.getPos()), pos, buffer, false);
			buffer.setTranslation(0.0D, 0.0D, 0.0D);
			tessellator.draw();
		}
		GlStateManager.popMatrix();

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}
}
