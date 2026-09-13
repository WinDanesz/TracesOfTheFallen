package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.block.BlockSkeletonCorpse;
import com.windanesz.tracesofthefallen.block.TileEntitySkeletonCorpse;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws the skeleton corpse body (ground or sit model, at the tile entity's fine rotation) and the
 * held item next to its arm. The block itself is {@code EnumBlockRenderType.INVISIBLE} - blockstate
 * model rotation only supports 90-degree steps, so all orientation is done here with GlStateManager
 * instead, the same way vanilla renders skulls/banners.
 */
@SideOnly(Side.CLIENT)
public class TileEntitySkeletonCorpseRenderer extends TileEntitySpecialRenderer<TileEntitySkeletonCorpse> {

	// Arm-relative offsets for the held item, in block-local (0-1) space. Rough first pass - tune visually.
	private static final double[] GROUND_ITEM_OFFSET = {1.15D, 0.12D - 1.2D / 16.0D, 0.28D};
	// Lower (hand/lap height, not chest height) and further out past the arm, so it doesn't render
	// through the ribs - also laid flat (see the rotate call below) instead of standing upright.
	private static final double[] SIT_ITEM_OFFSET = {0.95D, 0.025D, 0.75D - 0.325D};

	// Item render scale, as a fraction of a full block: base size per item shape, bumped up 33%.
	private static final float ITEM_SCALE_3D = 0.4F * 1.33F;
	private static final float ITEM_SCALE_FLAT = 0.5F * 1.33F;

	// Cubic (full-block) items only: nudged back down slightly after the half-height lift below -
	// that lift alone sits them a bit high.
	private static final double CUBIC_ITEM_EXTRA_DROP = 2.3D / 16.0D;

	// The two models aren't authored centered on a single 1x1 block (the ground pose in particular is a
	// full 2 blocks long, legs-to-head), so the rotation pivot has to use each model's actual geometric
	// center (in local 0-1 units) rather than the naive (0.5, 0.5) block-center assumption - otherwise the
	// body renders lopsided, hanging almost entirely into the neighboring block instead of straddling the
	// placed one.
	private static final double GROUND_CENTER_X = 0.5D;
	// Centering on the midpoint of the FULL z-range (-16 to 16 -> 0.0) puts the torso off-center, because
	// the legs (z -16 to -4, 12 units) and head (z 8 to 16, 8 units) aren't the same length - that 4-unit
	// difference skews the whole-body midpoint away from the torso itself. Center on the torso/Body
	// element's own span instead (z -4 to 8, midpoint 2 -> 2/16 block) so the torso sits mid-AABB.
	private double GROUND_CENTER_Z = 8.0D / 16.0D;
	private static final double SIT_CENTER_X = 0.5D;
	// Naively averaging the sit model's raw element bounds gives z=0.446, but that renders a full block
	// off (confirmed against a marker block: facing=north lands 1 block east, facing=east lands 1 block
	// south - both solve to the same exact 1-block/16-unit correction). Whatever the model's own local
	// origin convention is, this is the value that actually lines the mesh up with the block.
	private static final double SIT_CENTER_Z = 0.446D - 1.0D;

	// GlStateManager.rotate(angle, 0, 1, 0) turns counter-clockwise (viewed from above) as angle increases
	// in this codebase's convention (see TileEntityWoodenItemFrameRenderer's facing switch); both models
	// were authored facing 90 degrees clockwise from that assumption, so both need the same correction.
	private static final float GROUND_YAW_CORRECTION = -90.0F;
	private static final float SIT_YAW_CORRECTION = -90.0F;

	@Override
	public void render(TileEntitySkeletonCorpse te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		if (!(state.getBlock() instanceof BlockSkeletonCorpse)) {
			return;
		}

		BlockSkeletonCorpse.Pose pose = state.getValue(BlockSkeletonCorpse.POSE);
		EnumFacing facing = state.getValue(BlockSkeletonCorpse.FACING);
		BlockSkeletonCorpse block = (BlockSkeletonCorpse) state.getBlock();

		GlStateManager.pushMatrix();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);

		float yaw;
		float correction;
		IBlockState renderState;
		double centerX;
		double centerZ;
		if (pose == BlockSkeletonCorpse.Pose.SIT) {
			renderState = block.getDefaultState().withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.SIT);
			switch (facing) {
				case SOUTH: yaw = 180.0F; break;
				case EAST: yaw = 270.0F; break;
				case WEST: yaw = 90.0F; break;
				case NORTH:
				default: yaw = 0.0F; break;
			}
			correction = SIT_YAW_CORRECTION;
			centerX = SIT_CENTER_X;
			centerZ = SIT_CENTER_Z;
		} else {
			renderState = block.getDefaultState().withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.GROUND);
			yaw = -22.5F * te.getRotation();
			correction = GROUND_YAW_CORRECTION;
			centerX = GROUND_CENTER_X - 0.4D;
			centerZ = GROUND_CENTER_Z - 1.0D;
		}

		GlStateManager.rotate(yaw + correction, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-centerX, 0.0D, -centerZ);

		// Not renderBlockBrightness() - it no-ops unless state.getRenderType() == MODEL, and this block
		// reports INVISIBLE (on purpose, see class javadoc). Call the lower-level renderer directly instead.
		IBakedModel bodyModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(renderState);
		BlockModelRenderer modelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		GlStateManager.disableLighting();
		modelRenderer.renderModelBrightness(bodyModel, renderState, 1.0F, true);
		GlStateManager.enableLighting();

		ItemStack held = te.getHeldItem();
		if (!held.isEmpty()) {
			double[] offset = pose == BlockSkeletonCorpse.Pose.SIT ? SIT_ITEM_OFFSET : GROUND_ITEM_OFFSET;

			IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(held, te.getWorld(), null);
			boolean is3D = model != null && model.isGui3d();
			float scale = is3D ? ITEM_SCALE_3D : ITEM_SCALE_FLAT;

			GlStateManager.pushMatrix();
			GlStateManager.translate(offset[0], offset[1], offset[2]);
			if (is3D) {
				// Full 3D items (tools, blocks) render centered on their own origin, so at this offset's
				// height a cube would stick out half above and half below it - lift by half its (scaled)
				// height first, in world-vertical terms, before any pose-specific tilt below.
				GlStateManager.translate(0.0D, 0.5D * scale - CUBIC_ITEM_EXTRA_DROP, 0.0D);
			}
			if (pose == BlockSkeletonCorpse.Pose.SIT) {
				// Spin it 180 to face the other way, then lay it flat (resting on the lap/floor beside
				// the hand) instead of standing upright. Order matters: the spin is applied while the
				// item is still "standing", before the tilt turns it onto its side.
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			} else {
				// Lying down next to the arm on the ground, not standing upright/sticking into the floor.
				GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
				GlStateManager.translate(0.0D, 0.6D, 0.0D);
			}
			GlStateManager.scale(scale, scale, scale);

			RenderHelper.enableStandardItemLighting();
			Minecraft.getMinecraft().getRenderItem().renderItem(held, ItemCameraTransforms.TransformType.FIXED);
			RenderHelper.disableStandardItemLighting();

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}
}
