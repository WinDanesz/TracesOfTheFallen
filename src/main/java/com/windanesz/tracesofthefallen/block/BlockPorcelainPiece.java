package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.PorcelainLiquids;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Random;

public class BlockPorcelainPiece extends BlockDecoration {

	public static final PropertyBool FILLED = PropertyBool.create("filled");

	private final PieceType pieceType;
	private final AxisAlignedBB northBoundingBox;

	public BlockPorcelainPiece(Material material, PieceType pieceType, AxisAlignedBB northBoundingBox) {
		super(material);
		setDefaultState(this.blockState.getBaseState()
				.withProperty(FACING, EnumFacing.NORTH)
				.withProperty(FILLED, false));
		this.pieceType = pieceType;
		this.northBoundingBox = northBoundingBox;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return rotateBoundingBox(northBoundingBox, state.getValue(FACING));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
				.withProperty(FILLED, (meta & 4) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(FACING).getHorizontalIndex();
		if (state.getValue(FILLED)) {
			meta |= 4;
		}
		return meta;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityPorcelainVessel();
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, getPlacementFacing(placer));
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		if (requiresSolidTopSupport() && !hasSolidTopSupport(worldIn, pos)) {
			return false;
		}
		return super.canPlaceBlockAt(worldIn, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		if (!worldIn.isRemote && requiresSolidTopSupport() && !hasSolidTopSupport(worldIn, pos)) {
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntityPorcelainVessel vessel = getVessel(worldIn, pos);
		if (vessel != null) {
			vessel.readStackData(stack);
			syncFilledState(worldIn, pos, vessel);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hand == EnumHand.OFF_HAND) {
			return false;
		}

		ItemStack heldStack = playerIn.getHeldItem(hand);
		TileEntityPorcelainVessel vessel = getVessel(worldIn, pos);

		Boolean vesselResult = pieceType == PieceType.POT
				? handlePotInteraction(worldIn, pos, state, playerIn, hand, heldStack, vessel)
				: handleCupInteraction(worldIn, pos, playerIn, heldStack, vessel);

		if (vesselResult != null) {
			return vesselResult;
		}

		if (pieceType == PieceType.CUP && vessel != null && vessel.hasStoredCupData()) {
			return false;
		}

		BlockPorcelainSet.Arrangement targetArrangement = pieceType.getUpgradeArrangement(heldStack);
		if (targetArrangement == null) {
			return false;
		}

		if (!worldIn.isRemote) {
			worldIn.setBlockState(pos, ModBlocks.porcelain_set.getDefaultState()
					.withProperty(FACING, state.getValue(FACING))
					.withProperty(BlockPorcelainSet.ARRANGEMENT, targetArrangement), 3);
			if (!playerIn.capabilities.isCreativeMode) {
				heldStack.shrink(1);
			}
		}
		return true;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		TileEntityPorcelainVessel vessel = getVessel(world, pos);
		if (vessel != null) {
			ItemStack stack = vessel.createStack(this);
			if (!stack.isEmpty()) {
				drops.add(stack);
				return;
			}
		}

		Item item = Item.getItemFromBlock(this);
		if (item != null) {
			drops.add(new ItemStack(item));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntityPorcelainVessel vessel = getVessel(world, pos);
		return vessel == null ? new ItemStack(Item.getItemFromBlock(this)) : vessel.createStack(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, FILLED);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, worldIn, pos, rand);
		if (pieceType != PieceType.POT) {
			return;
		}

		TileEntityPorcelainVessel vessel = getVessel(worldIn, pos);
		if (vessel == null || !vessel.isSteaming()) {
			return;
		}

		if (!vessel.isActivelyHeated() && rand.nextInt(4) != 0) {
			return;
		}

		double x = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.12D;
		double y = pos.getY() + 0.42D + rand.nextDouble() * 0.05D;
		double z = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.12D;
		worldIn.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0.0D, 0.02D + rand.nextDouble() * 0.01D, 0.0D);
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
		IBlockState state = worldIn.getBlockState(pos);
		super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
		if (shouldBreakFromImpact(worldIn, entityIn, fallDistance)) {
			playImpactEffects(worldIn, pos, state);
			worldIn.setBlockToAir(pos);
		}
	}

	static boolean shouldBreakFromImpact(World worldIn, Entity entityIn, float fallDistance) {
		return !worldIn.isRemote
				&& fallDistance > 0.5F
				&& entityIn != null
				&& entityIn.motionY < 0.0D;
	}

	static void playImpactEffects(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.1F, 0.9F + (worldIn.rand.nextFloat() * 0.2F));
		if (worldIn instanceof WorldServer) {
			((WorldServer) worldIn).spawnParticle(
					EnumParticleTypes.BLOCK_DUST,
					pos.getX() + 0.5D,
					pos.getY() + 0.2D,
					pos.getZ() + 0.5D,
					24,
					0.28D,
					0.12D,
					0.28D,
					0.04D,
					Block.getStateId(state));
		}
	}

