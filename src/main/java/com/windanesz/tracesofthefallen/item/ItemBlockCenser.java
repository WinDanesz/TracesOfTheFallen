package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.IncenseEffects;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.TileEntityBurningIncense;
import com.windanesz.tracesofthefallen.block.TileEntityCenser;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemBlockCenser extends ItemBlock {

	private static final int CENSER_ITEM_AURA_RADIUS = 2; // 5x5 area

	public ItemBlockCenser(Block block) {
		super(block);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getBurnProgress(stack) > 0.0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		double burnProgress = getBurnProgress(stack);
		return burnProgress <= 0.0D ? 0.0D : 1.0D - burnProgress;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return getDurabilityBarColor(stack);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if ((!oldStack.isEmpty() || !newStack.isEmpty()) && oldStack.getItem() == newStack.getItem() && !slotChanged) {
			return false;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
		if (ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) {
			return true;
		}
		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
		if (ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) {
			return false;
		}
		return super.shouldCauseBlockBreakReset(oldStack, newStack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

		if (!(entityIn instanceof EntityPlayer) || !stack.hasTagCompound()) {
			return;
		}

		EntityPlayer player = (EntityPlayer) entityIn;
		boolean inMainHand = player.getHeldItemMainhand() == stack;
		boolean inOffHand = player.getHeldItemOffhand() == stack;
		if (!inMainHand && !inOffHand) {
			return;
		}

		TileEntityCenser heldCenser = readHeldCenserState(stack);
		if (heldCenser == null || !heldCenser.isActivelyBurning()) {
			return;
		}

		if (!worldIn.isRemote) {
			if (player.ticksExisted % IncenseEffects.EFFECT_REFRESH_INTERVAL == 0) {
				AxisAlignedBB area = IncenseEffects.getAuraArea(player.getPosition(), CENSER_ITEM_AURA_RADIUS);
				heldCenser.applyAuraAt(worldIn, area);
			}
			heldCenser.tickHeldItemBurningState();
			writeHeldCenserState(stack, heldCenser);
			return;
		}

		if (player.ticksExisted % 8 == 0) {
			spawnHeldSmoke(worldIn, player, inMainHand, heldCenser.getAuraParticleColor());
			spawnHeldMistFog(worldIn, player, heldCenser.getAuraRadius(), heldCenser.getAuraParticleColor());
		}
	}

	private static TileEntityCenser readHeldCenserState(ItemStack stack) {
		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null || !stackTag.hasKey(TileEntityBurningIncense.ITEM_DATA_TAG, 10)) {
			return null;
		}
		NBTTagCompound data = stackTag.getCompoundTag(TileEntityBurningIncense.ITEM_DATA_TAG);
		TileEntityCenser censer = new TileEntityCenser();
		censer.readBurningData(data);
		return censer;
	}

	private static void writeHeldCenserState(ItemStack stack, TileEntityCenser censer) {
		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null) {
			stackTag = new NBTTagCompound();
			stack.setTagCompound(stackTag);
		}
		stackTag.setTag(TileEntityBurningIncense.ITEM_DATA_TAG, censer.writeBurningData(new NBTTagCompound()));
	}

	private static double getBurnProgress(ItemStack stack) {
		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null || !stackTag.hasKey(TileEntityBurningIncense.ITEM_DATA_TAG, 10)) {
			return 0.0D;
		}

		NBTTagCompound data = stackTag.getCompoundTag(TileEntityBurningIncense.ITEM_DATA_TAG);
		int totalBurnTime = data.getInteger("TotalBurnTime");
		int remainingBurnTime = data.getInteger("RemainingBurnTime");
		if (totalBurnTime <= 0 || remainingBurnTime <= 0) {
			return 0.0D;
		}

		double progress = (double) remainingBurnTime / (double) totalBurnTime;
		return Math.max(0.0D, Math.min(1.0D, progress));
	}

	private static int getDurabilityBarColor(ItemStack stack) {
		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag != null && stackTag.hasKey(TileEntityBurningIncense.ITEM_DATA_TAG, 10)) {
			NBTTagCompound data = stackTag.getCompoundTag(TileEntityBurningIncense.ITEM_DATA_TAG);
			NBTTagList auraEffects = data.getTagList("AuraEffects", 10);
			if (auraEffects.tagCount() > 0) {
				NBTTagCompound effectTag = auraEffects.getCompoundTagAt(0);
				if (effectTag.hasKey("Potion")) {
					Potion potion = Potion.getPotionFromResourceLocation(effectTag.getString("Potion"));
					if (potion != null) {
						return potion.getLiquidColor();
					}
				}
			}
		}
		return ModPotions.serenity != null ? ModPotions.serenity.getLiquidColor() : 0x7FD8D8;
	}

	private void spawnHeldSmoke(World world, EntityPlayer player, boolean inMainHand, int color) {
		boolean rightSide = inMainHand ? player.getPrimaryHand() == EnumHandSide.RIGHT : player.getPrimaryHand() != EnumHandSide.RIGHT;
		double yawRadians = Math.toRadians(player.rotationYaw);
		double forwardX = -Math.sin(yawRadians);
		double forwardZ = Math.cos(yawRadians);
		double sideX = Math.cos(yawRadians);
		double sideZ = Math.sin(yawRadians);
		double handOffset = rightSide ? 0.22D : -0.22D;
		double x = player.posX + forwardX * 0.18D + sideX * handOffset;
		double y = player.posY + player.getEyeHeight() - 0.55D;
		double z = player.posZ + forwardZ * 0.18D + sideZ * handOffset;
		double motionX = (world.rand.nextDouble() - 0.5D) * 0.00018D;
		double motionY = 0.0045D + world.rand.nextDouble() * 0.0012D;
		double motionZ = (world.rand.nextDouble() - 0.5D) * 0.00018D;

		IncenseEffects.spawnSmokeParticle(world, x, y, z, motionX, motionY, motionZ, color);
	}

	private void spawnHeldMistFog(World world, EntityPlayer player, int radius, int color) {
		double spreadDiameter = Math.max(1.0D, radius * 2.0D);
		double x = player.posX + (world.rand.nextDouble() - 0.5D) * spreadDiameter;
		double y = player.posY + 0.02D + world.rand.nextDouble() * 0.04D;
		double z = player.posZ + (world.rand.nextDouble() - 0.5D) * spreadDiameter;
		double motionX = (world.rand.nextDouble() - 0.5D) * 0.0006D;
		double motionY = world.rand.nextDouble() * 0.00015D;
		double motionZ = (world.rand.nextDouble() - 0.5D) * 0.0006D;
		TracesOfTheFallen.proxy.spawnIncenseFloorMistParticle(world, x, y, z, motionX, motionY, motionZ, color);
	}
}
