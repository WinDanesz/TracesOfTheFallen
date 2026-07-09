package com.windanesz.tracesofthefallen.compat.jei;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class TOTFJeiPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        Block[] hiddenBlocks = {
                ModBlocks.glass_float_clear_blue_landed,
                ModBlocks.glass_float_wine_green_landed,
                ModBlocks.glass_float_longing_blue_landed,
                ModBlocks.glass_float_blood_red_landed,
                ModBlocks.rope_mossy,
                ModBlocks.rope_mossy_hook
        };

        for (Block block : hiddenBlocks) {
            hideIfPresent(blacklist, block);
        }
    }

    private void hideIfPresent(IIngredientBlacklist blacklist, Block block) {
        if (block != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(block));
        }
    }
}
