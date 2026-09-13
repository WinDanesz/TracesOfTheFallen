package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A skeleton corpse decoration. Lies flat on the floor with a fine (16-way) rotation baked into its
 * tile entity, or leans against a wall in a sitting pose when placed on a vertical surface. Rendering
 * (both the body and the item held next to the arm) happens entirely in
 * {@link com.windanesz.tracesofthefallen.client.renderer.TileEntitySkeletonCorpseRenderer} since fine
 * rotation isn't representable by blockstate model variants (only 90-degree steps are).
 */
public class BlockSkeletonCorpse extends BlockContainer {

	public enum Pose implements IStringSerializable {
		GROUND, SIT;

		@Override
		public String getName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public static final PropertyEnum<Pose> POSE = PropertyEnum.create("pose", Pose.class);
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	private static final AxisAlignedBB GROUND_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.55D, 1.0D);
	private static final AxisAlignedBB SIT_NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.4D, 0.6D);
	private static final AxisAlignedBB SIT_SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.4D, 1.0D, 1.4D, 1.0D);
	private static final AxisAlignedBB SIT_WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.6D, 1.4D, 1.0D);
	private static final AxisAlignedBB SIT_EAST_AABB = new AxisAlignedBB(0.4D, 0.0D, 0.0D, 1.0D, 1.4D, 1.0D);

	public ResourceLocation lootTable;

	public BlockSkeletonCorpse(Material materialIn) {
		super(materialIn);
		this.setDefaultState(this.blockState.getBaseState().withProperty(POSE, Pose.GROUND).withProperty(FACING, EnumFacing.NORTH));
		setHardness(0.5F);
		setResistance(0.5F);
	}

	public BlockSkeletonCorpse setLootTable(ResourceLocation lootTable) {
		this.lootTable = lootTable;
		return this;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySkeletonCorpse();
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (facing.getAxis() != EnumFacing.Axis.Y) {
			return this.getDefaultState().withProperty(POSE, Pose.SIT).withProperty(FACING, facing);
		}
		return this.getDefaultState().withProperty(POSE, Pose.GROUND).withProperty(FACING, placer.getHorizontalFacing());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (state.getValue(POSE) == Pose.GROUND) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntitySkeletonCorpse) {
				int rotation = MathHelper.floor((double) (placer.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
				((TileEntitySkeletonCorpse) te).setRotation(rotation);
			}
		}
	}

	/**
	 * Creative-only: right-click with an item to set it as the corpse's held item (rendered next to the
	 * arm), sneak-right-click with an empty hand to clear it. Survival players can't do this - held items
	 * are meant to be deterministic (worldgen-baked) or set once via BlockEntityTag NBT, not freely
	 * assignable for nothing.
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!playerIn.capabilities.isCreativeMode || hand != EnumHand.MAIN_HAND) {
			return false;
		}

		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntitySkeletonCorpse)) {
			return false;
		}
		TileEntitySkeletonCorpse corpse = (TileEntitySkeletonCorpse) te;
		ItemStack held = playerIn.getHeldItem(hand);

		if (playerIn.isSneaking()) {
			if (corpse.getHeldItem().isEmpty()) {
				return false;
			}
			if (!worldIn.isRemote) {
				corpse.setHeldItem(ItemStack.EMPTY);
				worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			return true;
		}

		if (held.isEmpty()) {
			return false;
		}
		if (!worldIn.isRemote) {
			ItemStack toDisplay = held.copy();
			toDisplay.setCount(1);
			corpse.setHeldItem(toDisplay);
			worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(POSE) == Pose.SIT) {
			// The body leans back against the wall *behind* it - i.e. on the opposite side from the
			// direction it faces - so facing=NORTH (wall to the south) needs the box hugging the south
			// side, not north.
			switch (state.getValue(FACING)) {
				case SOUTH: return SIT_NORTH_AABB;
				case WEST: return SIT_EAST_AABB;
				case EAST: return SIT_WEST_AABB;
				case NORTH:
				default: return SIT_SOUTH_AABB;
			}
		}
		return GROUND_AABB;
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
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(POSE) == Pose.SIT ? 4 : 0;
		return i | state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(POSE, (meta & 4) != 0 ? Pose.SIT : Pose.GROUND)
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, POSE, FACING);
	}

	/**
	 * Rolls just the "held_item" pool of this block's loot table (the item rendered next to the arm).
	 * Used to bake a deterministic item into the tile entity at worldgen time, rather than letting it
	 * re-roll on break.
	 */
	public ItemStack rollHeldItem(World world, Random rand) {
		if (this.lootTable == null || !(world instanceof WorldServer)) {
			return ItemStack.EMPTY;
		}
		LootTable table = world.getLootTableManager().getLootTableFromLocation(this.lootTable);
		LootPool pool = table.getPool("held_item");
		if (pool == null) {
			return ItemStack.EMPTY;
		}
		List<ItemStack> result = new ArrayList<>();
		LootContext context = new LootContext.Builder((WorldServer) world).build();
		pool.generateLoot(result, rand, context);
		return result.isEmpty() ? ItemStack.EMPTY : result.get(0);
	}

	private List<ItemStack> rollBreakLoot(World world, LootContext context) {
		List<ItemStack> drops = new ArrayList<>();
		if (this.lootTable == null) {
			return drops;
		}
		LootTable table = world.getLootTableManager().getLootTableFromLocation(this.lootTable);
		LootPool remainsPool = table.getPool("remains");
		if (remainsPool != null) {
			remainsPool.generateLoot(drops, world.rand, context);
		}
		LootPool skullPool = table.getPool("skull");
		if (skullPool != null) {
			skullPool.generateLoot(drops, world.rand, context);
		}
		return drops;
	}

	// Without this, canHarvestBlock() falls back to Material.isToolNotRequired(), which isn't reliably
	// true for Material.CIRCUITS - meaning the block could be punched apart (its hardness is low either
	// way) but silently drop nothing, since vanilla only awards drops when the block considers itself
	// harvestable. This is a bare-hands-breakable decoration, so it always is.
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	// Overriding harvestBlock() (rather than just getDrops()) because by the time getDrops() would run,
	// the block has already been replaced with air and its tile entity removed - the held item would
	// always read back empty. harvestBlock() still has the TE reference from before removal.
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);

		if (worldIn.isRemote || this.lootTable == null) {
			return;
		}

		LootContext context = new LootContext.Builder((WorldServer) worldIn).withLuck(0).build();
		for (ItemStack drop : rollBreakLoot(worldIn, context)) {
			spawnAsEntity(worldIn, pos, drop);
		}

		if (te instanceof TileEntitySkeletonCorpse) {
			ItemStack held = ((TileEntitySkeletonCorpse) te).getHeldItem();
			if (!held.isEmpty()) {
				spawnAsEntity(worldIn, pos, held.copy());
			}
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess iBlockAccess, BlockPos pos, IBlockState state, int fortune) {
		if (!(iBlockAccess instanceof WorldServer) || ((WorldServer) iBlockAccess).isRemote) {
			return Collections.emptyList();
		}

		World realWorld = (World) iBlockAccess;
		LootContext context = new LootContext.Builder((WorldServer) realWorld).withLuck(0).build();
		return rollBreakLoot(realWorld, context);
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!worldIn.isRemote && player.isCreative() && this.lootTable != null) {
			LootContext context = new LootContext.Builder((WorldServer) worldIn).withPlayer(player).withLuck(player.getLuck()).build();
			for (ItemStack itemstack : rollBreakLoot(worldIn, context)) {
				spawnAsEntity(worldIn, pos, itemstack);
			}

			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntitySkeletonCorpse) {
				ItemStack held = ((TileEntitySkeletonCorpse) te).getHeldItem();
				if (!held.isEmpty()) {
					spawnAsEntity(worldIn, pos, held.copy());
				}
			}
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack stack = super.getPickBlock(state, target, world, pos, player);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntitySkeletonCorpse) {
			ItemStack held = ((TileEntitySkeletonCorpse) te).getHeldItem();
			if (!held.isEmpty()) {
				NBTTagCompound teNbt = new NBTTagCompound();
				((TileEntitySkeletonCorpse) te).writeToNBT(teNbt);
				NBTTagCompound tag = new NBTTagCompound();
				tag.setTag("BlockEntityTag", teNbt);
				stack.setTagCompound(tag);
			}
		}
		return stack;
	}

	public ResourceLocation getLootTable() {
		return this.lootTable;
	}
}
