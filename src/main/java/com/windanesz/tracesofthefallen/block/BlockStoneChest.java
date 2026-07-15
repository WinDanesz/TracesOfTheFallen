package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockStoneChest extends BlockContainer {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	protected AxisAlignedBB boundingBox = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 0.7D, 0.75D);

	public BlockStoneChest() {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setHardness(2.5F);
		this.setResistance(10.0F);
		this.setSoundType(SoundType.STONE);
	}

	public BlockStoneChest setBoundingBox(AxisAlignedBB boundingBox) {
		this.boundingBox = boundingBox;
		return this;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return rotateBoundingBox(this.boundingBox, state.getValue(FACING));
	}

	private AxisAlignedBB rotateBoundingBox(AxisAlignedBB box, EnumFacing facing) {
		switch (facing) {
			case EAST:
				return new AxisAlignedBB(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX);
			case SOUTH:
				return new AxisAlignedBB(1.0D - box.maxX, box.minY, 1.0D - box.maxZ, 1.0D - box.minX, box.maxY, 1.0D - box.minZ);
			case WEST:
				return new AxisAlignedBB(box.minZ, box.minY, 1.0D - box.maxX, box.maxZ, box.maxY, 1.0D - box.minX);
			case NORTH:
			default:
				return box;
		}
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
		return this.getDefaultState().withProperty(FACING, facing);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityStoneChest();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityStoneChest) {
				playerIn.displayGUIChest((TileEntityStoneChest) te);
			}
		}
		return true;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityStoneChest) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityStoneChest) tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(worldIn, pos, state);
	}
}
