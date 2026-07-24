package com.windanesz.tracesofthefallen.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class TileEntityWroughtCagedLamp extends TileEntity implements ITickable {
    private int flashTimer = 0;

    public static void triggerNearbyWroughtLamps(net.minecraft.world.World world, BlockPos pos) {
        int radius = 16;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos p = pos.add(x, y, z);
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof TileEntityWroughtCagedLamp) {
                        ((TileEntityWroughtCagedLamp) te).triggerFlash();
                    }
                }
            }
        }
    }

    public void triggerFlash() {
        this.flashTimer = 40; // 2 seconds flash
        this.markDirty();
        this.world.checkLight(this.pos);
        this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
    }

    public int getLightLevel() {
        return flashTimer > 0 ? 15 : 8;
    }

    @Override
    public void update() {
        if (flashTimer > 0) {
            flashTimer--;
            if (flashTimer == 0) {
                this.markDirty();
                this.world.checkLight(this.pos);
                this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("FlashTimer", flashTimer);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.flashTimer = compound.getInteger("FlashTimer");
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
