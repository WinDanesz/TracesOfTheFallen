package com.windanesz.tracesofthefallen.compat.jei;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class TOTFJeiPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        if (ModBlocks.glass_float_clear_blue_landed != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.glass_float_clear_blue_landed));
        }
        if (ModBlocks.glass_float_wine_green_landed != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.glass_float_wine_green_landed));
        }
        if (ModBlocks.glass_float_longing_blue_landed != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.glass_float_longing_blue_landed));
        }
        if (ModBlocks.glass_float_blood_red_landed != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.glass_float_blood_red_landed));
        }
        if (ModBlocks.rope_mossy != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.rope_mossy));
        }
        if (ModBlocks.rope_mossy_hook != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.rope_mossy_hook));
        }
    }
}
