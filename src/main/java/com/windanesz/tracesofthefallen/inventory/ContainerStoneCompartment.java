package com.windanesz.tracesofthefallen.inventory;

import com.windanesz.tracesofthefallen.block.TileEntityStoneCompartment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerStoneCompartment extends Container {
    private final TileEntityStoneCompartment te;

    public ContainerStoneCompartment(EntityPlayer player, TileEntityStoneCompartment te) {
        this.te = te;
        
        IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        
        // 4x2 slots for the stone compartment
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                this.addSlotToContainer(new SlotItemHandler(inventory, j + i * 4, 53 + j * 18, 20 + i * 18));
            }
        }
        
        // Player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        
        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(this.te.getPos().getX() + 0.5D, this.te.getPos().getY() + 0.5D, this.te.getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 8) {
                // Move from compartment to player
                if (!this.mergeItemStack(itemstack1, 8, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 8, false)) {
                // Move from player to compartment
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
}
