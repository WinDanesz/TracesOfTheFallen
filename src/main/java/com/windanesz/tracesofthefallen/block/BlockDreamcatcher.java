package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockDreamcatcher extends BlockDecoration {

	private static final AxisAlignedBB AABB_NORTH_SOUTH = new AxisAlignedBB(0.0D, -1.0D, 0.4375D, 1.0D, 1.0D, 0.5625D);
	private static final AxisAlignedBB AABB_EAST_WEST = new AxisAlignedBB(0.4375D, -1.0D, 0.0D, 0.5625D, 1.0D, 1.0D);

	public BlockDreamcatcher(Material material) {
		super(material);
		setHardness(0.5F);
		setResistance(1.0F);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing facing = state.getValue(FACING);
		if (facing.getAxis() == EnumFacing.Axis.X) {
			return AABB_EAST_WEST;
		}
		return AABB_NORTH_SOUTH;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public net.minecraft.tileentity.TileEntity createTileEntity(net.minecraft.world.World world, IBlockState state) {
		return new TileEntityDreamcatcher();
	}

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, @Nullable net.minecraft.world.World worldIn, java.util.List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		com.windanesz.tracesofthefallen.client.ClientProxy.addMultiLineDescription(tooltip, net.minecraft.util.text.TextFormatting.BOLD + "" + net.minecraft.util.text.TextFormatting.GRAY + net.minecraft.client.resources.I18n.format("tile.totf:dreamcatcher.desc"));
		com.windanesz.tracesofthefallen.client.ClientProxy.addMultiLineDescription(tooltip, net.minecraft.util.text.TextFormatting.DARK_GRAY + net.minecraft.client.resources.I18n.format("tile.totf:dreamcatcher.desc2"));
	}

	@Override
	public boolean canPlaceBlockAt(net.minecraft.world.World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos) && this.canBlockStay(worldIn, pos);
	}

	private boolean canBlockStay(net.minecraft.world.World worldIn, BlockPos pos) {
		IBlockState upState = worldIn.getBlockState(pos.up());
		return upState.isSideSolid(worldIn, pos.up(), EnumFacing.DOWN) || upState.getBlock().isLeaves(upState, worldIn, pos.up()) || upState.isOpaqueCube();
	}

	@Override
	public void neighborChanged(IBlockState state, net.minecraft.world.World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
	}
}
