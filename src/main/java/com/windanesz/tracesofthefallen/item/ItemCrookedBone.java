package com.windanesz.tracesofthefallen.item;

import com.google.common.collect.Multimap;
import com.windanesz.tracesofthefallen.Settings;
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

public class ItemCrookedBone extends ItemSword {

	public ItemCrookedBone() {
		super(ToolMaterial.WOOD);
		this.setMaxDamage(70);
		this.setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", Settings.miscSettings.crookedBoneDamage - 1.0D, 0));
		}
		return multimap;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.totf:crooked_bone.desc"));
	}
}
