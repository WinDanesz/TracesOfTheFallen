package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBundleOfLostLetters extends Item {

	public ItemBundleOfLostLetters() {
		this.setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);

		if (!world.isRemote) {
			// Grant Bliss potion effect
			player.addPotionEffect(new PotionEffect(ModPotions.bliss, Settings.miscSettings.bundleOfLostLettersBlissDuration));

			// Grant XP
			player.addExperience(Settings.miscSettings.bundleOfLostLettersXP);

			// Consume the item
			if (!player.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}

			// 5 min cooldown
			player.getCooldownTracker().setCooldown(this, Settings.miscSettings.bundleOfLostLettersCooldown);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}
}
