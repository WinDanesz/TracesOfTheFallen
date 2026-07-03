package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockIncenseBurner extends BlockDecoration {

	private static final int BURNING_LIGHT_LEVEL = 2;

	public BlockIncenseBurner(Material material, AxisAlignedBB boundingBox) {
		super(material);
		setBoundingBox(boundingBox);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityIncenseBurner incenseBurner = getIncenseBurner(world, pos);
		return incenseBurner != null && incenseBurner.isActivelyBurning() ? BURNING_LIGHT_LEVEL : 0;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityIncenseBurner();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		AxisAlignedBB box = this.boundingBox;
		switch (state.getValue(FACING)) {
			case EAST:
				return rotateClockwise(box);
			case SOUTH:
				return rotateHalfTurn(box);
			case WEST:
				return rotateCounterClockwise(box);
			case NORTH:
			default:
				return box;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		TileEntityIncenseBurner incenseBurner = getIncenseBurner(world, pos);
		if (incenseBurner == null || !incenseBurner.isLit()) {
			return;
		}

		if (rand.nextInt(3) == 0) {
			return;
		}

		int particleCount = 2 + rand.nextInt(2);
		double spreadRadius = incenseBurner.getAuraRadius();
		double spreadDiameter = spreadRadius * 2.0D;

		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double y = pos.getY() + 0.02D + rand.nextDouble() * 0.04D;
			double z = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double motionX = (rand.nextDouble() - 0.5D) * 0.0006D;
			double motionY = rand.nextDouble() * 0.00015D;
			double motionZ = (rand.nextDouble() - 0.5D) * 0.0006D;

			TracesOfTheFallen.proxy.spawnIncenseFloorMistParticle(world, x, y, z, motionX, motionY, motionZ, incenseBurner.getAuraParticleColor());
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		TileEntityIncenseBurner incenseBurner = getIncenseBurner(worldIn, pos);
		if (incenseBurner == null) {
			return false;
		}

		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem.isEmpty()) {
			if (!worldIn.isRemote) {
				sendRemainingBurnStatus(playerIn, incenseBurner);
			}
			return true;
		}

		if (incenseBurner.canApplyPotion(heldItem)) {
			if (worldIn.isRemote) {
				return true;
			}

			if (!incenseBurner.applyPotionEffects(heldItem)) {
				return false;
			}

			consumePotionItem(playerIn, hand, heldItem);
			return true;
		}

		if (incenseBurner.canAcceptFuel(heldItem)) {
			if (worldIn.isRemote) {
				return true;
			}

			if (!incenseBurner.addFuel(heldItem)) {
				return false;
			}

			if (!playerIn.capabilities.isCreativeMode) {
				heldItem.shrink(1);
			}
			return true;
		}

		if (!IncenseEffects.isFireStarter(heldItem) || !incenseBurner.canIgnite()) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		if (!incenseBurner.tryLight()) {
			return false;
		}

		worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
				worldIn.rand.nextFloat() * 0.4F + 0.8F);

		damageFireStarter(playerIn, heldItem);
		return true;
	}

	private void sendRemainingBurnStatus(EntityPlayer player, TileEntityIncenseBurner incenseBurner) {
		int remainingTicks = Math.max(0, incenseBurner.getRemainingBurnTime());
		int potionTicks = Math.max(0, incenseBurner.getRemainingPotionDurationTicks());

		if (!incenseBurner.isLit() && remainingTicks == 0 && potionTicks == 0 && !incenseBurner.isBurnedOut()) {
			player.sendStatusMessage(new TextComponentTranslation("totf.incense_burner.empty"), true);
			return;
		}

		ITextComponent fuelComponent = getDurationValueComponent(remainingTicks);

		if (potionTicks <= 0) {
			if (remainingTicks < 1200) {
				player.sendStatusMessage(new TextComponentTranslation("totf.incense_burner.duration_nearly_spent"), true);
			} else {
				int minutes = Math.max(1, Math.round(remainingTicks / 1200.0F));
				player.sendStatusMessage(new TextComponentTranslation("totf.incense_burner.duration_minutes", minutes), true);
			}
			return;
		}

		ITextComponent potionComponent = getDurationValueComponent(potionTicks);
		player.sendStatusMessage(new TextComponentTranslation("totf.incense_burner.duration_combined", fuelComponent, potionComponent), true);
	}

	private ITextComponent getDurationValueComponent(int ticks) {
		if (ticks < 1200) {
			return new TextComponentTranslation("totf.incense_burner.duration_value_nearly_spent");
		}
		int minutes = Math.max(1, Math.round(ticks / 1200.0F));
		return new TextComponentTranslation("totf.incense_burner.duration_value_minutes", minutes);
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

	private TileEntityIncenseBurner getIncenseBurner(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityIncenseBurner ? (TileEntityIncenseBurner) tileEntity : null;
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
