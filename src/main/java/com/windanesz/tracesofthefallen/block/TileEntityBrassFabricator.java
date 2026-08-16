package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.api.ILampheadInteractable;
import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import com.windanesz.tracesofthefallen.inventory.ContainerBrassFabricator;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TileEntityBrassFabricator extends TileEntity implements ITickable, ILampheadInteractable {
    
    public EntityLamphead connectedLamphead = null;
    
    // 0-8: Crafting Grid
    // 9-11: Output Slots (top, middle, bottom)
    // 12-17: Fuel slots
    public ItemStackHandler inventory = new ItemStackHandler(18) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            if (world != null && !world.isRemote) {
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            }
        }
    };
    
    // Custom container to satisfy InventoryCrafting requirements
    private final ContainerBrassFabricator dummyContainer = new ContainerBrassFabricator(inventory);
    private final InventoryCrafting craftMatrix = new InventoryCrafting(dummyContainer, 3, 3);
    
    public int burnTime = 0;
    public int maxBurnTime = 0;
    public int speedLevel = 0; // 1 to 6
    public int craftProgress = 0;
    
    @Override
    public void update() {
        if (world.isRemote) return;
        
        boolean dirty = false;
        
        // Sync craft matrix
        for (int i = 0; i < 9; i++) {
            craftMatrix.setInventorySlotContents(i, inventory.getStackInSlot(i));
        }
        
        ItemStack recipeOutput = CraftingManager.findMatchingResult(craftMatrix, world);
        boolean hasRecipe = !recipeOutput.isEmpty();
        boolean canOutput = false;
        
        if (hasRecipe) {
            for (int i = 9; i <= 11; i++) {
                ItemStack outStack = inventory.getStackInSlot(i);
                if (outStack.isEmpty()) {
                    canOutput = true;
                    break;
                }
                if (outStack.isItemEqual(recipeOutput) && outStack.getCount() + recipeOutput.getCount() <= outStack.getMaxStackSize()) {
                    canOutput = true;
                    break;
                }
            }
        }
        
        boolean isLampheadActive = connectedLamphead != null && connectedLamphead.isFabricating();
        
        if (burnTime > 0) {
            burnTime--;
            dirty = true;
        }
        
        if (isLampheadActive && hasRecipe && canOutput) {
            if (burnTime == 0) {
                // Try to consume fuel
                int consumedCount = 0;
                int highestBurn = 0;
                for (int i = 12; i <= 17; i++) {
                    ItemStack fuel = inventory.getStackInSlot(i);
                    if (!fuel.isEmpty()) {
                        int itemBurn = getFuelValue(fuel);
                        if (itemBurn > 0) {
                            consumedCount++;
                            if (itemBurn > highestBurn) {
                                highestBurn = itemBurn;
                            }
                            fuel.shrink(1);
                        }
                    }
                }
                
                if (consumedCount > 0) {
                    speedLevel = consumedCount;
                    maxBurnTime = highestBurn;
                    burnTime = highestBurn;
                    dirty = true;
                }
            }
            
            if (burnTime > 0) {
                craftProgress++;
                int targetTicks = 180 - (speedLevel * 20); // 1 fuel = 160 ticks (8s), 6 fuels = 60 ticks (3s)
                if (craftProgress >= targetTicks) {
                    craftItem(recipeOutput);
                    craftProgress = 0;
                }
                dirty = true;
            } else {
                // Lamphead is animating but no fuel
                craftProgress = 0;
            }
        } else {
            if (craftProgress > 0) {
                craftProgress = 0;
                dirty = true;
            }
        }
        
        if (dirty) {
            markDirty();
        }
    }
    
    private static Map<String, Integer> customFuelCache = null;

    public static void clearFuelCache() {
        customFuelCache = null;
    }

    private int getFuelValue(ItemStack stack) {
        if (customFuelCache == null) {
            customFuelCache = new HashMap<>();
            for (String entry : Settings.miscSettings.fabricatorCustomFuels) {
                String[] parts = entry.split("\\|");
                if (parts.length == 2) {
                    try {
                        customFuelCache.put(parts[0], Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        
        String name = stack.getItem().getRegistryName().toString();
        String nameWithMeta = name + ":" + stack.getMetadata();
        
        if (customFuelCache.containsKey(nameWithMeta)) {
            return customFuelCache.get(nameWithMeta);
        } else if (customFuelCache.containsKey(name)) {
            return customFuelCache.get(name);
        }
        
        return TileEntityFurnace.getItemBurnTime(stack);
    }
    
    private void craftItem(ItemStack result) {
        // Find output slot (top to bottom: 9 to 11)
        for (int i = 9; i <= 11; i++) {
            ItemStack outStack = inventory.getStackInSlot(i);
            if (outStack.isEmpty()) {
                inventory.setStackInSlot(i, result.copy());
                consumeIngredients();
                return;
            } else if (outStack.isItemEqual(result) && outStack.getCount() + result.getCount() <= outStack.getMaxStackSize()) {
                outStack.grow(result.getCount());
                consumeIngredients();
                return;
            }
        }
    }
    
    private void consumeIngredients() {
        NonNullList<ItemStack> remainingItems = CraftingManager.getRemainingItems(craftMatrix, world);
        for (int i = 0; i < 9; ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            ItemStack remaining = remainingItems.get(i);

            if (!itemstack.isEmpty()) {
                inventory.extractItem(i, 1, false);
                itemstack = inventory.getStackInSlot(i);
            }

            if (!remaining.isEmpty()) {
                if (itemstack.isEmpty()) {
                    inventory.setStackInSlot(i, remaining);
                } else if (ItemStack.areItemsEqual(itemstack, remaining) && ItemStack.areItemStackTagsEqual(itemstack, remaining)) {
                    remaining.grow(itemstack.getCount());
                    inventory.setStackInSlot(i, remaining);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Inventory")) {
            inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
        }
        burnTime = compound.getInteger("BurnTime");
        maxBurnTime = compound.getInteger("MaxBurnTime");
        speedLevel = compound.getInteger("SpeedLevel");
        craftProgress = compound.getInteger("CraftProgress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Inventory", inventory.serializeNBT());
        compound.setInteger("BurnTime", burnTime);
        compound.setInteger("MaxBurnTime", maxBurnTime);
        compound.setInteger("SpeedLevel", speedLevel);
        compound.setInteger("CraftProgress", craftProgress);
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

    private final IItemHandler handlerDown = new RangedWrapper(inventory, 9, 12) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }
    };

    private final IItemHandler handlerSides = new IItemHandler() {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot >= 9 && slot <= 11) return stack;
            if (inventory.getStackInSlot(slot).isEmpty()) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }
    };

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
            if (facing == EnumFacing.DOWN) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlerDown);
            }
            if (facing != null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlerSides);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean canLampheadInteract(EntityLamphead lamphead) {
        if (this.connectedLamphead != null && this.connectedLamphead != lamphead && this.connectedLamphead.isEntityAlive()) {
            return false;
        }
        ItemStack recipeOutput = CraftingManager.findMatchingResult(craftMatrix, world);
        if (!recipeOutput.isEmpty()) {
            for (int i = 9; i <= 11; i++) {
                ItemStack outStack = inventory.getStackInSlot(i);
                if (outStack.isEmpty() || (outStack.isItemEqual(recipeOutput) && outStack.getCount() + recipeOutput.getCount() <= outStack.getMaxStackSize())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getInteractionPriority() {
        boolean hasFuel = burnTime > 0;
        if (!hasFuel) {
            for (int i = 12; i <= 17; i++) {
                if (!inventory.getStackInSlot(i).isEmpty() && getFuelValue(inventory.getStackInSlot(i)) > 0) {
                    hasFuel = true;
                    break;
                }
            }
        }
        return hasFuel ? 4 : 3;
    }

    @Override
    public void startInteraction(EntityLamphead lamphead) {
        this.connectedLamphead = lamphead;
    }

    @Override
    public void onLampheadInteractTick(EntityLamphead lamphead) {
        // State updates are handled in update()
    }

    @Override
    public boolean isInteractionComplete(EntityLamphead lamphead) {
        return !canLampheadInteract(lamphead);
    }

    @Override
    public void stopInteraction(EntityLamphead lamphead) {
        if (this.connectedLamphead == lamphead) {
            this.connectedLamphead = null;
        }
    }

    @Override
    public BlockPos getInteractionPosition() {
        EnumFacing facing = world.getBlockState(pos).getValue(BlockBrassFabricator.FACING);
        return pos.offset(facing);
    }

    @Override
    public float getInteractionFacing() {
        return world.getBlockState(pos).getValue(BlockBrassFabricator.FACING).getOpposite().getHorizontalAngle();
    }
}
