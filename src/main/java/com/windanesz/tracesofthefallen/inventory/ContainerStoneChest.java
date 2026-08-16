package com.windanesz.tracesofthefallen.inventory;

import com.windanesz.tracesofthefallen.block.TileEntityStoneChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerStoneChest extends Container {
    private final TileEntityStoneChest te;

    public ContainerStoneChest(EntityPlayer player, TileEntityStoneChest te) {
        this.te = te;
        te.openInventory(player);
        
        // 3x9 slots for the stone chest
        // Topleft container slot is at 10x 12y
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(te, k + j * 9, 10 + k * 18, 12 + j * 18));
            }
        }
        
        // Player inventory
        // topleft player inventory slot is 10x 94y
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(player.inventory, j1 + l * 9 + 9, 10 + j1 * 18, 94 + l * 18));
            }
        }
        
        // Player hotbar
        // Usually 58 pixels below the player inventory Y (94 + 58 = 152)
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(player.inventory, i1, 10 + i1 * 18, 152));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(this.te.getPos().getX() + 0.5D, this.te.getPos().getY() + 0.5D, this.te.getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.te.closeInventory(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 27) { // 27 slots in chest
                if (!this.mergeItemStack(itemstack1, 27, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
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
