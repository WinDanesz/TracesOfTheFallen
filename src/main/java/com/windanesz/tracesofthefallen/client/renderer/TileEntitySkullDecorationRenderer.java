package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.block.BlockSkullDecoration;
import com.windanesz.tracesofthefallen.tile.TileEntitySkullDecoration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws the skull at the tile entity's fine (16-way) rotation when floor-placed, or at a fixed
 * orientation flush to the wall it's mounted on. The block itself is
 * {@code EnumBlockRenderType.INVISIBLE} - blockstate model rotation only supports 90-degree steps -
 * so orientation is done here with GlStateManager instead, the same way vanilla renders skulls/banners
 * (see also {@link TileEntitySkeletonCorpseRenderer} and {@link TileEntityWoodenItemFrameRenderer},
 * whose facing/yaw mapping this reuses).
 */
@SideOnly(Side.CLIENT)
public class TileEntitySkullDecorationRenderer extends TileEntitySpecialRenderer<TileEntitySkullDecoration> {

	@Override
	public void render(TileEntitySkullDecoration te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		if (!(state.getBlock() instanceof BlockSkullDecoration)) {
			return;
		}

		EnumFacing facing = state.getValue(BlockSkullDecoration.FACING);

		GlStateManager.pushMatrix();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);

		// The model box only spans local Y 0-0.5 and is horizontally centered (0.25-0.75 on both X and
		// Z) - correct for sitting on the floor, but wall mounting needs it lifted to mid-height and
		// pushed flush against the wall. Both the lift and the push are expressed here in the model's
		// own pre-rotation local space (applied below, after the rotate call) so the same single offset
		// works for all four wall facings - the rotate call reorients the push direction to match
		// whichever wall the block is actually facing. This has to mirror BlockSkullDecoration's
		// WALL_AABB_* boxes exactly, or the model renders offset from its hitbox.
		float yaw;
		double offsetY;
		double offsetZ;
		switch (facing) {
			case SOUTH:
				yaw = 180.0F;
				offsetY = 0.25D;
				offsetZ = -0.25D;
				break;
			case EAST:
				yaw = 270.0F;
				offsetY = 0.25D;
				offsetZ = -0.25D;
				break;
			case WEST:
				yaw = 90.0F;
				offsetY = 0.25D;
				offsetZ = -0.25D;
				break;
			case NORTH:
				yaw = 0.0F;
				offsetY = 0.25D;
				offsetZ = -0.25D;
				break;
			case UP:
			default:
				yaw = -22.5F * te.getRotation();
				offsetY = 0.0D;
				offsetZ = -0.5D;
				break;
		}

		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, offsetY, offsetZ);
		// renderModelBrightness bakes in its own 90-degree Y rotation about the origin (meant for
		// inventory-style rendering), which would shift the model by a block - cancel it out.
		GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);

		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		BlockModelRenderer modelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		GlStateManager.disableLighting();
		modelRenderer.renderModelBrightness(model, state, 1.0F, true);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
}
