package com.windanesz.tracesofthefallen.compat.jei;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.SidheDiscoveries;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntitySidhe;
import com.windanesz.tracesofthefallen.init.ModItems;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lists the items a Sidhe accepts. What it gives back stays a secret until the player has seen it happen.
 * Inputs are the only thing registered as ingredients, so undiscovered rewards cannot be found by searching for them.
 */
public class SidheCategory implements IRecipeCategory<SidheCategory.Wrapper> {

    public static final String UID = TracesOfTheFallen.MODID + ".sidhe";

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public SidheCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(116, 40);
        icon = guiHelper.createDrawableIngredient(new ItemStack(ModItems.furred_trout));
        slot = guiHelper.getSlotDrawable();
        arrow = guiHelper.createDrawable(new ResourceLocation("textures/gui/container/furnace.png"), 79, 34, 24, 17);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.totf:sidhe.title");
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
        slot.draw(minecraft, 8, 11);
        arrow.draw(minecraft, 46, 12);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, Wrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(INPUT_SLOT, true, 8, 11);
        stacks.set(INPUT_SLOT, ingredients.getInputs(ItemStack.class).get(0));
        // The reward slot only exists once the player has seen this trade play out; setRecipe runs each time the
        // recipe is displayed, so the reveal shows up without reloading JEI.
        if (wrapper.isDiscovered() && !wrapper.reward.isEmpty()) {
            stacks.init(OUTPUT_SLOT, false, 90, 11);
            stacks.set(OUTPUT_SLOT, wrapper.reward);
        }
    }

    public static List<Wrapper> getRecipes() {
        List<Wrapper> wrappers = new ArrayList<>();
        for (String entry : Settings.miscSettings.sidheInteractItems) {
            String[] parts = entry.split("\\|");
            if (parts.length < 2) continue;
            ItemStack input = parseInput(parts[0]);
            if (!input.isEmpty()) {
                wrappers.add(new Wrapper(parts[0], input, parts[1]));
            }
        }
        return wrappers;
    }

    /** Mirrors the matching rules of {@link EntitySidhe#getInteractEntry}; a missing meta means 0 and '*' means any. */
    private static ItemStack parseInput(String pattern) {
        String[] itemParts = pattern.split(":", 4);
        if (itemParts.length < 2) return ItemStack.EMPTY;
        Item item = Item.REGISTRY.getObject(new ResourceLocation(itemParts[0], itemParts[1]));
        if (item == null) return ItemStack.EMPTY;
        int meta = 0;
        if (itemParts.length >= 3) {
            if (itemParts[2].equals("*")) {
                meta = OreDictionary.WILDCARD_VALUE;
            } else {
                try {
                    meta = Integer.parseInt(itemParts[2]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        ItemStack stack = new ItemStack(item, 1, meta);
        if (itemParts.length >= 4) {
            try {
                stack.setTagCompound(JsonToNBT.getTagFromJson(itemParts[3]));
            } catch (Exception ignored) {
            }
        }
        return stack;
    }

    public static class Wrapper implements IRecipeWrapper {
        private final String key;
        private final ItemStack input;
        private final boolean attack;
        private final ItemStack reward;

        Wrapper(String key, ItemStack input, String action) {
            this.key = key;
            this.input = input;
            this.attack = action.equals("attack");
            this.reward = attack ? ItemStack.EMPTY : EntitySidhe.parseRewardItem(action);
        }

        boolean isDiscovered() {
            return SidheDiscoveries.isDiscovered(key);
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInputs(ItemStack.class, Collections.singletonList(input));
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            if (!isDiscovered()) {
                drawCentered(minecraft, "?", 99, 16, 0xA0A0A0);
            } else if (attack) {
                drawCentered(minecraft, I18n.format("jei.totf:sidhe.attack"), 99, 16, 0xB04040);
            }
        }

        private void drawCentered(Minecraft minecraft, String text, int centerX, int y, int color) {
            minecraft.fontRenderer.drawString(text, centerX - minecraft.fontRenderer.getStringWidth(text) / 2, y, color);
        }

        @Override
        public List<String> getTooltipStrings(int mouseX, int mouseY) {
            if (mouseX >= 80 && mouseX <= 116 && mouseY >= 8 && mouseY <= 32) {
                if (!isDiscovered()) {
                    return Collections.singletonList(I18n.format("jei.totf:sidhe.unknown.tooltip"));
                } else if (attack) {
                    return Collections.singletonList(I18n.format("jei.totf:sidhe.attack.tooltip"));
                }
            }
            return Collections.emptyList();
        }
    }
}
