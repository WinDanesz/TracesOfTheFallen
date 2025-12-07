package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ModCreativeTab {
	public static final CreativeTabs TOTF_TAB = new CreativeTabs(TracesOfTheFallen.MODID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ModItems.grave_rose);
		}
	};
}
