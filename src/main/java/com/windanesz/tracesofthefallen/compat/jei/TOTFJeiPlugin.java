package com.windanesz.tracesofthefallen.compat.jei;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class TOTFJeiPlugin implements IModPlugin {
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new SpinningWheelCategory(registry.getJeiHelpers().getGuiHelper()),
                new SidheCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(SpinningWheelCategory.getRecipes(), SpinningWheelCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.spinning_wheel), SpinningWheelCategory.UID);
        registry.addRecipes(SidheCategory.getRecipes(), SidheCategory.UID);

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

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.brass_fabricator), mezz.jei.api.recipe.VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeClickArea(com.windanesz.tracesofthefallen.client.gui.GuiBrassFabricator.class, 89, 35, 24, 17, mezz.jei.api.recipe.VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(com.windanesz.tracesofthefallen.inventory.ContainerBrassFabricator.class, mezz.jei.api.recipe.VanillaRecipeCategoryUid.CRAFTING, 0, 9, 18, 36);
    }

    private void hideIfPresent(IIngredientBlacklist blacklist, Block block) {
        if (block != null) {
            blacklist.addIngredientToBlacklist(new ItemStack(block));
        }
    }
}
