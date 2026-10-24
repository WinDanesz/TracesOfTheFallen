package com.windanesz.tracesofthefallen.compat.jei;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.item.SidheBarter;
import com.windanesz.tracesofthefallen.misc.SidheDiscoveries;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.living.EntitySidhe;
import com.windanesz.tracesofthefallen.init.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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

    private static final int WIDTH = 130;
    private static final int HEIGHT = 40;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/gui_jei_sidhe.png");
    private static final ResourceLocation BACKGROUND_ANGRY = new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/gui_jei_sidhe_angry.png");
    /** Horizontal span to the right of the arrow where the result (slot or text) is shown. */
    private static final int RESULT_LEFT = 74;
    private static final int RESULT_RIGHT = WIDTH - 2;

    private final IDrawable background;
    private final IDrawable icon;

    public SidheCategory(IGuiHelper guiHelper) {
        // Left blank: every recipe draws its own background (see Wrapper#drawInfo), as angering trades use a different one
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableIngredient(new ItemStack(ModItems.furred_trout));
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
    public void setRecipe(IRecipeLayout layout, Wrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(INPUT_SLOT, true, 8, 11);
        stacks.set(INPUT_SLOT, ingredients.getInputs(ItemStack.class).get(0));
        // The reward slot only exists once the player has seen this trade play out; setRecipe runs each time the
        // recipe is displayed, so the reveal shows up without reloading JEI.
        // With several possible rewards JEI cycles through them
        List<ItemStack> rewards = wrapper.getRewards();
        if (wrapper.isDiscovered() && !rewards.isEmpty()) {
            stacks.init(OUTPUT_SLOT, false, 93, 11);
            stacks.set(OUTPUT_SLOT, rewards);
        }
    }

    public static List<Wrapper> getRecipes() {
        List<Wrapper> wrappers = new ArrayList<>();
        for (String entry : Settings.miscSettings.sidheInteractItems) {
            String[] parts = entry.split("\\|");
            if (parts.length < 2) continue;
            // A wildcard entry becomes one recipe per variant, each discovered on its own.
            for (ItemStack input : SidheBarter.expandInputs(parts[0])) {
                wrappers.add(new Wrapper(EntitySidhe.getDiscoveryKey(input), input, parts[1]));
            }
        }
        return wrappers;
    }

    public static class Wrapper implements IRecipeWrapper {
        private final String key;
        private final ItemStack input;
        private final String configuredAction;

        Wrapper(String key, ItemStack input, String action) {
            this.key = key;
            this.input = input;
            this.configuredAction = action;
        }

        /** What the server recorded for this player, or null if undiscovered or unknown. Outcomes can be randomized per player. */
        private SidheDiscoveries.Info getInfo() {
            return Settings.miscSettings.randomizeSidheBarter ? SidheDiscoveries.getInfo(key) : null;
        }

        private boolean isAttack() {
            SidheDiscoveries.Info info = getInfo();
            return info != null ? info.attack : configuredAction.equals(SidheBarter.ATTACK);
        }

        /**
         * With randomization on, the rewards the player has received so far, followed by a question mark for each one they
         * haven't. Otherwise all the rewards listed in the config.
         */
        List<ItemStack> getRewards() {
            SidheDiscoveries.Info info = getInfo();
            if (info == null) return Settings.miscSettings.randomizeSidheBarter ? Collections.emptyList() : EntitySidhe.parseRewardItems(configuredAction);
            List<ItemStack> rewards = new ArrayList<>();
            for (String seen : info.seen) {
                ItemStack reward = EntitySidhe.parseRewardItem(seen);
                if (!reward.isEmpty()) rewards.add(reward);
            }
            for (int i = rewards.size(); i < info.total; i++) {
                rewards.add(new ItemStack(ModItems.sidhe_unknown_reward));
            }
            return rewards;
        }

        boolean isDiscovered() {
            if (!SidheDiscoveries.isDiscovered(key)) return false;
            // Discovered before outcomes were recorded: with randomization on the config doesn't say what this player
            // gets, so keep it a secret until they try the offering again
            return !Settings.miscSettings.randomizeSidheBarter || getInfo() != null;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInputs(ItemStack.class, Collections.singletonList(input));
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            minecraft.getTextureManager().bindTexture(isDiscovered() && isAttack() ? BACKGROUND_ANGRY : BACKGROUND);
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, WIDTH, HEIGHT, 256.0F, 256.0F);

            if (!isDiscovered()) {
                drawCentered(minecraft, "?", 16, 0xA0A0A0, false);
            } else if (isAttack()) {
                drawCentered(minecraft, I18n.format("jei.totf:sidhe.attack"), 16, 0xB04040, true);
            }
        }

        /** Draws the text centred in the result area, shrinking it if a translation is too long to fit. */
        private void drawCentered(Minecraft minecraft, String text, int y, int color, boolean whiteBorder) {
            int textWidth = minecraft.fontRenderer.getStringWidth(text);
            int available = RESULT_RIGHT - RESULT_LEFT;
            float scale = textWidth > available ? (float) available / textWidth : 1.0F;
            float centerX = (RESULT_LEFT + RESULT_RIGHT) / 2.0F;
            // keep the text vertically centred on the same line when scaled down
            float drawY = y + (1.0F - scale) * minecraft.fontRenderer.FONT_HEIGHT / 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(centerX - textWidth * scale / 2.0F, drawY, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            if (whiteBorder) {
                // 1px white outline for contrast against the grey recipe background
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) minecraft.fontRenderer.drawString(text, dx, dy, 0xFFFFFF);
                    }
                }
            }
            minecraft.fontRenderer.drawString(text, 0, 0, color);
            GlStateManager.popMatrix();
        }

        @Override
        public List<String> getTooltipStrings(int mouseX, int mouseY) {
            if (mouseX >= RESULT_LEFT && mouseX <= WIDTH && mouseY >= 8 && mouseY <= 32) {
                if (!isDiscovered()) {
                    return Collections.singletonList(I18n.format("jei.totf:sidhe.unknown.tooltip"));
                } else if (isAttack()) {
                    return Collections.singletonList(I18n.format("jei.totf:sidhe.attack.tooltip"));
                }
            }
            return Collections.emptyList();
        }
    }
}
