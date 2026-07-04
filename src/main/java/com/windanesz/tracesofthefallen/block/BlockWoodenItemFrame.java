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
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockWoodenItemFrame extends BlockContainer {

	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0D, 0.25D, 0.9375D, 1.0D, 0.75D, 1.0D);
	protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 0.75D, 0.0625D);
	protected static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 0.0625D, 0.75D, 1.0D);
	protected static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.9375D, 0.25D, 0.0D, 1.0D, 0.75D, 1.0D);
	protected static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 0.0625D, 0.75D);
	protected static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.0D, 0.9375D, 0.25D, 1.0D, 1.0D, 0.75D);

	public BlockWoodenItemFrame() {
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setHardness(0.5F);
		this.setSoundType(SoundType.WOOD);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
			case NORTH: return AABB_NORTH;
			case SOUTH: return AABB_SOUTH;
			case EAST: return AABB_EAST;
			case WEST: return AABB_WEST;
			case UP: return AABB_UP;
			case DOWN: return AABB_DOWN;
			default: return AABB_NORTH;
		}
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
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
		return this.getDefaultState().withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityWoodenItemFrame();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityWoodenItemFrame) {
				TileEntityWoodenItemFrame frame = (TileEntityWoodenItemFrame) te;
				ItemStack held = playerIn.getHeldItem(hand);

				if (playerIn.isSneaking() && !frame.getDisplayedItem().isEmpty()) {
					spawnAsEntity(worldIn, pos, frame.getDisplayedItem().copy());
					frame.setDisplayedItem(ItemStack.EMPTY);
					frame.setItemRotation(0);
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
					worldIn.updateComparatorOutputLevel(pos, this);
					return true;
				}

				if (frame.getDisplayedItem().isEmpty() && !held.isEmpty()) {
					ItemStack displayStack = held.copy();
					displayStack.setCount(1);
					frame.setDisplayedItem(displayStack);
					frame.setItemRotation(0);
					if (!playerIn.capabilities.isCreativeMode) {
						held.shrink(1);
					}
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
					worldIn.updateComparatorOutputLevel(pos, this);
					return true;
				} else if (!frame.getDisplayedItem().isEmpty()) {
					frame.setItemRotation((frame.getItemRotation() + 1) % 8);
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ROTATE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
					worldIn.updateComparatorOutputLevel(pos, this);
					return true;
				}
			}
		}
		return true;
	}

	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityWoodenItemFrame) {
				TileEntityWoodenItemFrame frame = (TileEntityWoodenItemFrame) te;
				if (!frame.getDisplayedItem().isEmpty()) {
					spawnAsEntity(worldIn, pos, frame.getDisplayedItem().copy());
					frame.setDisplayedItem(ItemStack.EMPTY);
					frame.setItemRotation(0);
					worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
					worldIn.updateComparatorOutputLevel(pos, this);
					return;
				}
			}
		}
		super.onBlockClicked(worldIn, pos, playerIn);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityWoodenItemFrame) {
				TileEntityWoodenItemFrame frame = (TileEntityWoodenItemFrame) te;
				if (!frame.getDisplayedItem().isEmpty()) {
					spawnAsEntity(worldIn, pos, frame.getDisplayedItem().copy());
				}
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityWoodenItemFrame) {
			TileEntityWoodenItemFrame frame = (TileEntityWoodenItemFrame) te;
			if (!frame.getDisplayedItem().isEmpty()) {
				return frame.getItemRotation() + 1;
			}
		}
		return 0;
	}
}
