package com.windanesz.tracesofthefallen.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Holds a single ItemStack that can only be retrieved by breaking the pot (see BlockClayPot).
 * Deliberately exposes no IItemHandler capability, so hoppers and other automation cannot see
 * or extract the stored item while the pot is placed.
 */
public class TileEntityClayPot extends TileEntity {

	public static final String STORED_ITEM_TAG = "StoredItem";

	private ItemStack storedItem = ItemStack.EMPTY;

	public ItemStack getStoredItem() {
		return storedItem;
	}

	public void setStoredItem(ItemStack storedItem) {
		this.storedItem = storedItem;
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.storedItem = compound.hasKey(STORED_ITEM_TAG)
				? new ItemStack(compound.getCompoundTag(STORED_ITEM_TAG))
				: ItemStack.EMPTY;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (!this.storedItem.isEmpty()) {
			compound.setTag(STORED_ITEM_TAG, this.storedItem.writeToNBT(new NBTTagCompound()));
		}
		return compound;
	}
}
