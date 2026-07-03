package com.windanesz.tracesofthefallen.item;

import com.google.common.collect.Multimap;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBrassClub extends ItemSword {

	public ItemBrassClub() {
		super(ToolMaterial.IRON);
		this.setMaxDamage(180);
		this.setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return Settings.miscSettings.brassClubDurability;
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", Settings.miscSettings.brassClubDamage - 1.0D, 0));
		}
		return multimap;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.totf:brass_club.desc"));
	}
}
