package com.windanesz.tracesofthefallen.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

public class ItemBerserkerHelmet extends ItemArmor {

	private static final ArmorMaterial BERSERKER_MATERIAL = EnumHelper.addArmorMaterial(
			"BERSERKER", "totf:berserk_armor", 20,
			new int[]{2, 5, 6, 2}, 10,
			SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F);

	public ItemBerserkerHelmet() {
		super(BERSERKER_MATERIAL, 0, EntityEquipmentSlot.HEAD);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return "totf:textures/armor/berserk_armor_layer_1.png";
	}
}
