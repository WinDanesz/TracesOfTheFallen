package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockIncense extends BlockDecoration {

	public static final int MAX_ACTIVE_BURN_STAGE = 7;
	public static final int BURNT_OUT_STAGE = 8;
	public static final PropertyInteger BURN_STAGE = PropertyInteger.create("burn_stage", 0, BURNT_OUT_STAGE);

	public BlockIncense(Material material) {
		super(material);
		setDefaultState(this.blockState.getBaseState()
				.withProperty(FACING, EnumFacing.NORTH)
				.withProperty(BURN_STAGE, 0));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityIncense();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
				.withProperty(BURN_STAGE, 0);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityIncense incense = getIncense(world, pos);
		if (incense != null) {
			return state.withProperty(BURN_STAGE, incense.getBurnStage());
		}

		return state;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack heldItem = playerIn.getHeldItem(hand);
		TileEntityIncense incense = getIncense(worldIn, pos);

		if (incense == null || incense.isLit() || incense.isBurnedOut() || !IncenseEffects.isFireStarter(heldItem)) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		if (!incense.tryLight()) {
			return false;
		}

		worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
				worldIn.rand.nextFloat() * 0.4F + 0.8F);

		if (!playerIn.capabilities.isCreativeMode) {
			if (heldItem.getItem() == Items.FIRE_CHARGE) {
				heldItem.shrink(1);
			} else if (heldItem.isItemStackDamageable()) {
				heldItem.damageItem(1, playerIn);
			} else {
				heldItem.shrink(1);
			}
		}

		return true;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		TileEntityIncense incense = getIncense(world, pos);
		if (incense != null && incense.isBurnedOut()) {
			return;
		}

		Item item = Item.getItemFromBlock(this);
		if (item != null) {
			drops.add(new ItemStack(item));
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, BURN_STAGE);
	}

	@Nullable
	private TileEntityIncense getIncense(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityIncense ? (TileEntityIncense) tileEntity : null;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
			case EAST:
				return rotateClockwise(this.boundingBox);
			case SOUTH:
				return rotateHalfTurn(this.boundingBox);
			case WEST:
				return rotateCounterClockwise(this.boundingBox);
			case NORTH:
			default:
				return this.boundingBox;
		}
	}

	private AxisAlignedBB rotateClockwise(AxisAlignedBB box) {
		return new AxisAlignedBB(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX);
	}

	private AxisAlignedBB rotateHalfTurn(AxisAlignedBB box) {
		return new AxisAlignedBB(1.0D - box.maxX, box.minY, 1.0D - box.maxZ, 1.0D - box.minX, box.maxY, 1.0D - box.minZ);
	}

	private AxisAlignedBB rotateCounterClockwise(AxisAlignedBB box) {
		return new AxisAlignedBB(box.minZ, box.minY, 1.0D - box.maxX, box.maxZ, box.maxY, 1.0D - box.minX);
	}
}
