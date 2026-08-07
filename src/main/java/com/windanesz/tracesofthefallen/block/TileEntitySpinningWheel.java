package com.windanesz.tracesofthefallen.block;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySpinningWheel extends TileEntity implements ITickable, IWorldNameable {

	@Override
	public String getName() {
		return "container.totf:spinning_wheel";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(getName());
	}

	private final ItemStackHandler stickInventory = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			TileEntitySpinningWheel.this.onContentsChanged();
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (stack.getItem() != Items.STICK) {
				return stack;
			}
			return super.insertItem(slot, stack, simulate);
		}
	};

	private int spinTimeRemaining = 0;
	private float spinAngle = 0.0F;

	@Override
	public void update() {
		if (spinTimeRemaining > 0) {
			spinTimeRemaining--;
			spinAngle += 20.0F; // Adjust speed as necessary
			if (spinAngle >= 1440.0F) {
				spinAngle -= 1440.0F;
			}
		} else {
			// Decelerate or snap
			if (spinAngle % 90.0F != 0) {
				spinAngle += 5.0F;
				if (spinAngle >= 1440.0F) {
					spinAngle -= 1440.0F;
				}
				// snap if close
				if (Math.abs((spinAngle % 90.0F)) <= 5.0F || Math.abs((spinAngle % 90.0F) - 90.0F) <= 5.0F) {
					spinAngle = Math.round(spinAngle / 90.0F) * 90.0F;
					if (spinAngle >= 1440.0F) {
						spinAngle -= 1440.0F;
					}
				}
			}
		}
	}

	public boolean hasStick() {
		ItemStack stack = stickInventory.getStackInSlot(0);
		return !stack.isEmpty() && stack.getItem() == Items.STICK && stack.getCount() >= 1;
	}

	public void consumeStick() {
		stickInventory.extractItem(0, 1, false);
	}

	public ItemStack insertStick(ItemStack stack) {
		return stickInventory.insertItem(0, stack, false);
	}

	public boolean isSpinning() {
		return spinTimeRemaining > 0;
	}

	public void startSpinning(int ticks) {
		this.spinTimeRemaining = ticks;
		onContentsChanged();
	}

	public float getSpinAngle() {
		return spinAngle;
	}

	public void onContentsChanged() {
		markDirty();
		if (world != null && !world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(stickInventory);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("Inventory", stickInventory.serializeNBT());
		compound.setInteger("SpinTimeRemaining", spinTimeRemaining);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("Inventory")) {
			stickInventory.deserializeNBT(compound.getCompoundTag("Inventory"));
		} else if (compound.hasKey("StoredSticks")) { // Backwards compat just in case
			stickInventory.setStackInSlot(0, new ItemStack(Items.STICK, compound.getInteger("StoredSticks")));
		}
		spinTimeRemaining = compound.getInteger("SpinTimeRemaining");
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
}
