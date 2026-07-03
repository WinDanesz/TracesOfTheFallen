package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPorcelainSet extends BlockDecoration {

	public static final PropertyEnum<Arrangement> ARRANGEMENT = PropertyEnum.create("arrangement", Arrangement.class);
	public static final PropertyInteger FILLED_CUPS = PropertyInteger.create("filled_cups", 0, 2);

	public BlockPorcelainSet(Material material) {
		super(material);
		setDefaultState(this.blockState.getBaseState()
				.withProperty(FACING, EnumFacing.NORTH)
				.withProperty(ARRANGEMENT, Arrangement.FULL)
				.withProperty(FILLED_CUPS, 0));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BlockPorcelainPiece.rotateBoundingBox(state.getValue(ARRANGEMENT).getNorthBoundingBox(), state.getValue(FACING));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return getStateFromMeta(meta).withProperty(FACING, BlockPorcelainPiece.getPlacementFacing(placer));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(ARRANGEMENT).getMetadata() << 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
				.withProperty(ARRANGEMENT, Arrangement.byMetadata(meta >> 2))
				.withProperty(FILLED_CUPS, 0);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(FILLED_CUPS, 0);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Arrangement arrangement = state.getValue(ARRANGEMENT);
		TileEntityPorcelainSet set = getSet(world, pos);
		if (set == null) {
			for (int i = 0; i < arrangement.getCupCount(); i++) {
				drops.add(new ItemStack(Item.getItemFromBlock(ModBlocks.porcelain_cup)));
			}
		} else {
			for (int slot : arrangement.getCupSlots()) {
				String content = set.getCupContent(slot);
				drops.add(content.isEmpty()
						? new ItemStack(Item.getItemFromBlock(ModBlocks.porcelain_cup))
						: TileEntityPorcelainVessel.createCupStack(content));
			}
		}

		for (int i = 0; i < arrangement.getPotCount(); i++) {
			drops.add(new ItemStack(Item.getItemFromBlock(ModBlocks.porcelain_pot)));
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityPorcelainSet();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hand == EnumHand.OFF_HAND) {
			return false;
		}

		ItemStack heldStack = playerIn.getHeldItem(hand);
		Arrangement arrangement = state.getValue(ARRANGEMENT);
		TileEntityPorcelainSet set = getSet(worldIn, pos);
		if (set == null) {
			return false;
		}

		float localX = Arrangement.getLocalX(state.getValue(FACING), hitX, hitZ);
		float localZ = Arrangement.getLocalZ(state.getValue(FACING), hitX, hitZ);

		if (TileEntityPorcelainVessel.canStoredPotPourLiquid(heldStack)) {
			int cupSlot = arrangement.getCupSlotForHit(localX, localZ, set);
			if (cupSlot >= 0 && set.getCupContent(cupSlot).isEmpty()) {
				if (!worldIn.isRemote) {
					String liquidType = TileEntityPorcelainVessel.getStoredBrewLiquidType(heldStack);
					if (TileEntityPorcelainVessel.pourLiquidFromStoredPot(heldStack)) {
						set.setCupContent(cupSlot, liquidType);
						worldIn.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.7F, 1.2F);
					}
				}
				return true;
			}
		}

		if (heldStack.isEmpty()) {
			return handleEmptyHandInteraction(worldIn, pos, state, playerIn, arrangement, localX, set);
		}

		if (!arrangement.canUpgradeWith(heldStack)) {
			return false;
		}

		if (!worldIn.isRemote) {
			worldIn.setBlockState(pos, state.withProperty(ARRANGEMENT, Arrangement.FULL), 3);
			TileEntityPorcelainSet newSet = getSet(worldIn, pos);
			if (newSet != null) {
				newSet.copyFrom(set, Arrangement.FULL);
			}
			if (!playerIn.capabilities.isCreativeMode) {
				heldStack.shrink(1);
			}
		}
		return true;
	}

	private boolean handleEmptyHandInteraction(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, Arrangement arrangement,
			float localX, TileEntityPorcelainSet set) {
		if (worldIn.isRemote) {
			return true;
		}

		RemovalResult removal = arrangement.getRemovalResult(state.getValue(FACING), localX);
		if (removal == null) {
			return false;
		}

		ItemStack removedStack = createRemovedStack(removal, set);
		if (removal.replacementState == null) {
			worldIn.setBlockToAir(pos);
		} else {
			worldIn.setBlockState(pos, removal.replacementState, 3);
			applyReplacementContents(worldIn, pos, removal, set);
		}

		if (!playerIn.capabilities.isCreativeMode) {
			if (!playerIn.inventory.addItemStackToInventory(removedStack)) {
				Block.spawnAsEntity(worldIn, pos, removedStack);
			}
		}

		worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.7F, 1.3F);
		return true;
	}

	private ItemStack createRemovedStack(RemovalResult removal, TileEntityPorcelainSet set) {
		if (removal.removedItem != Item.getItemFromBlock(ModBlocks.porcelain_cup) || removal.removedCupSlot < 0) {
			return new ItemStack(removal.removedItem);
		}

		String content = set.getCupContent(removal.removedCupSlot);
		return content.isEmpty() ? new ItemStack(removal.removedItem) : TileEntityPorcelainVessel.createCupStack(content);
	}

	private void applyReplacementContents(World world, BlockPos pos, RemovalResult removal, TileEntityPorcelainSet previousSet) {
		if (removal.replacementState.getBlock() == ModBlocks.porcelain_set) {
			TileEntityPorcelainSet replacementSet = getSet(world, pos);
			if (replacementSet != null) {
				replacementSet.applyTransition(previousSet, removal);
			}
		} else if (removal.replacementState.getBlock() == ModBlocks.porcelain_cup && removal.replacementCupSourceSlot >= 0) {
			String remainingContent = previousSet.getCupContent(removal.replacementCupSourceSlot);
			if (!remainingContent.isEmpty()) {
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity instanceof TileEntityPorcelainVessel) {
					TileEntityPorcelainVessel vessel = (TileEntityPorcelainVessel) tileEntity;
					vessel.setCupContent(remainingContent);
					BlockPorcelainPiece.syncFilledState(world, pos, vessel);
				}
			}
		}
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
		IBlockState state = worldIn.getBlockState(pos);
		super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
		if (!BlockPorcelainPiece.shouldBreakFromImpact(worldIn, entityIn, fallDistance)) {
			return;
		}

		TileEntityPorcelainSet set = getSet(worldIn, pos);
		BlockPorcelainPiece.playImpactEffects(worldIn, pos, state);
		IBlockState replacement = state.getValue(ARRANGEMENT).getBreakResultState(state.getValue(FACING), worldIn);
		if (replacement == null) {
			worldIn.setBlockToAir(pos);
			return;
		}

		worldIn.setBlockState(pos, replacement, 3);
		if (set == null) {
			return;
		}

		if (replacement.getBlock() == ModBlocks.porcelain_set) {
			TileEntityPorcelainSet replacementSet = getSet(worldIn, pos);
			if (replacementSet != null) {
				replacementSet.copyFrom(set, replacement.getValue(ARRANGEMENT));
			}
		} else if (replacement.getBlock() == ModBlocks.porcelain_cup) {
			String content = set.getFirstAvailableCupContent();
			if (!content.isEmpty()) {
				TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof TileEntityPorcelainVessel) {
					TileEntityPorcelainVessel vessel = (TileEntityPorcelainVessel) tileEntity;
					vessel.setCupContent(content);
					BlockPorcelainPiece.syncFilledState(worldIn, pos, vessel);
				}
			}
		}
	}

	private int getFilledCupCount(IBlockAccess world, BlockPos pos, Arrangement arrangement) {
		TileEntityPorcelainSet set = getSet(world, pos);
		if (set == null) {
			return 0;
		}
		return set.getFilledCupCount(arrangement);
	}

	@Nullable
	private TileEntityPorcelainSet getSet(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityPorcelainSet ? (TileEntityPorcelainSet) tileEntity : null;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, ARRANGEMENT, FILLED_CUPS);
	}

	public enum Arrangement implements IStringSerializable {
		CUPS(0, "cups", 2, 0, new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.5D, 0.25D, 1.0D)),
		POT_AND_CUP(1, "pot_and_cup", 1, 1, new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 0.4375D, 0.8125D)),
		FULL(2, "full", 2, 1, new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 0.4375D, 1.0D));

		private static final Arrangement[] VALUES = values();

		private final int metadata;
		private final String name;
		private final int cupCount;
		private final int potCount;
		private final AxisAlignedBB northBoundingBox;

		Arrangement(int metadata, String name, int cupCount, int potCount, AxisAlignedBB northBoundingBox) {
			this.metadata = metadata;
			this.name = name;
			this.cupCount = cupCount;
			this.potCount = potCount;
			this.northBoundingBox = northBoundingBox;
		}

		public int getMetadata() {
			return metadata;
		}

		@Override
		public String getName() {
			return name;
		}

		public AxisAlignedBB getNorthBoundingBox() {
			return northBoundingBox;
		}

		int getCupCount() {
			return cupCount;
		}

		private int getPotCount() {
			return potCount;
		}

		int[] getCupSlots() {
			return this == CUPS || this == FULL ? new int[]{0, 1} : this == POT_AND_CUP ? new int[]{0} : new int[0];
		}

		private int getCupSlotForHit(float localX, float localZ, TileEntityPorcelainSet set) {
			switch (this) {
				case CUPS: {
					int target = localZ < 0.5F ? 0 : 1;
					if (!set.getCupContent(target).isEmpty() && set.getCupContent(1 - target).isEmpty()) {
						return 1 - target;
					}
					return target;
				}
				case POT_AND_CUP:
					return localX < 0.5F ? 0 : -1;
				case FULL: {
					if (localX >= 0.5F) {
						return -1;
					}
					int target = localZ < 0.5F ? 0 : 1;
					if (!set.getCupContent(target).isEmpty() && set.getCupContent(1 - target).isEmpty()) {
						return 1 - target;
					}
					return target;
				}
				default:
					return -1;
			}
		}

		private boolean canUpgradeWith(ItemStack stack) {
			return (this == CUPS && TileEntityPorcelainVessel.isPlainPorcelainPot(stack))
					|| (this == POT_AND_CUP && TileEntityPorcelainVessel.isPlainPorcelainCup(stack));
		}

		private RemovalResult getRemovalResult(EnumFacing facing, float localX) {
			switch (this) {
				case CUPS: {
					int removedCupSlot = localX < 0.5F ? 0 : 1;
					int remainingCupSlot = removedCupSlot == 0 ? 1 : 0;
					return new RemovalResult(
							ModBlocks.porcelain_cup.getDefaultState().withProperty(FACING, facing),
							Item.getItemFromBlock(ModBlocks.porcelain_cup),
							removedCupSlot,
							remainingCupSlot
					);
				}
				case POT_AND_CUP:
					return new RemovalResult(
							ModBlocks.porcelain_cup.getDefaultState().withProperty(FACING, facing),
							Item.getItemFromBlock(ModBlocks.porcelain_pot),
							-1,
							0
					);
				case FULL:
					return new RemovalResult(
							ModBlocks.porcelain_set.getDefaultState().withProperty(FACING, facing).withProperty(ARRANGEMENT, CUPS),
							Item.getItemFromBlock(ModBlocks.porcelain_pot),
							-1,
							-1
					);
				default:
					return null;
			}
		}

		private IBlockState getBreakResultState(EnumFacing facing, World world) {
			switch (this) {
				case CUPS:
					return ModBlocks.porcelain_cup.getDefaultState().withProperty(FACING, facing);
				case POT_AND_CUP:
					return world.rand.nextBoolean()
							? ModBlocks.porcelain_cup.getDefaultState().withProperty(FACING, facing)
							: ModBlocks.porcelain_pot.getDefaultState().withProperty(FACING, facing);
				case FULL:
					return world.rand.nextInt(3) == 0
							? ModBlocks.porcelain_set.getDefaultState().withProperty(FACING, facing).withProperty(ARRANGEMENT, CUPS)
							: ModBlocks.porcelain_set.getDefaultState().withProperty(FACING, facing).withProperty(ARRANGEMENT, POT_AND_CUP);
				default:
					return null;
			}
		}

		public static Arrangement byMetadata(int metadata) {
			if (metadata < 0 || metadata >= VALUES.length) {
				return FULL;
			}
			return VALUES[metadata];
		}

		private static float getLocalX(EnumFacing facing, float hitX, float hitZ) {
			switch (facing) {
				case EAST:
					return hitZ;
				case SOUTH:
					return 1.0F - hitX;
				case WEST:
					return 1.0F - hitZ;
				case NORTH:
				default:
					return hitX;
			}
		}

		private static float getLocalZ(EnumFacing facing, float hitX, float hitZ) {
			switch (facing) {
				case EAST:
					return 1.0F - hitX;
				case SOUTH:
					return 1.0F - hitZ;
				case WEST:
					return hitX;
				case NORTH:
				default:
					return hitZ;
			}
		}
	}

	static final class RemovalResult {
		final IBlockState replacementState;
		final Item removedItem;
		final int removedCupSlot;
		final int replacementCupSourceSlot;

		RemovalResult(IBlockState replacementState, Item removedItem, int removedCupSlot, int replacementCupSourceSlot) {
			this.replacementState = replacementState;
			this.removedItem = removedItem;
			this.removedCupSlot = removedCupSlot;
			this.replacementCupSourceSlot = replacementCupSourceSlot;
		}
	}
}
