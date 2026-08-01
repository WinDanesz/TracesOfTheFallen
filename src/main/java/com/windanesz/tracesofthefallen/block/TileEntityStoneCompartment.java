package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.api.ILampheadInteractable;
import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TileEntityStoneCompartment extends TileEntity implements ITickable, ILampheadInteractable {
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
    public final ItemStackHandler inventory = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private EntityLamphead connectedLamphead = null;
    private int interactProgress = 0;
    private final int MAX_PROGRESS = 120; // 6 seconds
    private BlockPos interactionPos = null;
    private float interactionFacing = 0;

    @Override
    public void update() {
        if (world.isRemote) return;

        if (world.getBlockState(pos).getBlock() instanceof BlockStoneCompartment) {
            int currentState = world.getBlockState(pos).getValue(BlockStoneCompartment.OPEN);
            
            if (connectedLamphead != null && connectedLamphead.isEntityAlive() && connectedLamphead.isFabricating()) {
                if (currentState < 3) {
                    interactProgress++;
                    
                    int targetState = (interactProgress * 3) / MAX_PROGRESS; // 3 steps: 1, 2, 3
                    if (targetState > 3) targetState = 3;
                    
                    if (currentState != targetState) {
                        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockStoneCompartment.OPEN, targetState), 3);
                    }
                }
            }
        }
    }

    @Override
    public boolean canLampheadInteract(EntityLamphead lamphead) {
        if (this.connectedLamphead != null && this.connectedLamphead != lamphead && this.connectedLamphead.isEntityAlive()) {
            return false;
        }
        
        if (world.getBlockState(pos).getBlock() instanceof BlockStoneCompartment) {
            int currentState = world.getBlockState(pos).getValue(BlockStoneCompartment.OPEN);
            if (currentState == 0) {
                // Find a valid spot to stand
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    BlockPos p = pos.offset(facing);
                    if (world.isAirBlock(p) && world.isAirBlock(p.up()) && world.getBlockState(p.down()).isOpaqueCube()) {
                        this.interactionPos = p;
                        this.interactionFacing = facing.getOpposite().getHorizontalAngle();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getInteractionPriority() {
        return 5; // Higher than Fabricator
    }

    @Override
    public void startInteraction(EntityLamphead lamphead) {
        this.connectedLamphead = lamphead;
        this.interactProgress = 0;
    }

    @Override
    public void onLampheadInteractTick(EntityLamphead lamphead) {
        // Handled in update()
    }

    @Override
    public boolean isInteractionComplete(EntityLamphead lamphead) {
        if (world.getBlockState(pos).getBlock() instanceof BlockStoneCompartment) {
            return world.getBlockState(pos).getValue(BlockStoneCompartment.OPEN) == 3;
        }
        return true;
    }

    @Override
    public void stopInteraction(EntityLamphead lamphead) {
        if (this.connectedLamphead == lamphead) {
            this.connectedLamphead = null;
        }
    }

    @Override
    public BlockPos getInteractionPosition() {
        return this.interactionPos != null ? this.interactionPos : pos.north();
    }

    @Override
    public float getInteractionFacing() {
        return this.interactionFacing;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Inventory")) {
            inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
        }
        interactProgress = compound.getInteger("InteractProgress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Inventory", inventory.serializeNBT());
        compound.setInteger("InteractProgress", interactProgress);
        return compound;
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }
}
