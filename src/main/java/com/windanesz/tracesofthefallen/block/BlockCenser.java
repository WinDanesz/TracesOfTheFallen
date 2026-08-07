package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockCenser extends BlockDecoration {

	private static final int BURNING_LIGHT_LEVEL = 2;

	// Tight model bounds (single-block space); censer is non-rotating so AABB is facing-invariant.
	private static final AxisAlignedBB CENSER_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.9397, 0.6875);

	public BlockCenser(Material material) {
		super(material);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return CENSER_AABB;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityCenser censer = getCenser(world, pos);
		return censer != null && censer.isActivelyBurning() ? BURNING_LIGHT_LEVEL : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		TileEntityCenser censer = getCenser(world, pos);
		if (censer == null || !censer.isActivelyBurning() || rand.nextInt(3) == 0) {
			return;
		}

		int particleCount = 1 + rand.nextInt(2);
		double spreadDiameter = censer.getAuraRadius() * 2.0D;
		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double y = pos.getY() + 0.02D + rand.nextDouble() * 0.04D;
			double z = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double motionX = (rand.nextDouble() - 0.5D) * 0.0006D;
			double motionY = rand.nextDouble() * 0.00015D;
			double motionZ = (rand.nextDouble() - 0.5D) * 0.0006D;
			TracesOfTheFallen.proxy.spawnIncenseFloorMistParticle(world, x, y, z, motionX, motionY, motionZ, censer.getAuraParticleColor());
		}
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityCenser();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}

		TileEntityCenser censer = getCenser(worldIn, pos);
		if (censer == null) {
			return false;
		}

		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem.isEmpty()) {
			if (worldIn.isRemote) {
				return true;
			}

			ItemStack pickedUp = createCenserStack(worldIn, pos);
			worldIn.setBlockToAir(pos);
			playerIn.setHeldItem(hand, pickedUp);
			return true;
		}

		if (censer.canApplyPotion(heldItem)) {
			if (worldIn.isRemote) {
				return true;
			}

			if (!censer.applyPotionEffects(heldItem)) {
				return false;
			}

			consumePotionItem(playerIn, hand, heldItem);
			return true;
		}

		if (censer.canAcceptFuel(heldItem)) {
			if (worldIn.isRemote) {
				return true;
			}

			if (!censer.addFuel(heldItem)) {
				return false;
			}

			if (!playerIn.capabilities.isCreativeMode) {
				heldItem.shrink(1);
			}
			return true;
		}

		if (!IncenseEffects.isFireStarter(heldItem) || !censer.canIgnite()) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		if (!censer.tryLight()) {
			return false;
		}

		worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
				worldIn.rand.nextFloat() * 0.4F + 0.8F);

		damageFireStarter(playerIn, heldItem);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, net.minecraft.entity.EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntityCenser censer = getCenser(worldIn, pos);
		if (censer == null || !stack.hasTagCompound()) {
			return;
		}

		NBTTagCompound data = stack.getTagCompound().getCompoundTag(TileEntityBurningIncense.ITEM_DATA_TAG);
		if (data.isEmpty()) {
			return;
		}
		censer.readBurningData(data);
		censer.markDirty();
		censer.syncToClient();
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		drops.add(createCenserStack(world, pos));
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		if (!worldIn.isRemote) {
			TileEntityCenser censer = te instanceof TileEntityCenser ? (TileEntityCenser) te : getCenser(worldIn, pos);
			spawnAsEntity(worldIn, pos, createCenserStack(censer));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return createCenserStack(world, pos);
	}

	private ItemStack createCenserStack(IBlockAccess world, BlockPos pos) {
		return createCenserStack(getCenser(world, pos));
	}

	private ItemStack createCenserStack(@Nullable TileEntityCenser censer) {
		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
		if (censer == null) {
			return stack;
		}

		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}
		tag.setTag(TileEntityBurningIncense.ITEM_DATA_TAG, censer.writeBurningData(new NBTTagCompound()));
		return stack;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return hasSupport(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);

		if (!worldIn.isRemote) {
			if (!hasSupport(worldIn, pos)) {
				dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
		}
	}

	private boolean hasSupport(World world, BlockPos pos) {
		BlockPos belowPos = pos.down();
		IBlockState belowState = world.getBlockState(belowPos);
		if (belowState.isSideSolid(world, belowPos, EnumFacing.UP)) {
			return true;
		}

		BlockPos abovePos = pos.up();
		IBlockState aboveState = world.getBlockState(abovePos);
		return aboveState.isSideSolid(world, abovePos, EnumFacing.DOWN);
	}

	@Nullable
	private TileEntityCenser getCenser(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityCenser ? (TileEntityCenser) tileEntity : null;
	}

	private void consumePotionItem(EntityPlayer player, EnumHand hand, ItemStack stack) {
		if (player.capabilities.isCreativeMode) {
			return;
		}

		if (stack.getItem() == Items.POTIONITEM) {
			stack.shrink(1);
			if (stack.isEmpty()) {
				player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
			} else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
				player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
			}
			return;
		}

		stack.shrink(1);
	}

	private void damageFireStarter(EntityPlayer player, ItemStack stack) {
		if (player.capabilities.isCreativeMode) {
			return;
		}

		if (stack.getItem() == Items.FIRE_CHARGE) {
			stack.shrink(1);
		} else if (stack.isItemStackDamageable()) {
			stack.damageItem(1, player);
		} else {
			stack.shrink(1);
		}
	}
}
