package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemOldWorldTinkersKit extends Item {

	public ItemOldWorldTinkersKit() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack kit = player.getHeldItem(hand);
		EnumHand otherHand = hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
		ItemStack tool = player.getHeldItem(otherHand);

		// Check if the other hand has a damageable item
		if (tool.isEmpty() || !tool.isItemStackDamageable() || !tool.isItemDamaged()) {
			return new ActionResult<>(EnumActionResult.PASS, kit);
		}

		if (!world.isRemote) {
			// Calculate durability to restore
			int maxDurability = tool.getMaxDamage();
			int restoreAmount = (int) Math.ceil(maxDurability * Settings.miscSettings.oldWorldTinkersKitRestorePercent);
			
			// Get current damage and reduce it
			int currentDamage = tool.getItemDamage();
			int newDamage = Math.max(0, currentDamage - restoreAmount);
			tool.setItemDamage(newDamage);

			// Consume the kit
			if (!player.capabilities.isCreativeMode) {
				kit.shrink(1);
			}
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, kit);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TextFormatting.GRAY + I18n.format("item.totf:old_world_tinkers_kit.desc"));
		int percent = (int) (Settings.miscSettings.oldWorldTinkersKitRestorePercent * 100);
		tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:old_world_tinkers_kit.desc2", percent));
	}
}
