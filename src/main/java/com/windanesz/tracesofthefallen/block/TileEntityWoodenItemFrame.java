package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileEntityWoodenItemFrame extends TileEntity {

	private ItemStack displayedItem = ItemStack.EMPTY;
	private int itemRotation = 0;

	public ItemStack getDisplayedItem() {
		return displayedItem;
	}

	public void setDisplayedItem(ItemStack displayedItem) {
		this.displayedItem = displayedItem;
		this.markDirty();
		if (world != null && !world.isRemote) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	public int getItemRotation() {
		return itemRotation;
	}

	public void setItemRotation(int itemRotation) {
		this.itemRotation = itemRotation;
		this.markDirty();
		if (world != null && !world.isRemote) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("DisplayedItem")) {
			this.displayedItem = new ItemStack(compound.getCompoundTag("DisplayedItem"));
		} else {
			this.displayedItem = ItemStack.EMPTY;
		}
		this.itemRotation = compound.getInteger("ItemRotation");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (!this.displayedItem.isEmpty()) {
			compound.setTag("DisplayedItem", this.displayedItem.writeToNBT(new NBTTagCompound()));
		}
		compound.setInteger("ItemRotation", this.itemRotation);
		return compound;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
}
