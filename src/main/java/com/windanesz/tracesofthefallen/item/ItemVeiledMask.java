package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemVeiledMask extends ItemArmor {

    // Create a custom armor material for the Veiled Mask
    private static final ArmorMaterial VEILED_MASK_MATERIAL = EnumHelper.addArmorMaterial(
            "VEILED_MASK",
            "totf:veiled_mask",
            Settings.miscSettings.veiledMaskDurability,
            new int[]{1, 0, 0, 0}, // armor values (boots, legs, chest, helmet)
            9,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            0.0F
    );

    public ItemVeiledMask() {
        super(VEILED_MASK_MATERIAL, 0, EntityEquipmentSlot.HEAD);
        this.setMaxStackSize(1);
        this.setMaxDamage(Settings.miscSettings.veiledMaskDurability);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        // Only run on server side
        if (world.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }

        // Check if it's time to apply effects (every X ticks based on config)
        if (player.ticksExisted % Settings.miscSettings.veiledMaskHauntingTickRate == 0) {
            // Increase haunting progress
            HauntingCapability cap = HauntingCapability.get(player);
            if (cap != null) {
                cap.addHauntingProgress(Settings.miscSettings.veiledMaskHauntingAmount);
            }

            // Consume durability (unless in creative mode)
            if (!player.capabilities.isCreativeMode) {
                itemStack.damageItem(1, player);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("item.totf:veiled_mask.desc"));
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.DARK_GRAY + I18n.format("item.totf:veiled_mask.desc2"));
	}
}