package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

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
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityDreamcatcher();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("tile.totf:dreamcatcher.desc"));
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.DARK_GRAY + I18n.format("tile.totf:dreamcatcher.desc2"));
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos) && this.canBlockStay(worldIn, pos);
	}

	private boolean canBlockStay(World worldIn, BlockPos pos) {
		IBlockState upState = worldIn.getBlockState(pos.up());
		return upState.isSideSolid(worldIn, pos.up(), EnumFacing.DOWN) || upState.getBlock().isLeaves(upState, worldIn, pos.up()) || upState.isOpaqueCube();
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
	}
}
