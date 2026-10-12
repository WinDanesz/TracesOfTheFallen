package com.windanesz.tracesofthefallen.recipe;

import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Recipes for the item held when right-clicking a spinning wheel. */
public final class SpinningWheelRecipes {
    private static final Map<String, Recipe> RECIPES = new LinkedHashMap<>();
    private static boolean initialized;

    private SpinningWheelRecipes() {}

    private static void initialize() {
        if (initialized) return;
        initialized = true;
        add("silk_spindle", stack -> stack.getItem() == Items.STRING, 32,
                new ItemStack(ModItems.silk_spindle), 1, true);
        add("silk_rope", stack -> stack.getItem() == ModItems.silk_spindle, 1,
                new ItemStack(ModItems.silk_rope, 16), 0, false);
    }

    public static void add(String name, Predicate<ItemStack> input, int inputCount,
                           ItemStack output, int stickCount, boolean requiresSpinning) {
        initialize();
        if (name == null || name.trim().isEmpty() || input == null || inputCount < 1 || inputCount > 64
                || output == null || output.isEmpty() || output.getCount() > output.getMaxStackSize()
                || stickCount < 0 || stickCount > 64) {
            throw new IllegalArgumentException("Invalid spinning wheel recipe");
        }
        // Replacing a name also moves it to the highest matching priority.
        RECIPES.remove(name);
        RECIPES.put(name, new Recipe(input, inputCount, output, stickCount, requiresSpinning));
    }

    public static void remove(String name) {
        initialize();
        RECIPES.remove(name);
    }

    public static void removeAll() {
        initialize();
        RECIPES.clear();
    }

    public static boolean isInput(ItemStack stack) {
        initialize();
        return !stack.isEmpty() && RECIPES.values().stream().anyMatch(recipe -> recipe.input.test(stack));
    }

    public static Recipe find(ItemStack stack, int sticks, boolean spinning) {
        initialize();
        Recipe found = null;
        if (!stack.isEmpty()) {
            for (Recipe recipe : RECIPES.values()) {
                if (stack.getCount() >= recipe.inputCount && sticks >= recipe.stickCount
                        && (!recipe.requiresSpinning || spinning) && recipe.input.test(stack)) {
                    found = recipe;
                }
            }
        }
        return found;
    }

    public static final class Recipe {
        private final Predicate<ItemStack> input;
        public final int inputCount;
        public final int stickCount;
        private final ItemStack output;
        public final boolean requiresSpinning;

        private Recipe(Predicate<ItemStack> input, int inputCount, ItemStack output,
                       int stickCount, boolean requiresSpinning) {
            this.input = input;
            this.inputCount = inputCount;
            this.output = output.copy();
            this.stickCount = stickCount;
            this.requiresSpinning = requiresSpinning;
        }

        public ItemStack getOutput() {
            return output.copy();
        }
    }
}
