package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFloater extends Block {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection VERTICAL_FACING = PropertyDirection.create("vertical_facing", facing -> facing == EnumFacing.UP || facing == EnumFacing.DOWN);
	// We'll just use a single PropertyDirection that can be all 6 directions.
	public static final PropertyDirection DIRECTION = PropertyDirection.create("direction");

	private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);
	private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.125D, 0.75D, 0.125D, 0.875D, 1.0D, 0.875D);
	private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.125D, 0.125D, 0.75D, 0.875D, 0.875D, 1.0D);
	private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.125D, 0.125D, 0.0D, 0.875D, 0.875D, 0.25D);
	private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.75D, 0.125D, 0.125D, 1.0D, 0.875D, 0.875D);
	private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.125D, 0.125D, 0.25D, 0.875D, 0.875D);

	public BlockFloater(Material materialIn) {
		super(materialIn);
		this.setDefaultState(this.blockState.getBaseState().withProperty(DIRECTION, EnumFacing.UP));
		this.setHardness(0.5F);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing facing = state.getValue(DIRECTION);
		switch (facing) {
			case DOWN:
				return AABB_DOWN;
			case NORTH:
				return AABB_NORTH;
			case SOUTH:
				return AABB_SOUTH;
			case WEST:
				return AABB_WEST;
			case EAST:
				return AABB_EAST;
			case UP:
			default:
				return AABB_UP;
		}
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(DIRECTION, facing);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, DIRECTION);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DIRECTION).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.byIndex(meta);
		return this.getDefaultState().withProperty(DIRECTION, facing);
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing enumfacing : EnumFacing.values()) {
			if (this.canBlockStay(worldIn, pos, enumfacing)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		return this.canBlockStay(worldIn, pos, side);
	}

	private boolean canBlockStay(World worldIn, BlockPos pos, EnumFacing facing) {
		BlockPos supportingPos = pos.offset(facing.getOpposite());
		IBlockState supportingState = worldIn.getBlockState(supportingPos);
		return supportingState.getBlockFaceShape(worldIn, supportingPos, facing) == net.minecraft.block.state.BlockFaceShape.SOLID || supportingState.isSideSolid(worldIn, supportingPos, facing);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(worldIn, pos, state.getValue(DIRECTION))) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
	}
}