	static AxisAlignedBB rotateBoundingBox(AxisAlignedBB box, EnumFacing facing) {
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

	static EnumFacing getPlacementFacing(EntityLivingBase placer) {
		return placer.getHorizontalFacing().rotateYCCW();
	}

	private boolean hasSolidTopSupport(World world, BlockPos pos) {
		BlockPos belowPos = pos.down();
		IBlockState belowState = world.getBlockState(belowPos);
		if (belowState.isSideSolid(world, belowPos, EnumFacing.UP)) {
			return true;
		}
		Block belowBlock = belowState.getBlock();
		return TileEntityPorcelainVessel.isConfiguredHeatSource(belowBlock.getRegistryName());
	}

	private boolean requiresSolidTopSupport() {
		return pieceType == PieceType.CUP || pieceType == PieceType.POT;
	}

	@Nullable
	private Boolean handlePotInteraction(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			ItemStack heldStack, @Nullable TileEntityPorcelainVessel vessel) {
		if (vessel == null) {
			return false;
		}

		if (heldStack.isEmpty()) {
			if (worldIn.isRemote) {
				return true;
			}

			ItemStack stack = vessel.createStack(this);
			if (!stack.isEmpty() && !playerIn.inventory.addItemStackToInventory(stack)) {
				Block.spawnAsEntity(worldIn, pos, stack);
			}
			worldIn.setBlockToAir(pos);
			return true;
		}

		if (isWaterBottle(heldStack)) {
			if (!vessel.canAcceptFluid("water")) {
				if (!worldIn.isRemote) {
					sendPotStatus(playerIn, getWaterStatusKey(vessel), getWaterStatusArgs(vessel));
				}
				return true;
			}

			if (worldIn.isRemote) {
				return true;
			}

			if (!vessel.addFluid("water", 1)) {
				return false;
			}
			if (!playerIn.capabilities.isCreativeMode) {
				ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
				heldStack.shrink(1);
				if (heldStack.isEmpty()) {
					playerIn.setHeldItem(hand, emptyBottle);
				} else if (!playerIn.inventory.addItemStackToInventory(emptyBottle)) {
					playerIn.dropItem(emptyBottle, false);
				}
			}
			worldIn.playSound(playerIn, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 0.8F, 1.0F);
			sendPotStatus(playerIn, "totf:porcelain_pot.status.water", getFluidDisplayName(vessel.getBaseFluidType()), vessel.getFluidLevel(), 3);
			return true;
		}

		String containerFluid = getContainerFluidName(heldStack);
		if (!containerFluid.isEmpty()) {
			if (!vessel.canAcceptFluid(containerFluid)) {
				if (!worldIn.isRemote) {
					sendPotStatus(playerIn, getWaterStatusKey(vessel), getWaterStatusArgs(vessel));
				}
				return true;
			}

			if (worldIn.isRemote) {
				return true;
			}

			if (!vessel.fillWithFluid(containerFluid)) {
				return false;
			}
			if (!consumeFilledFluidContainer(playerIn, hand, heldStack, containerFluid)) {
				return false;
			}
			worldIn.playSound(playerIn, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.8F, 1.0F);
			sendPotStatus(playerIn, "totf:porcelain_pot.status.full");
			return true;
		}

		PorcelainLiquids.LiquidDefinition liquidDefinition = PorcelainLiquids.getForIngredient(heldStack);
		if (liquidDefinition != null) {
			String requiredFluid = normalizeFluidName(liquidDefinition.forgeFluidName);
			if (!requiredFluid.isEmpty() && !requiredFluid.equals(vessel.getBaseFluidType())) {
				if (!worldIn.isRemote) {
					sendPotStatus(playerIn, "totf:porcelain_pot.status.need_base_fluid", getFluidDisplayName(requiredFluid));
				}
				return true;
			}

			if (!vessel.canBrewLiquid(heldStack)) {
				if (!worldIn.isRemote) {
					sendPotStatus(playerIn, getTeaStatusKey(vessel), getTeaStatusArgs(vessel));
				}
				return true;
			}

			if (worldIn.isRemote) {
				return true;
			}

			if (!vessel.brewLiquid(heldStack)) {
				return false;
			}

			if (!playerIn.capabilities.isCreativeMode) {
				heldStack.shrink(1);
			}
			worldIn.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.8F, 1.0F);
			sendPotStatus(playerIn, "totf:porcelain_pot.status.tea_ready", vessel.getBrewServings());
			return true;
		}

		if (TileEntityPorcelainVessel.isPlainPorcelainCup(heldStack)) {
			if (!vessel.canPourBrew() || playerIn.isSneaking()) {
				return null;
			}

			if (worldIn.isRemote) {
				return true;
			}

			String liquidType = vessel.getBrewedLiquidType();
			if (!vessel.pourBrewIntoCup()) {
				return false;
			}

			ItemStack filledCup = TileEntityPorcelainVessel.createCupStack(liquidType);
			if (!playerIn.capabilities.isCreativeMode) {
				if (heldStack.getCount() == 1) {
					playerIn.setHeldItem(hand, filledCup);
				} else {
					heldStack.shrink(1);
					if (!playerIn.inventory.addItemStackToInventory(filledCup)) {
						playerIn.dropItem(filledCup, false);
					}
				}
			} else if (!playerIn.inventory.addItemStackToInventory(filledCup)) {
				playerIn.dropItem(filledCup, false);
			}

			worldIn.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.7F, 1.2F);
			sendPotStatus(playerIn, "totf:porcelain_pot.status.poured", vessel.getBrewServings());
			return true;
		}

