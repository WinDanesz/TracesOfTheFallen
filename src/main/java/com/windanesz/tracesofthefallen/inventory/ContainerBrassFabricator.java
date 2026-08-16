package com.windanesz.tracesofthefallen.inventory;

import com.windanesz.tracesofthefallen.block.TileEntityBrassFabricator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerBrassFabricator extends Container {
    
    private TileEntityBrassFabricator te;
    
    // Dummy constructor for TileEntity's InventoryCrafting
    public ContainerBrassFabricator() {
    }
    
    // Used by TileEntity
    public ContainerBrassFabricator(IItemHandler handler) {
    }

    public ContainerBrassFabricator(EntityPlayer player, TileEntityBrassFabricator te) {
        this.te = te;
        IItemHandler inventory = te.inventory;

        // Crafting Grid (0-8)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlotToContainer(new SlotItemHandler(inventory, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        // Output Slots (9-11)
        this.addSlotToContainer(new SlotItemHandler(inventory, 9, 124, 15) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return false;
            }
        });
        this.addSlotToContainer(new SlotItemHandler(inventory, 10, 124, 35) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return false;
            }
        });
        this.addSlotToContainer(new SlotItemHandler(inventory, 11, 124, 55) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return false;
            }
        });

        // Fuel Slots (12-17)
        int[] fuelX = {12, 39, 66, 94, 121, 148};
        for (int i = 0; i < 6; i++) {
            this.addSlotToContainer(new SlotItemHandler(inventory, 12 + i, fuelX[i], 91));
        }

        // Player Inventory
        int playerInvY = 115;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, playerInvY + i * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, playerInvY + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        if (te != null) {
            return playerIn.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 64.0D;
        }
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 18) {
                if (!this.mergeItemStack(itemstack1, 18, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    private int burnTime;
    private int maxBurnTime;
    private int speedLevel;
    private int craftProgress;

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        
        if (te == null) return;
        
        for (int i = 0; i < this.listeners.size(); ++i) {
            IContainerListener listener = this.listeners.get(i);
            
            if (this.burnTime != te.burnTime) {
                listener.sendWindowProperty(this, 0, te.burnTime);
            }
            if (this.maxBurnTime != te.maxBurnTime) {
                listener.sendWindowProperty(this, 1, te.maxBurnTime);
            }
            if (this.speedLevel != te.speedLevel) {
                listener.sendWindowProperty(this, 2, te.speedLevel);
            }
            if (this.craftProgress != te.craftProgress) {
                listener.sendWindowProperty(this, 3, te.craftProgress);
            }
        }
        
        this.burnTime = te.burnTime;
        this.maxBurnTime = te.maxBurnTime;
        this.speedLevel = te.speedLevel;
        this.craftProgress = te.craftProgress;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (te == null) return;
        
        if (id == 0) te.burnTime = data;
        else if (id == 1) te.maxBurnTime = data;
        else if (id == 2) te.speedLevel = data;
        else if (id == 3) te.craftProgress = data;
    }
}
