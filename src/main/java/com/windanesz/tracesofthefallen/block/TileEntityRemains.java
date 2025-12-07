package com.windanesz.tracesofthefallen.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRemains extends TileEntity {

	private int shovelClicks = 0;

	public int getShovelClicks() {
		return shovelClicks;
	}

	public void incrementShovelClicks() {
		this.shovelClicks++;
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("ShovelClicks")) {
			this.shovelClicks = compound.getInteger("ShovelClicks");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("ShovelClicks", this.shovelClicks);
		return compound;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}
}