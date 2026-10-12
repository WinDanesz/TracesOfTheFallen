package com.windanesz.tracesofthefallen.compat.crafttweaker;

import com.windanesz.tracesofthefallen.recipe.SpinningWheelRecipes;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/** Discovered by CraftTweaker only when that optional mod is installed. */
@ZenRegister
@ZenClass("mods.totf.SpinningWheel")
public final class SpinningWheel {
    private SpinningWheel() {}

    @ZenMethod
    public static void addRecipe(String name, IItemStack output, IIngredient input,
                                 int inputCount, int stickCount, boolean requiresSpinning) {
        ItemStack result = output == null ? ItemStack.EMPTY : CraftTweakerMC.getItemStack(output).copy();
        if (name == null || name.trim().isEmpty() || input == null || inputCount < 1 || inputCount > 64
                || result.isEmpty() || result.getCount() > result.getMaxStackSize()
                || stickCount < 0 || stickCount > 64) {
            CraftTweakerAPI.logError("Invalid spinning wheel recipe: " + name);
            return;
        }
        apply("Add spinning wheel recipe " + name, () -> SpinningWheelRecipes.add(name,
                stack -> input.matches(CraftTweakerMC.getIItemStack(stack)), inputCount,
                result, stickCount, requiresSpinning));
    }

    @ZenMethod
    public static void removeRecipe(String name) {
        apply("Remove spinning wheel recipe " + name, () -> SpinningWheelRecipes.remove(name));
    }

    @ZenMethod
    public static void removeAll() {
        apply("Remove all spinning wheel recipes", SpinningWheelRecipes::removeAll);
    }

    private static void apply(String description, Runnable action) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() { action.run(); }

            @Override
            public String describe() { return description; }
        });
    }
}
