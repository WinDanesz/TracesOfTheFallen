package com.windanesz.tracesofthefallen.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class ItemGoblinIdol extends Item {

	public ItemGoblinIdol() {
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(1000);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		// Check if item has active tag - if so, prevent usage while on cooldown
		if (itemstack.hasTagCompound() && itemstack.getTagCompound().getBoolean("active")) {
			// Item is already active, allow deactivation
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}
		// Item is not active, allow activation
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null) {
			ItemStack activeStack = player.getActiveItemStack();
			if (!activeStack.isEmpty() && activeStack == stack) {
				int timeUsed = this.getMaxItemUseDuration(stack) - player.getItemInUseCount();
				if (timeUsed >= 20) {
					return true;
				}
			}
		}
		// Check if the item has the active NBT tag
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("active")) {
			return stack.getTagCompound().getBoolean("active");
		}
		return false;
	}

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
		// Ignore durability changes

		return true;

//		if (ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack))
//			return true;
//		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase user, int count) {
		int timeUsed = this.getMaxItemUseDuration(stack) - count;

		if (timeUsed >= 40) {
			// After 40 ticks, set active=true and stop using
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setBoolean("active", true);
			user.stopActiveHand();
		}
		super.onUsingTick(stack, user, count);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, net.minecraft.entity.Entity entityIn, int itemSlot, boolean isSelected) {
		if (!worldIn.isRemote && stack.hasTagCompound() && stack.getTagCompound().getBoolean("active")) {
			if (entityIn instanceof EntityPlayerMP && entityIn.ticksExisted % 20 == 0) {
				EntityPlayerMP player = (EntityPlayerMP) entityIn;
				if (stack.attemptDamageItem(1, new Random(), player)) {
					// Item broke
					stack.shrink(1);
				}
			}
		}
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		// If active tag exists, remove it (player used it again for 40 ticks)
		if (stack.getTagCompound().hasKey("active")) {
			stack.getTagCompound().removeTag("active");
			if (entityLiving instanceof EntityPlayer) {
				((EntityPlayer) entityLiving).getCooldownTracker().setCooldown(this, 10);
			}
		} else {
			// First time finishing use, set active to true
			stack.getTagCompound().setBoolean("active", true);
			if (entityLiving instanceof EntityPlayer) {
				((EntityPlayer) entityLiving).getCooldownTracker().setCooldown(this, 10);
			}
		}
		return stack;
	}

	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (!oldStack.isEmpty() || !newStack.isEmpty()) {
			// We only care about the situation where we specifically want the animation NOT to play.
			if (oldStack.getItem() == newStack.getItem() && !slotChanged) {
				return false;
			}
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public boolean isActive(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().getBoolean("active");
	}
}
