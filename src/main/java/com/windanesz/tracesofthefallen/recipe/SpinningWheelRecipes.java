package com.windanesz.tracesofthefallen.recipe;

import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
        add("silk_spindle", stack -> stack.getItem() == Items.STRING, Collections.singletonList(new ItemStack(Items.STRING)), 32,
                new ItemStack(ModItems.silk_spindle), 1, true);
        add("silk_rope", stack -> stack.getItem() == ModItems.silk_spindle, Collections.singletonList(new ItemStack(ModItems.silk_spindle)), 1,
                new ItemStack(ModItems.silk_rope, 16), 0, true);
    }

    public static void add(String name, Predicate<ItemStack> input, int inputCount,
                           ItemStack output, int stickCount, boolean requiresSpinning) {
        add(name, input, Collections.emptyList(), inputCount, output, stickCount, requiresSpinning);
    }

    /** @param displayInputs example stacks matching the input predicate, shown in recipe viewers. */
    public static void add(String name, Predicate<ItemStack> input, List<ItemStack> displayInputs, int inputCount,
                           ItemStack output, int stickCount, boolean requiresSpinning) {
        initialize();
        if (name == null || name.trim().isEmpty() || input == null || inputCount < 1 || inputCount > 64
                || output == null || output.isEmpty() || output.getCount() > output.getMaxStackSize()
                || stickCount < 0 || stickCount > 64) {
            throw new IllegalArgumentException("Invalid spinning wheel recipe");
        }
        // Replacing a name also moves it to the highest matching priority.
        RECIPES.remove(name);
        RECIPES.put(name, new Recipe(input, displayInputs, inputCount, output, stickCount, requiresSpinning));
    }

    public static void remove(String name) {
        initialize();
        RECIPES.remove(name);
    }

    public static void removeAll() {
        initialize();
        RECIPES.clear();
    }

    public static List<Recipe> getRecipes() {
        initialize();
        return new ArrayList<>(RECIPES.values());
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
        private final List<ItemStack> displayInputs;
        public final int inputCount;
        public final int stickCount;
        private final ItemStack output;
        public final boolean requiresSpinning;

        private Recipe(Predicate<ItemStack> input, List<ItemStack> displayInputs, int inputCount, ItemStack output,
                       int stickCount, boolean requiresSpinning) {
            this.input = input;
            this.displayInputs = new ArrayList<>();
            for (ItemStack stack : displayInputs) {
                if (!stack.isEmpty()) this.displayInputs.add(stack.copy());
            }
            this.inputCount = inputCount;
            this.output = output.copy();
            this.stickCount = stickCount;
            this.requiresSpinning = requiresSpinning;
        }

        /** Example input stacks (with the required count applied); may be empty for recipes added without them. */
        public List<ItemStack> getDisplayInputs() {
            List<ItemStack> copies = new ArrayList<>();
            for (ItemStack stack : displayInputs) {
                ItemStack copy = stack.copy();
                copy.setCount(inputCount);
                copies.add(copy);
            }
            return copies;
        }

        public ItemStack getOutput() {
            return output.copy();
        }
    }
}
