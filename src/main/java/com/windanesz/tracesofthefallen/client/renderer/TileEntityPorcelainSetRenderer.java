package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.PorcelainLiquids;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockPorcelainSet;
import com.windanesz.tracesofthefallen.block.TileEntityPorcelainSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntityPorcelainSetRenderer extends TileEntitySpecialRenderer<TileEntityPorcelainSet> {

	private static final double CUP_Y = 0.185D;
	private static final double CUP_A_MIN_X = 0.25D;
	private static final double CUP_A_MAX_X = 0.4375D;
	private static final double CUP_A_MIN_Z = 0.0625D;
	private static final double CUP_A_MAX_Z = 0.25D;
	private static final double CUP_B_MIN_X = 0.25D;
	private static final double CUP_B_MAX_X = 0.4375D;
	private static final double CUP_B_MIN_Z = 0.75D;
	private static final double CUP_B_MAX_Z = 0.9375D;
	private static final double LIQUID_UV_MAX = 3.0D / 16.0D;
	private static final double LIQUID_INSET = 0.2D / 16.0D;
	private static final ResourceLocation LIQUID_TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/blocks/porcelain_liquid.png");
	private static ITextureObject liquidTextureObject;

	@Override
	public void render(TileEntityPorcelainSet te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		if (!(state.getBlock() instanceof BlockPorcelainSet)) {
			return;
		}

		BlockPorcelainSet.Arrangement arrangement = state.getValue(BlockPorcelainSet.ARRANGEMENT);
		EnumFacing facing = state.getValue(BlockPorcelainSet.FACING);
		String cup0 = te.getCupContent(0);
		String cup1 = te.getCupContent(1);

		if (arrangement == BlockPorcelainSet.Arrangement.POT_AND_CUP) {
			if (!cup0.isEmpty()) {
				renderOrientedCupQuad(x, y, z, facing, CUP_A_MIN_X, CUP_A_MAX_X, CUP_A_MIN_Z, CUP_A_MAX_Z, PorcelainLiquids.getColor(cup0));
			}
			return;
		}

		if (arrangement == BlockPorcelainSet.Arrangement.CUPS || arrangement == BlockPorcelainSet.Arrangement.FULL) {
			if (!cup0.isEmpty()) {
				renderOrientedCupQuad(x, y, z, facing, CUP_A_MIN_X, CUP_A_MAX_X, CUP_A_MIN_Z, CUP_A_MAX_Z, PorcelainLiquids.getColor(cup0));
			}
			if (!cup1.isEmpty()) {
				renderOrientedCupQuad(x, y, z, facing, CUP_B_MIN_X, CUP_B_MAX_X, CUP_B_MIN_Z, CUP_B_MAX_Z, PorcelainLiquids.getColor(cup1));
			}
		}
	}

	private void renderOrientedCupQuad(double x, double y, double z, EnumFacing facing, double minX, double maxX, double minZ, double maxZ, int color) {
		double rMinX = minX;
		double rMaxX = maxX;
		double rMinZ = minZ;
		double rMaxZ = maxZ;

		switch (facing) {
			case EAST:
				rMinX = 1.0D - maxZ;
				rMaxX = 1.0D - minZ;
				rMinZ = minX;
				rMaxZ = maxX;
				break;
			case SOUTH:
				rMinX = 1.0D - maxX;
				rMaxX = 1.0D - minX;
				rMinZ = 1.0D - maxZ;
				rMaxZ = 1.0D - minZ;
				break;
			case WEST:
				rMinX = minZ;
				rMaxX = maxZ;
				rMinZ = 1.0D - maxX;
				rMaxZ = 1.0D - minX;
				break;
			case NORTH:
			default:
				break;
		}

		renderQuad(x, y, z, rMinX, rMaxX, rMinZ, rMaxZ, color);
	}

	private void renderQuad(double x, double y, double z, double minX, double maxX, double minZ, double maxZ, int color) {
		float r = ((color >> 16) & 0xFF) / 255.0F;
		float g = ((color >> 8) & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;
		double insetMinX = minX + LIQUID_INSET;
		double insetMinZ = minZ + LIQUID_INSET;
		double insetMaxX = maxX - LIQUID_INSET;
		double insetMaxZ = maxZ - LIQUID_INSET;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		ITextureObject texture = getLiquidTexture();
		texture.setBlurMipmap(false, false);
		bindTexture(LIQUID_TEXTURE);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos(insetMinX, CUP_Y, insetMaxZ).tex(0.0D, LIQUID_UV_MAX).color(r, g, b, 0.95F).endVertex();
		buffer.pos(insetMaxX, CUP_Y, insetMaxZ).tex(LIQUID_UV_MAX, LIQUID_UV_MAX).color(r, g, b, 0.95F).endVertex();
		buffer.pos(insetMaxX, CUP_Y, insetMinZ).tex(LIQUID_UV_MAX, 0.0D).color(r, g, b, 0.95F).endVertex();
		buffer.pos(insetMinX, CUP_Y, insetMinZ).tex(0.0D, 0.0D).color(r, g, b, 0.95F).endVertex();
		tessellator.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		texture.restoreLastBlurMipmap();
		GlStateManager.popMatrix();
	}

	private static ITextureObject getLiquidTexture() {
		if (liquidTextureObject == null) {
			liquidTextureObject = new SimpleTexture(LIQUID_TEXTURE);
			Minecraft.getMinecraft().getTextureManager().loadTexture(LIQUID_TEXTURE, liquidTextureObject);
		}
		return liquidTextureObject;
	}
}
