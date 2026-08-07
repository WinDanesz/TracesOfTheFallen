package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.world.FlatMultiblockPattern;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class BlockStoneCircle extends BlockContainer {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool SNOWY = PropertyBool.create("snowy");
	public static final PropertyBool CORE = PropertyBool.create("core");
	private static final FlatMultiblockPattern REQUIRED_FLAT_MULTIBLOCK = FlatMultiblockPattern.builder()
			.addSquare(0, -2, 1)
			.addSquare(0, 2, 1)
			.addSquare(-2, 0, 1)
			.addSquare(2, 0, 1)
			.build();
	public ResourceLocation lootTable;
	public AxisAlignedBB boundingBox;

	public BlockStoneCircle(Material materialmaterialn) {
		super(materialmaterialn);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(SNOWY, false).withProperty(CORE, true));
		setBlockUnbreakable();
		setResistance(6000000.0F);
		this.boundingBox = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D); // Default AABB
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return (meta & 8) == 0 ? new TileEntityStoneCircle() : null;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(CORE);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		if (!state.getValue(CORE)) {
			return false;
		}

		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityStoneCircle)) {
			return false;
		}

		TileEntityStoneCircle stoneCircle = (TileEntityStoneCircle) te;
		ItemStack heldItem = playerIn.getHeldItem(hand);

		if (heldItem.getItem() == ModItems.rune_of_skimming) {
			if (!hasRequiredFlatMultiblock(worldIn, pos)) {
				playerIn.sendMessage(new TextComponentTranslation("totf:stone_circle.incomplete"));
				return true;
			}
			if (stoneCircle.getPair() != null) {
				// Already paired, teleport
				teleportPlayerToTwin(playerIn, stoneCircle.getPair(), worldIn.provider.getDimension());
			} else {
				// Not paired.
				playerIn.sendMessage(new TextComponentTranslation("totf:stone_circle.dormant"));
			}
			return true;
		}

		return false;
	}


	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return this.boundingBox;
	}

	public BlockStoneCircle setBoundingBox(AxisAlignedBB boundingBox) {
		this.boundingBox = boundingBox;
		return this;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		boolean isSnowy = worldIn.getBiome(pos).isSnowyBiome();
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(SNOWY, isSnowy).withProperty(CORE, true);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (!worldIn.isRemote && state.getValue(CORE) && canGenerateStructureAt(worldIn, pos.down())) {
			placeStructure(worldIn, pos, state, 2);
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
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(FACING).getHorizontalIndex();

		if (state.getValue(SNOWY)) {
			i |= 4;
		}
		if (!state.getValue(CORE)) {
			i |= 8;
		}

		return i;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3)).withProperty(SNOWY, (meta & 4) != 0).withProperty(CORE, (meta & 8) == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, SNOWY, CORE);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return state.getValue(CORE) ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			IBlockState newState = worldIn.getBlockState(pos);
			boolean isReplacingWithStoneCircle = newState.getBlock() instanceof BlockStoneCircle;

			BlockPos corePos = state.getValue(CORE) ? pos : findCore(worldIn, pos);
			if (corePos != null) {
				forEachStructurePos(corePos, partPos -> {
					if (!partPos.equals(pos)) {
						IBlockState currentPartState = worldIn.getBlockState(partPos);
						if (currentPartState.getBlock() == state.getBlock()) {
							if (isReplacingWithStoneCircle) {
								worldIn.setBlockState(partPos, newState.withProperty(CORE, partPos.equals(corePos)), 2);
							} else {
								worldIn.setBlockToAir(partPos);
							}
						}
					}
				});
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	private BlockPos findCore(World worldIn, BlockPos satellitePos) {
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				BlockPos candidate = satellitePos.add(dx, 0, dz);
				IBlockState s = worldIn.getBlockState(candidate);
				if (s.getBlock() instanceof BlockStoneCircle && s.getValue(CORE)) {
					boolean[] owned = {false};
					REQUIRED_FLAT_MULTIBLOCK.forEachPosition(candidate, p -> {
						if (p.equals(satellitePos)) owned[0] = true;
					});
					if (owned[0]) return candidate;
				}
			}
		}
		return null;
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!worldIn.isRemote && player.isCreative() && this.lootTable != null) {
			LootTable loottable = worldIn.getLootTableManager().getLootTableFromLocation(this.lootTable);
			LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) worldIn).withPlayer(player).withLuck(player.getLuck());

			for (ItemStack itemstack : loottable.generateLootForPools(worldIn.rand, lootcontext$builder.build())) {
				spawnAsEntity(worldIn, pos, itemstack);
			}
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess iBlockAccess, BlockPos pos, IBlockState state, int fortune) {
		if (!(iBlockAccess instanceof WorldServer) || ((WorldServer) iBlockAccess).isRemote) {
			return Collections.emptyList();
		}

		World realWorld = (World) iBlockAccess;
		LootTable lootTable = realWorld.getLootTableManager().getLootTableFromLocation(this.lootTable);
		LootContext.Builder builder = new LootContext.Builder((WorldServer) realWorld);
		builder.withLuck(0);
		// Optionally add more context (e.g., player, tile entity)
		return lootTable.generateLootForPools(realWorld.rand, builder.build());
	}
	
	private void teleportPlayerToTwin(EntityPlayer player, BlockPos twinPos, int dimension) {
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;

			if (playerMP.dimension != dimension) {
				// Cross-dimension teleport
				playerMP.getServer().getPlayerList().transferPlayerToDimension(playerMP, dimension, (world, entity, yaw) -> {
					// Teleport above the altar structure
					entity.setPositionAndUpdate(twinPos.getX() + 0.5, twinPos.getY() + 0.2, twinPos.getZ() + 0.5);
				});
			} else {
				// Same dimension teleport - place above the altar
				playerMP.setPositionAndUpdate(twinPos.getX() + 0.5, twinPos.getY() + 0.2, twinPos.getZ() + 0.5);
			}

			// Play teleport sound
			playerMP.world.playSound(null, playerMP.posX, playerMP.posY, playerMP.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

			com.windanesz.tracesofthefallen.block.TileEntityWroughtCagedLamp.triggerNearbyWroughtLamps(playerMP.world, playerMP.getPosition());

			player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "You have been teleported!"));
		}
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		// Return UNDEFINED to prevent fences from connecting
		return BlockFaceShape.UNDEFINED;
	}

	public static boolean hasRequiredFlatMultiblock(World world, BlockPos centerPos) {
		return REQUIRED_FLAT_MULTIBLOCK.matches(centerPos, partPos -> isStoneCirclePart(world, partPos));
	}

	public static boolean canGenerateStructureAt(World world, BlockPos groundCenterPos) {
		BlockPos centerPos = groundCenterPos.up();
		return canPlaceStoneCirclePart(world, centerPos) && REQUIRED_FLAT_MULTIBLOCK.matches(centerPos, partPos -> canPlaceStoneCirclePart(world, partPos));
	}

	public static void placeStructure(World world, BlockPos centerPos, IBlockState centerState, int flags) {
		IBlockState coreState = centerState.withProperty(CORE, true);
		IBlockState partState = coreState.withProperty(CORE, false);
		world.setBlockState(centerPos, coreState, flags);
		REQUIRED_FLAT_MULTIBLOCK.forEachPosition(centerPos, partPos -> world.setBlockState(partPos, partState, flags));
	}

	private static boolean isStoneCirclePart(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getBlock() instanceof BlockStoneCircle && !state.getValue(CORE);
	}

	public static void forEachStructurePos(BlockPos centerPos, java.util.function.Consumer<BlockPos> consumer) {
		consumer.accept(centerPos);
		REQUIRED_FLAT_MULTIBLOCK.forEachPosition(centerPos, consumer);
	}

	private static boolean canPlaceStoneCirclePart(World world, BlockPos pos) {
		IBlockState supportState = world.getBlockState(pos.down());
		if (!supportState.isTopSolid() || supportState.getMaterial().isLiquid()) {
			return false;
		}
		if (world.getBlockState(pos).getBlock() instanceof BlockStoneCircle) {
			return true;
		}
		return isClearAbove(world, pos);
	}

	private static boolean isClearAbove(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return world.isAirBlock(pos) || state.getMaterial().isReplaceable() || isGrassOrFlower(world, pos);
	}

	private static boolean isGrassOrFlower(World world, BlockPos pos) {
		ResourceLocation registryName = world.getBlockState(pos).getBlock().getRegistryName();
		if (registryName == null) {
			return false;
		}
		String name = registryName.toString();
		return name.contains("grass") || name.contains("flower");
	}
}
