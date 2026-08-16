package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityArmillary extends TileEntity implements ITickable {

	private int lastHauntingLevel = 0;
	private UUID lastPlayerUUID = null;
	private int tickCount = 0;
	private float currentClickSpinSpeed = 0.0F;
	
	private float innerAngle = 0.0F;
	private float outerAngle = 0.0F;

	@Override
	public void update() {
		if (world != null && !world.isRemote && lastPlayerUUID != null) {
			tickCount++;
			if (tickCount >= 200) {
				tickCount = 0;
				EntityPlayer player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(lastPlayerUUID);
				if (player != null) {
					HauntingCapability cap = HauntingCapability.get(player);
					if (cap != null) {
						int newLevel = cap.getHauntingProgress();
						if (newLevel != this.lastHauntingLevel) {
							this.lastHauntingLevel = newLevel;
							onContentsChanged();
						}
					}
				} else {
					if (this.lastHauntingLevel >= 50) {
						this.lastHauntingLevel = 0;
						onContentsChanged();
					}
				}
			}
		}

		// Calculate base speed from haunting level (Threshold = 50)
		float hauntingSpeed = 0.0F;
		if (lastHauntingLevel >= 50) {
			// At 50 haunting, speed is 6.0F. Scales up to 18.0F at 100 haunting.
			hauntingSpeed = 6.0F + ((lastHauntingLevel - 50) / 50.0F) * 12.0F;
		}

		// Click spin speed decays over time
		if (currentClickSpinSpeed > 0) {
			currentClickSpinSpeed -= 0.05F; // Decay speed
			if (currentClickSpinSpeed < 0) {
				currentClickSpinSpeed = 0.0F;
			}
		}

		float totalSpeed = hauntingSpeed + currentClickSpinSpeed;

		if (totalSpeed > 0) {
			innerAngle += totalSpeed * 1.5F; // Inner spins a bit faster
			outerAngle += totalSpeed;
			
			if (innerAngle >= 360.0F) innerAngle -= 360.0F;
			if (outerAngle >= 360.0F) outerAngle -= 360.0F;
		} else {
			// Gracefully complete the spin to return to 0 (default rotation)
			if (innerAngle > 0.0F) {
				innerAngle += 2.0F;
				if (innerAngle >= 360.0F) innerAngle = 0.0F;
			}
			if (outerAngle > 0.0F) {
				outerAngle += 2.0F;
				if (outerAngle >= 360.0F) outerAngle = 0.0F;
			}
		}
	}

	public void onRightClick(EntityPlayer player, int hauntingLevel) {
		this.lastPlayerUUID = player.getUniqueID();
		this.lastHauntingLevel = hauntingLevel;
		if (hauntingLevel < 50) {
			this.currentClickSpinSpeed = 3.0F; // Slow, limited initial burst of speed
		}
		onContentsChanged();
	}

	public float getInnerAngle() {
		return innerAngle;
	}

	public float getOuterAngle() {
		return outerAngle;
	}

	public void onContentsChanged() {
		markDirty();
		if (world != null && !world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (lastPlayerUUID != null) {
			compound.setUniqueId("LastPlayerUUID", lastPlayerUUID);
		}
		compound.setInteger("LastHauntingLevel", lastHauntingLevel);
		compound.setFloat("ClickSpinSpeed", currentClickSpinSpeed);
		compound.setFloat("InnerAngle", innerAngle);
		compound.setFloat("OuterAngle", outerAngle);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasUniqueId("LastPlayerUUID")) {
			lastPlayerUUID = compound.getUniqueId("LastPlayerUUID");
		}
		lastHauntingLevel = compound.getInteger("LastHauntingLevel");
		currentClickSpinSpeed = compound.getFloat("ClickSpinSpeed");
		innerAngle = compound.getFloat("InnerAngle");
		outerAngle = compound.getFloat("OuterAngle");
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
