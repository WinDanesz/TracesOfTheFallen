package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntityStoneReceiver extends TileEntity implements ITickable {

    private int ticksRemaining = 0;
    private boolean active = false;

    @Override
    public void update() {
        if (!world.isRemote) {
            if (active) {
                if (ticksRemaining > 0) {
                    ticksRemaining--;
                }
                if (ticksRemaining <= 0) {
                    active = false;
                    IBlockState state = world.getBlockState(pos);
                    if (state.getBlock() instanceof BlockStoneReceiver) {
                        world.setBlockState(pos, state.withProperty(BlockStoneReceiver.POWERED, false), 3);
                        world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                    }
                }
            }
        }
    }

    public void trigger() {
        if (!world.isRemote) {
            this.ticksRemaining = Settings.miscSettings.stoneReceiverPulseDurationTicks;
            if (!active) {
                this.active = true;
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockStoneReceiver) {
                    world.setBlockState(pos, state.withProperty(BlockStoneReceiver.POWERED, true), 3);
                    world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("TicksRemaining", ticksRemaining);
        compound.setBoolean("Active", active);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.ticksRemaining = compound.getInteger("TicksRemaining");
        this.active = compound.getBoolean("Active");
    }
}
