package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockGlassFloat;
import com.windanesz.tracesofthefallen.block.TileEntityGlassFloat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class TileEntityGlassFloatRenderer extends TileEntitySpecialRenderer<TileEntityGlassFloat> {

	@Override
	public void render(TileEntityGlassFloat te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		if (!(state.getBlock() instanceof BlockGlassFloat)) {
			return;
		}

		BlockGlassFloat.FloatVariant variant = ((BlockGlassFloat) state.getBlock()).getVariant();
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
				.getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":" + variant.getModelName(), "inventory"));
		if (model == null) {
			return;
		}

		int combinedLight = te.getWorld().getCombinedLight(te.getPos(), 0);
		int blockLight = combinedLight & 65535;
		int skyLight = combinedLight >> 16;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D + te.getInterpolatedBobOffset(partialTicks), z + 0.5D);
		GlStateManager.rotate(getFacingAngle(state.getValue(BlockGlassFloat.FACING)), 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(te.getInterpolatedPitch(partialTicks), 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(te.getInterpolatedRoll(partialTicks), 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	private static float getFacingAngle(EnumFacing facing) {
		switch (facing) {
			case EAST:
				return 270.0F;
			case SOUTH:
				return 180.0F;
			case WEST:
				return 90.0F;
			case NORTH:
			default:
				return 0.0F;
		}
	}
}
