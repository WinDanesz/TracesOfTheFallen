package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * A fired-clay pot that stores a single ItemStack. Right-clicking with an item while the pot is
 * empty places that item inside it; it has no GUI beyond that, and the only way to get the stored
 * item back out is to break the pot. Mining it without Silk Touch shatters it (spilling the
 * stored item or rolling loot for an empty pot, never dropping the pot itself); Silk Touch drops the pot with the stored item
 * baked into its NBT so it is restored when re-placed.
 */
public class BlockClayPot extends Block {

	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "blocks/clay_pot");

	// Anticipates 2-6 fired-clay texture variants selected by metadata; only variant 0 ships today.
	public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 5);

	private static final AxisAlignedBB CLAY_POT_AABB = new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 1.0D, 0.8125D);

	public BlockClayPot() {
		super(Material.ROCK);
		setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, 0));
		setHardness(0.2F);
		setResistance(5.0F);
		setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return CLAY_POT_AABB;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityClayPot();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, meta & 7);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}

		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityClayPot)) {
			return false;
		}

		TileEntityClayPot pot = (TileEntityClayPot) te;
		if (!pot.getStoredItem().isEmpty()) {
			// Already holding something; retrieval only happens by breaking the pot.
			return false;
		}

		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem.isEmpty()) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		ItemStack toStore = heldItem.copy();
		toStore.setCount(1);
		pot.setStoredItem(toStore);
		if (!playerIn.capabilities.isCreativeMode) {
			heldItem.shrink(1);
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (!stack.hasTagCompound()) {
			return;
		}
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityClayPot)) {
			return;
		}
		NBTTagCompound tag = stack.getTagCompound();
		if (tag.hasKey(TileEntityClayPot.STORED_ITEM_TAG)) {
			((TileEntityClayPot) te).setStoredItem(new ItemStack(tag.getCompoundTag(TileEntityClayPot.STORED_ITEM_TAG)));
		}
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		// Non-player removal (explosions, projectile impact) uses the same contents as player breaks.
		TileEntity te = world.getTileEntity(pos);
		addContents(drops, world, te instanceof TileEntityClayPot ? (TileEntityClayPot) te : null);
	}

	private void addContents(NonNullList<ItemStack> drops, IBlockAccess world, @Nullable TileEntityClayPot pot) {
		if (pot != null && !pot.getStoredItem().isEmpty()) {
			drops.add(pot.getStoredItem().copy());
		} else if (world instanceof WorldServer) {
			WorldServer server = (WorldServer) world;
			LootContext context = new LootContext.Builder(server).withLuck(0).build();
			drops.addAll(server.getLootTableManager().getLootTableFromLocation(LOOT_TABLE)
					.generateLootForPools(server.rand, context));
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack toolStack) {
		if (worldIn.isRemote) {
			return;
		}
		TileEntityClayPot pot = te instanceof TileEntityClayPot ? (TileEntityClayPot) te : null;
		boolean silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, toolStack) > 0;
		if (silkTouch) {
			spawnAsEntity(worldIn, pos, createPotStack(state, pot));
		} else {
			NonNullList<ItemStack> drops = NonNullList.create();
			addContents(drops, worldIn, pot);
			for (ItemStack drop : drops) {
				spawnAsEntity(worldIn, pos, drop);
			}
		}
		// Neither branch calls super/dropBlockAsItem, so the pot never drops on a non-silk-touch break.
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		return createPotStack(state, te instanceof TileEntityClayPot ? (TileEntityClayPot) te : null);
	}

	private ItemStack createPotStack(IBlockState state, @Nullable TileEntityClayPot pot) {
		ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(state));
		if (pot != null && !pot.getStoredItem().isEmpty()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag(TileEntityClayPot.STORED_ITEM_TAG, pot.getStoredItem().writeToNBT(new NBTTagCompound()));
			stack.setTagCompound(tag);
		}
		return stack;
	}
}
