package com.windanesz.tracesofthefallen.compat.jei;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.recipe.SpinningWheelRecipes;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpinningWheelCategory implements IRecipeCategory<SpinningWheelCategory.Wrapper> {

    public static final String UID = TracesOfTheFallen.MODID + ".spinning_wheel";

    private static final int INPUT_SLOT = 0;
    private static final int STICK_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public SpinningWheelCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(130, 58);
        icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.spinning_wheel));
        slot = guiHelper.getSlotDrawable();
        arrow = guiHelper.createDrawable(new ResourceLocation("textures/gui/container/furnace.png"), 79, 34, 24, 17);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.totf:spinning_wheel.title");
    }

    @Override
    public String getModName() {
        return TracesOfTheFallen.MODNAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 8, 8);
        slot.draw(minecraft, 8, 30);
        slot.draw(minecraft, 104, 19);
        arrow.draw(minecraft, 74, 20);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, Wrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(INPUT_SLOT, true, 8, 8);
        stacks.init(STICK_SLOT, true, 8, 30);
        stacks.init(OUTPUT_SLOT, false, 104, 19);
        stacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if (slotIndex == INPUT_SLOT) {
                tooltip.add(TextFormatting.GRAY + I18n.format("jei.totf:spinning_wheel.hand.tooltip"));
            } else if (slotIndex == STICK_SLOT) {
                tooltip.add(TextFormatting.GRAY + I18n.format("jei.totf:spinning_wheel.wheel.tooltip"));
            }
        });
        stacks.set(ingredients);
    }

    public static List<Wrapper> getRecipes() {
        List<Wrapper> wrappers = new ArrayList<>();
        for (SpinningWheelRecipes.Recipe recipe : SpinningWheelRecipes.getRecipes()) {
            // Recipes without example inputs (e.g. added by script with an opaque predicate) cannot be displayed.
            if (!recipe.getDisplayInputs().isEmpty()) {
                wrappers.add(new Wrapper(recipe));
            }
        }
        return wrappers;
    }

    public static class Wrapper implements IRecipeWrapper {
        private final SpinningWheelRecipes.Recipe recipe;

        Wrapper(SpinningWheelRecipes.Recipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<List<ItemStack>> inputs = new ArrayList<>();
            inputs.add(recipe.getDisplayInputs());
            // Always keep the stick slot index stable, even when the recipe uses no sticks.
            inputs.add(recipe.stickCount > 0
                    ? Collections.singletonList(new ItemStack(Items.STICK, recipe.stickCount))
                    : Collections.<ItemStack>emptyList());
            ingredients.setInputLists(ItemStack.class, inputs);
            ingredients.setOutput(ItemStack.class, recipe.getOutput());
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            minecraft.fontRenderer.drawString(I18n.format("jei.totf:spinning_wheel.hand"), 28, 13, 0x404040);
            if (recipe.stickCount > 0) {
                minecraft.fontRenderer.drawString(I18n.format("jei.totf:spinning_wheel.wheel"), 28, 35, 0x404040);
            }
            if (recipe.requiresSpinning) {
                String text = I18n.format("jei.totf:spinning_wheel.requires_spinning");
                minecraft.fontRenderer.drawString(text, (recipeWidth - minecraft.fontRenderer.getStringWidth(text)) / 2,
                        recipeHeight - 9, 0x808080);
            }
        }

        @Override
        public List<String> getTooltipStrings(int mouseX, int mouseY) {
            if (recipe.requiresSpinning && mouseY >= 49) {
                return Collections.singletonList(I18n.format("jei.totf:spinning_wheel.requires_spinning.tooltip"));
            }
            return Collections.emptyList();
        }
    }
}