		if (pieceType.getUpgradeArrangement(heldStack) == null) {
			if (!worldIn.isRemote) {
				sendPotStatus(playerIn, getTeaStatusKey(vessel), getTeaStatusArgs(vessel));
			}
			return true;
		}

		return null;
	}

	@Nullable
	private Boolean handleCupInteraction(World worldIn, BlockPos pos, EntityPlayer playerIn, ItemStack heldStack,
			@Nullable TileEntityPorcelainVessel vessel) {
		if (vessel == null) {
			return null;
		}

		if (!playerIn.isSneaking() && !vessel.hasCupContent() && TileEntityPorcelainVessel.canStoredPotPourLiquid(heldStack)) {
			if (worldIn.isRemote) {
				return true;
			}

			String liquidType = TileEntityPorcelainVessel.getStoredBrewLiquidType(heldStack);
			if (!vessel.hasCupContent() && TileEntityPorcelainVessel.pourLiquidFromStoredPot(heldStack)) {
				vessel.setCupContent(liquidType);
				worldIn.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.7F, 1.2F);
				return true;
			}

			return false;
		}

		if (!heldStack.isEmpty()) {
			return null;
		}

		if (worldIn.isRemote) {
			return true;
		}

		ItemStack stack = vessel.createStack(this);
		if (!stack.isEmpty() && !playerIn.inventory.addItemStackToInventory(stack)) {
			Block.spawnAsEntity(worldIn, pos, stack);
		}
		worldIn.setBlockToAir(pos);
		return true;
	}

	private boolean isWaterBottle(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() == Items.POTIONITEM
				&& PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER;
	}

	private String getContainerFluidName(ItemStack stack) {
		if (stack.isEmpty()) {
			return "";
		}
		if (stack.getItem() == Items.WATER_BUCKET) {
			return "water";
		}
		if (stack.getItem() == Items.MILK_BUCKET) {
			return "milk";
		}
		FluidStack fluidStack = FluidUtil.getFluidContained(stack);
		if (fluidStack == null || fluidStack.amount < Fluid.BUCKET_VOLUME || fluidStack.getFluid() == null) {
			return "";
		}
		String fluidName = fluidStack.getFluid().getName();
		return fluidName == null ? "" : fluidName.trim().toLowerCase(Locale.ROOT);
	}

	private boolean consumeFilledFluidContainer(EntityPlayer player, EnumHand hand, ItemStack heldStack, String expectedFluid) {
		if (player.capabilities.isCreativeMode) {
			return true;
		}
		if (heldStack.getItem() == Items.MILK_BUCKET || heldStack.getItem() == Items.WATER_BUCKET) {
			player.setHeldItem(hand, new ItemStack(Items.BUCKET));
			return true;
		}

		ItemStack singleContainer = heldStack.copy();
		singleContainer.setCount(1);
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(singleContainer);
		if (handler == null) {
			return false;
		}

		FluidStack drained = handler.drain(Fluid.BUCKET_VOLUME, true);
		if (drained == null || drained.amount < Fluid.BUCKET_VOLUME || drained.getFluid() == null
				|| !expectedFluid.equals(drained.getFluid().getName())) {
			return false;
		}

		ItemStack emptyContainer = handler.getContainer();
		if (heldStack.getCount() == 1) {
			player.setHeldItem(hand, emptyContainer);
		} else {
			heldStack.shrink(1);
			if (!emptyContainer.isEmpty() && !player.inventory.addItemStackToInventory(emptyContainer)) {
				player.dropItem(emptyContainer, false);
			}
		}
		return true;
	}

	private String getFluidDisplayName(String fluidName) {
		String normalized = normalizeFluidName(fluidName);
		if (normalized.isEmpty()) {
			return "Fluid";
		}
		String key = "item.totf:porcelain_fluid." + normalized;
		String localized = new TextComponentTranslation(key).getUnformattedText();
		if (key.equals(localized)) {
			return normalized.replace('_', ' ');
		}
		return localized;
	}

	private String normalizeFluidName(String fluidName) {
		return fluidName == null ? "" : fluidName.trim().toLowerCase(Locale.ROOT);
	}

	private void sendPotStatus(EntityPlayer player, String translationKey, Object... args) {
		player.sendStatusMessage(new TextComponentTranslation(translationKey, args), true);
	}

	private String getWaterStatusKey(TileEntityPorcelainVessel vessel) {
		if (vessel.isBrewReady()) {
			return "totf:porcelain_pot.status.tea_ready";
		}
		if (vessel.isHeated()) {
			return "totf:porcelain_pot.status.add_leaves";
		}
		if (vessel.getFluidLevel() >= 3) {
			if (vessel.isHeatingInProgress()) {
				return "totf:porcelain_pot.status.heating_progress";
			}
			return "totf:porcelain_pot.status.place_on_furnace";
		}
		return "totf:porcelain_pot.status.water";
	}

	private Object[] getWaterStatusArgs(TileEntityPorcelainVessel vessel) {
		if (vessel.isBrewReady()) {
			return new Object[] {vessel.getBrewServings()};
		}
		if (vessel.getFluidLevel() >= 3 && vessel.isHeatingInProgress()) {
			return new Object[] {Math.max(1, vessel.getHeatingTicks() / 20), vessel.getRequiredHeatTime() / 20};
		}
		return new Object[] {getFluidDisplayName(vessel.getBaseFluidType()), vessel.getFluidLevel(), 3};
	}

	private String getTeaStatusKey(TileEntityPorcelainVessel vessel) {
		if (vessel.isBrewReady()) {
			return "totf:porcelain_pot.status.tea_ready";
		}
		if (vessel.getFluidLevel() < 3) {
			return "totf:porcelain_pot.status.need_water";
		}
		if (vessel.isHeated()) {
			return "totf:porcelain_pot.status.add_leaves";
		}
		if (vessel.isHeatingInProgress()) {
			return "totf:porcelain_pot.status.heating_progress";
		}
		return "totf:porcelain_pot.status.place_on_furnace";
	}

	private Object[] getTeaStatusArgs(TileEntityPorcelainVessel vessel) {
		if (vessel.isBrewReady()) {
			return new Object[] {vessel.getBrewServings()};
		}
		if (vessel.isHeatingInProgress()) {
			return new Object[] {Math.max(1, vessel.getHeatingTicks() / 20), vessel.getRequiredHeatTime() / 20};
		}
		if (vessel.getFluidLevel() < 3) {
			return new Object[] {getFluidDisplayName(vessel.getBaseFluidType()), vessel.getFluidLevel(), 3};
		}
		return new Object[0];
	}

	@Nullable
	private static TileEntityPorcelainVessel getVessel(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityPorcelainVessel ? (TileEntityPorcelainVessel) tileEntity : null;
	}

	static void syncFilledState(World world, BlockPos pos, @Nullable TileEntityPorcelainVessel vessel) {
		IBlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof BlockPorcelainPiece)) {
			return;
		}

		BlockPorcelainPiece block = (BlockPorcelainPiece) state.getBlock();
		boolean filled = block.pieceType == PieceType.CUP && vessel != null && vessel.hasCupContent();
		if (state.getValue(FILLED) != filled) {
			world.setBlockState(pos, state.withProperty(FILLED, filled), 2);
		}
	}

	public enum PieceType {
		CUP,
		POT;

		private BlockPorcelainSet.Arrangement getUpgradeArrangement(ItemStack heldStack) {
			if (this == CUP) {
				if (TileEntityPorcelainVessel.isPlainPorcelainCup(heldStack)) {
					return BlockPorcelainSet.Arrangement.CUPS;
				}
				if (TileEntityPorcelainVessel.isPlainPorcelainPot(heldStack)) {
					return BlockPorcelainSet.Arrangement.POT_AND_CUP;
				}
				return null;
			}

			return TileEntityPorcelainVessel.isPlainPorcelainCup(heldStack)
					? BlockPorcelainSet.Arrangement.POT_AND_CUP
					: null;
		}
	}
}
