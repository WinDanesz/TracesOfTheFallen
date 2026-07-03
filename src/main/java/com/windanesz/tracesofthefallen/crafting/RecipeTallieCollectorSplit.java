package com.windanesz.tracesofthefallen.crafting;

import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.item.ItemTallieCollector;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeTallieCollectorSplit extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		StackSlot stackSlot = findSingleCollector(inv);
		if (stackSlot == null) {
			return false;
		}

		long count = ItemTallieCollector.getTallieCount(stackSlot.stack);
		return count >= 2;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		StackSlot stackSlot = findSingleCollector(inv);
		if (stackSlot == null) {
			return ItemStack.EMPTY;
		}

		long count = ItemTallieCollector.getTallieCount(stackSlot.stack);
		if (count < 2) {
			return ItemStack.EMPTY;
		}

		ItemStack result = new ItemStack(ModItems.tally_collector);
		ItemTallieCollector.setTallieCount(result, (count / 2) + (count % 2));
		return result;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ModItems.tally_collector);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		StackSlot stackSlot = findSingleCollector(inv);
		if (stackSlot == null) {
			return remaining;
		}

		long count = ItemTallieCollector.getTallieCount(stackSlot.stack);
		if (count < 2) {
			return remaining;
		}

		ItemStack half = new ItemStack(ModItems.tally_collector);
		ItemTallieCollector.setTallieCount(half, count / 2);
		remaining.set(stackSlot.slot, half);
		return remaining;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	private StackSlot findSingleCollector(InventoryCrafting inv) {
		StackSlot found = null;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.getItem() != ModItems.tally_collector) {
				return null;
			}
			if (found != null) {
				return null;
			}
			found = new StackSlot(i, stack);
		}
		return found;
	}

	private static final class StackSlot {
		final int slot;
		final ItemStack stack;

		StackSlot(int slot, ItemStack stack) {
			this.slot = slot;
			this.stack = stack;
		}
	}
}
