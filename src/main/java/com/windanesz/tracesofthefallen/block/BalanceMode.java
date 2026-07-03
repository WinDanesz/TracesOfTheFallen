package com.windanesz.tracesofthefallen.block;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public enum BalanceMode {
	VENDING("vending"),
	REDSTONE("redstone");

	public static final String NBT_KEY = "BalanceMode";
	public static final BalanceMode DEFAULT = REDSTONE;

	private final String serializedName;

	BalanceMode(String serializedName) {
		this.serializedName = serializedName;
	}

	public String getSerializedName() {
		return serializedName;
	}

	public String getTranslationKey() {
		return "totf.balance.mode." + serializedName;
	}

	public BalanceMode next() {
		return this == VENDING ? REDSTONE : VENDING;
	}

	public static BalanceMode fromString(String value) {
		for (BalanceMode mode : values()) {
			if (mode.serializedName.equalsIgnoreCase(value)) {
				return mode;
			}
		}
		return DEFAULT;
	}

	public static BalanceMode fromStack(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return DEFAULT;
		}
		return fromString(stack.getTagCompound().getString(NBT_KEY));
	}

	public static void applyToStack(ItemStack stack, BalanceMode mode) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}
		tag.setString(NBT_KEY, mode.getSerializedName());
	}
}
