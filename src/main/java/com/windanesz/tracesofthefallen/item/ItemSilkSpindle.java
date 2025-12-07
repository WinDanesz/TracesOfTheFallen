package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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

public class ItemSilkSpindle extends Item {

	public ItemSilkSpindle() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);

		if (!world.isRemote) {
			ItemStack silk = new ItemStack(Items.STRING, Settings.miscSettings.silkSpindleStringAmount);
			
			if (!player.inventory.addItemStackToInventory(silk)) {
				// If inventory is full, drop the string
				player.dropItem(silk, false);
			}

			// Consume the spindle
			if (!player.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TextFormatting.GRAY + I18n.format("item.totf:silk_spindle.desc", Settings.miscSettings.silkSpindleStringAmount));
	}
}
