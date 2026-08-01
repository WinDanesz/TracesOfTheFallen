package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityGuillotine extends TileEntity implements ITickable {
    private boolean active = false;
    private int activeTicks = 0;
    private int extensionLength = 0;
    private boolean poweredLastTick = false;

    @Override
    public void update() {
        if (active) {
            if (activeTicks > 0) {
                activeTicks--;
            } else {
                active = false;
                extensionLength = 0;
                markDirty();
                if (!world.isRemote) {
                    IBlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
            }
        }
    }

    public void activate() {
        if (!world.isRemote) {
            active = true;
            activeTicks = 600; // 30 seconds
            calculateExtensionAndDamage();
            markDirty();
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private void calculateExtensionAndDamage() {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockGuillotine)) return;
        
        EnumFacing facing = state.getValue(BlockGuillotine.FACING);
        extensionLength = 0;

        for (int i = 1; i <= 5; i++) {
            BlockPos targetPos = pos.offset(facing, i);
            IBlockState targetState = world.getBlockState(targetPos);
            Block targetBlock = targetState.getBlock();
            
            // If it's a solid block, we stop extending
            if (targetState.getMaterial().isSolid()) {
                break;
            }
            
            // Break soft blocks (grass, crops, etc.)
            if (!targetBlock.isAir(targetState, world, targetPos)) {
                world.destroyBlock(targetPos, true);
            }
            
            extensionLength = i;
        }

        // Damage entities in the path
        if (extensionLength > 0) {
            AxisAlignedBB damageBox = new AxisAlignedBB(pos).offset(facing.getDirectionVec().getX(), facing.getDirectionVec().getY(), facing.getDirectionVec().getZ());
            if (extensionLength > 1) {
                damageBox = damageBox.union(new AxisAlignedBB(pos).offset(facing.getDirectionVec().getX() * extensionLength, facing.getDirectionVec().getY() * extensionLength, facing.getDirectionVec().getZ() * extensionLength));
            }
            
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, damageBox);
            for (EntityLivingBase entity : entities) {
                entity.attackEntityFrom(DamageSource.CACTUS, 10.0F); // Custom damage source could be used
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getExtensionLength() {
        return extensionLength;
    }

    public boolean isPoweredLastTick() {
        return poweredLastTick;
    }

    public void setPoweredLastTick(boolean poweredLastTick) {
        this.poweredLastTick = poweredLastTick;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Active", active);
        compound.setInteger("ActiveTicks", activeTicks);
        compound.setInteger("ExtensionLength", extensionLength);
        compound.setBoolean("PoweredLastTick", poweredLastTick);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        active = compound.getBoolean("Active");
        activeTicks = compound.getInteger("ActiveTicks");
        extensionLength = compound.getInteger("ExtensionLength");
        poweredLastTick = compound.getBoolean("PoweredLastTick");
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
