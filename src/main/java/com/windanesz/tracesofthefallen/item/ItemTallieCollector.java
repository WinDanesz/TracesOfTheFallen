package com.windanesz.tracesofthefallen.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTallieCollector extends Item {

	private static final String COUNT_KEY = "Count";

	public ItemTallieCollector() {
		setMaxStackSize(1);
		addPropertyOverride(new ResourceLocation("multiple"),
				(stack, worldIn, entityIn) -> getTallieCount(stack) > 1 ? 1.0F : 0.0F);
	}

	public static long getTallieCount(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTagCompound()) {
			return 1;
		}
		return Math.max(1L, stack.getTagCompound().getLong(COUNT_KEY));
	}

	public static void setTallieCount(ItemStack stack, long count) {
		if (stack.isEmpty()) {
			return;
		}

		long normalized = Math.max(1L, count);
		if (normalized == 1) {
			if (stack.hasTagCompound()) {
				stack.getTagCompound().removeTag(COUNT_KEY);
				if (stack.getTagCompound().getSize() == 0) {
					stack.setTagCompound(null);
				}
			}
			return;
		}

		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		tag.setLong(COUNT_KEY, normalized);
		stack.setTagCompound(tag);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		long count = getTallieCount(stack);
		if (count == 1) {
			tooltip.add(I18n.translateToLocal("item.totf:tally_collector.count_one"));
		} else {
			tooltip.add(I18n.translateToLocalFormatted("item.totf:tally_collector.count_many", count));
		}
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (!isInCreativeTab(tab)) {
			return;
		}

		items.add(new ItemStack(this));
		ItemStack hundredTallies = new ItemStack(this);
		setTallieCount(hundredTallies, 100);
		items.add(hundredTallies);
	}
}
