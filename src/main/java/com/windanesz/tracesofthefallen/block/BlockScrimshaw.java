package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The Scrimshaw model is asymmetrical and extends above one block, so its
 * selection and collision boxes track its horizontal model rotation.
 */
public class BlockScrimshaw extends BlockDecoration {

	private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(
			0.1845209775D, 0.0D, 0.250993125D,
			0.750993125D, 1.3321824078D, 0.750993125D);
	private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(
			0.249006875D, 0.0D, 0.1845209775D,
			0.749006875D, 1.3321824078D, 0.750993125D);
	private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(
			0.249006875D, 0.0D, 0.249006875D,
			0.8154790225D, 1.3321824078D, 0.749006875D);
	private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(
			0.250993125D, 0.0D, 0.249006875D,
			0.750993125D, 1.3321824078D, 0.8154790225D);

	public BlockScrimshaw() {
		super(Material.ROCK);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
			case EAST:
				return EAST_AABB;
			case SOUTH:
				return SOUTH_AABB;
			case WEST:
				return WEST_AABB;
			case NORTH:
			default:
				return NORTH_AABB;
		}
	}
}
