package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBalance extends BlockDecoration {

	private static final AxisAlignedBB BALANCE_AABB_NS = new AxisAlignedBB(0.1D, 0.0D, 0.1875D, 0.9D, 1.1D, 0.8125D);
	private static final AxisAlignedBB BALANCE_AABB_EW = new AxisAlignedBB(0.1875D, 0.0D, 0.1D, 0.8125D, 1.1D, 0.9D);

	public BlockBalance(Material material) {
		super(material);
		setHardness(1.5F);
		setResistance(5.0F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityBalance();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return getFacingAwareBoundingBox(state);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return getFacingAwareBoundingBox(state);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntityBalance balance = getBalance(worldIn, pos);
		if (balance != null) {
			balance.setMode(BalanceMode.fromStack(stack));
			balance.setOwnerName(placer.getName());
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}

		TileEntityBalance balance = getBalance(worldIn, pos);
		if (balance == null) {
			return true;
		}

		if (worldIn.isRemote) {
			return true;
		}

		boolean rightSide = isRightSide(state.getValue(FACING), hitX, hitZ);
		if (balance.getMode() == BalanceMode.VENDING) {
			handleVendingInteraction(playerIn, hand, balance, rightSide);
		} else {
			handleRedstoneInteraction(playerIn, hand, balance, rightSide);
		}

		return true;
	}

	private void handleVendingInteraction(EntityPlayer player, EnumHand hand, TileEntityBalance balance, boolean rightSide) {
		ItemStack heldStack = player.getHeldItem(hand);

		if (rightSide) {
			if (heldStack.isEmpty()) {
				if (!balance.getRightStack().isEmpty()) {
					balance.setRightStack(ItemStack.EMPTY);
				}
				return;
			}

			ItemStack currentPrice = balance.getRightStack();
			if (!balance.getLeftStack().isEmpty() && !currentPrice.isEmpty() && canPay(heldStack, currentPrice)) {
				if (!player.capabilities.isCreativeMode) {
					heldStack.shrink(currentPrice.getCount());
				}
				giveOrDrop(player, balance.getLeftStack().copy());
				balance.setLeftStack(ItemStack.EMPTY);
			} else if (currentPrice.isEmpty()) {
				balance.setRightStack(copySingle(heldStack));
			} else if (canStacksMerge(currentPrice, heldStack) && currentPrice.getCount() < currentPrice.getMaxStackSize()) {
				ItemStack updatedPrice = currentPrice.copy();
				updatedPrice.grow(1);
				balance.setRightStack(updatedPrice);
			} else {
				balance.setRightStack(copySingle(heldStack));
			}
			return;
		}

		ItemStack leftStack = balance.getLeftStack();
		if (heldStack.isEmpty()) {
			if (!leftStack.isEmpty()) {
				giveOrDrop(player, leftStack.copy());
				balance.setLeftStack(ItemStack.EMPTY);
			}
			return;
		}

		if (leftStack.isEmpty()) {
			if (!heldStack.isEmpty()) {
				balance.setLeftStack(copySingle(heldStack));
				consumeHeld(player, hand, 1);
			}
			return;
		}

		if (canStacksMerge(leftStack, heldStack) && leftStack.getCount() < leftStack.getMaxStackSize()) {
			ItemStack updatedOffer = leftStack.copy();
			updatedOffer.grow(1);
			balance.setLeftStack(updatedOffer);
			consumeHeld(player, hand, 1);
			return;
		}

		ItemStack previousOffer = leftStack.copy();
		balance.setLeftStack(copySingle(heldStack));
		if (!player.capabilities.isCreativeMode) {
			heldStack.shrink(1);
			giveOrDrop(player, previousOffer);
		}
	}

	private void handleRedstoneInteraction(EntityPlayer player, EnumHand hand, TileEntityBalance balance, boolean rightSide) {
		ItemStack heldStack = player.getHeldItem(hand);
		ItemStack currentStack = rightSide ? balance.getRightStack() : balance.getLeftStack();

		if (heldStack.isEmpty()) {
			if (!currentStack.isEmpty()) {
				giveOrDrop(player, currentStack.copy());
				setMeasuredStack(balance, rightSide, ItemStack.EMPTY);
			}
			return;
		}

		if (currentStack.isEmpty()) {
			int transferCount = Math.min(heldStack.getCount(), heldStack.getMaxStackSize());
			ItemStack inserted = heldStack.copy();
			inserted.setCount(transferCount);
			setMeasuredStack(balance, rightSide, inserted);
			consumeHeld(player, hand, transferCount);
			return;
		}

		if (canStacksMerge(currentStack, heldStack) && currentStack.getCount() < currentStack.getMaxStackSize()) {
			int space = currentStack.getMaxStackSize() - currentStack.getCount();
			int transferCount = Math.min(space, heldStack.getCount());
			if (transferCount > 0) {
				ItemStack updated = currentStack.copy();
				updated.grow(transferCount);
				setMeasuredStack(balance, rightSide, updated);
				consumeHeld(player, hand, transferCount);
			}
		}
	}

	private void setMeasuredStack(TileEntityBalance balance, boolean rightSide, ItemStack stack) {
		if (rightSide) {
			balance.setRightStack(stack);
		} else {
			balance.setLeftStack(stack);
		}
	}

	private static boolean canPay(ItemStack offered, ItemStack price) {
		return !offered.isEmpty() && canStacksMerge(offered, price) && offered.getCount() >= price.getCount();
	}

	private static boolean canStacksMerge(ItemStack first, ItemStack second) {
		return ItemStack.areItemsEqual(first, second) && ItemStack.areItemStackTagsEqual(first, second);
	}

	private static ItemStack copySingle(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.setCount(1);
		return copy;
	}

	private static void consumeHeld(EntityPlayer player, EnumHand hand, int amount) {
		if (!player.capabilities.isCreativeMode) {
			player.getHeldItem(hand).shrink(amount);
		}
	}

	private static void giveOrDrop(EntityPlayer player, ItemStack stack) {
		if (!player.addItemStackToInventory(stack)) {
			player.dropItem(stack, false);
		}
	}

	private static boolean isRightSide(EnumFacing blockFacing, float hitX, float hitZ) {
		double lateral;
		switch (blockFacing) {
			case EAST:
				lateral = hitZ - 0.5D;
				break;
			case SOUTH:
				lateral = 0.5D - hitX;
				break;
			case WEST:
				lateral = 0.5D - hitZ;
				break;
			case NORTH:
			default:
				lateral = hitX - 0.5D;
				break;
		}
		return lateral > 0.0D;
	}

	private static AxisAlignedBB getFacingAwareBoundingBox(IBlockState state) {
		EnumFacing facing = state.getValue(FACING);
		return facing == EnumFacing.EAST || facing == EnumFacing.WEST ? BALANCE_AABB_EW : BALANCE_AABB_NS;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			TileEntityBalance balance = getBalance(worldIn, pos);
			if (balance != null) {
				if (!balance.getLeftStack().isEmpty()) {
					spawnAsEntity(worldIn, pos, balance.getLeftStack().copy());
				}
				if (balance.getMode() == BalanceMode.REDSTONE && !balance.getRightStack().isEmpty()) {
					spawnAsEntity(worldIn, pos, balance.getRightStack().copy());
				}
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ItemStack droppedBlock = new ItemStack(Item.getItemFromBlock(this));
		TileEntityBalance balance = getBalance(world, pos);
		BalanceMode.applyToStack(droppedBlock, balance == null ? BalanceMode.DEFAULT : balance.getMode());
		drops.add(droppedBlock);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
		TileEntityBalance balance = getBalance(world, pos);
		BalanceMode.applyToStack(stack, balance == null ? BalanceMode.DEFAULT : balance.getMode());
		return stack;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		TileEntityBalance balance = getBalance(blockAccess, pos);
		return balance == null ? 0 : balance.getSignalForSide(side, blockState.getValue(FACING));
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntityBalance balance = getBalance(worldIn, pos);
		if (balance == null || balance.getMode() != BalanceMode.REDSTONE) {
			return 0;
		}
		return balance.getComparatorStrength();
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		TileEntityBalance balance = getBalance(world, pos);
		return side != null && balance != null && balance.getMode() == BalanceMode.REDSTONE;
	}

	@Nullable
	private static TileEntityBalance getBalance(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityBalance ? (TileEntityBalance) tileEntity : null;
	}
}
