package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMysteriousFur extends Item {
    public ItemMysteriousFur() {
        this.maxStackSize = 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);

        BlockPos pos = playerIn.getPosition();
        Biome biome = worldIn.getBiome(pos);
        
        // Check if biome is cold/freezing (temperature <= 0.15 is considered cold/snowy)
        if (biome.getTemperature(pos) <= 0.15f || biome.isSnowyBiome()) {
            if (!worldIn.isRemote) {
                playerIn.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1200, 0));
                playerIn.getFoodStats().addStats(3, 0.3f);
                itemStack.shrink(1);
            } else {
                // Client-side: play totem animation
                TracesOfTheFallen.proxy.renderFur();
            }
            
            return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
        }
        
        return new ActionResult<>(EnumActionResult.PASS, itemStack);
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("item.totf:mysterious_fur.desc"));
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.DARK_GRAY + I18n.format("item.totf:mysterious_fur.desc2"));
	}
}