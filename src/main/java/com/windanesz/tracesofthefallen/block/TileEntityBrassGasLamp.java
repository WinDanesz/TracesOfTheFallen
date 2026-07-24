package com.windanesz.tracesofthefallen.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class TileEntityBrassGasLamp extends TileEntity {
    private int lightLevel = 0; // 0 for 10, 1 for 11, 2 for 12

    public int getLampLightLevel() {
        return lightLevel;
    }

    public void cycleLightLevel() {
        lightLevel = (lightLevel + 1) % 3;
        this.markDirty();
        if (this.world != null) {
            this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
            this.world.checkLight(this.pos);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("LightLevel", lightLevel);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("LightLevel")) {
            this.lightLevel = compound.getInteger("LightLevel");
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        if (this.world != null) {
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
            this.world.checkLight(this.pos);
        }
    }
}
