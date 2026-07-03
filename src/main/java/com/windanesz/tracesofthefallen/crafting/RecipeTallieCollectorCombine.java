package com.windanesz.tracesofthefallen.crafting;

import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.item.ItemTallieCollector;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeTallieCollectorCombine extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		return collectAndCount(inv) == 2;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		int collectors = 0;
		long totalTallies = 0;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.getItem() != ModItems.tally_collector) {
				return ItemStack.EMPTY;
			}

			collectors++;
			totalTallies += ItemTallieCollector.getTallieCount(stack);
		}

		if (collectors != 2) {
			return ItemStack.EMPTY;
		}

		ItemStack result = new ItemStack(ModItems.tally_collector);
		ItemTallieCollector.setTallieCount(result, totalTallies);
		return result;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ModItems.tally_collector);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	private int collectAndCount(InventoryCrafting inv) {
		int collectors = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.getItem() != ModItems.tally_collector) {
				return 0;
			}
			collectors++;
		}
		return collectors;
	}
}
