package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import com.windanesz.tracesofthefallen.client.model.ModelChitinArmor;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

public class ChitinArmor extends ItemArmor {
	private static final ModelBiped CHITIN_MODEL = new ModelChitinArmor();

	private static final ArmorMaterial CHITIN_MATERIAL = EnumHelper.addArmorMaterial(
			"CHITIN",
			"totf:chitin",
			Settings.miscSettings.chitinArmorDurability,
			new int[]{
					Settings.miscSettings.chitinArmorProtectionBoots,
					Settings.miscSettings.chitinArmorProtectionLeggings,
					Settings.miscSettings.chitinArmorProtectionChestplate,
					Settings.miscSettings.chitinArmorProtectionHelmet
			},
			Settings.miscSettings.chitinArmorEnchantability,
			SoundEvents.ITEM_ARMOR_EQUIP_IRON,
			(float) Settings.miscSettings.chitinArmorToughness
	);

	public ChitinArmor(EntityEquipmentSlot slot) {
		super(CHITIN_MATERIAL, 0, slot);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return slot == EntityEquipmentSlot.LEGS
				? "totf:textures/armor/chitin_layer_2.png"
				: "totf:textures/armor/chitin_layer_1.png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped original) {
		CHITIN_MODEL.setModelAttributes(original);
		CHITIN_MODEL.setLivingAnimations(entityLiving, 0.0F, 0.0F, 0.0F);

		CHITIN_MODEL.setVisible(false);
		CHITIN_MODEL.bipedHead.showModel = armorSlot == EntityEquipmentSlot.HEAD;
		CHITIN_MODEL.bipedHeadwear.showModel = false;
		CHITIN_MODEL.bipedBody.showModel = armorSlot == EntityEquipmentSlot.CHEST || armorSlot == EntityEquipmentSlot.LEGS;
		CHITIN_MODEL.bipedRightArm.showModel = armorSlot == EntityEquipmentSlot.CHEST;
		CHITIN_MODEL.bipedLeftArm.showModel = armorSlot == EntityEquipmentSlot.CHEST;
		CHITIN_MODEL.bipedRightLeg.showModel = armorSlot == EntityEquipmentSlot.LEGS || armorSlot == EntityEquipmentSlot.FEET;
		CHITIN_MODEL.bipedLeftLeg.showModel = armorSlot == EntityEquipmentSlot.LEGS || armorSlot == EntityEquipmentSlot.FEET;
		CHITIN_MODEL.isSneak = entityLiving.isSneaking();
		CHITIN_MODEL.isRiding = entityLiving.isRiding();
		CHITIN_MODEL.isChild = entityLiving.isChild();

		return CHITIN_MODEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (isClientPlayerWearingFullSet() && Settings.miscSettings.chitinArmorMinecrawlerStealthRadius > 0) {
			ClientProxy.addMultiLineDescription(tooltip,
					TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
							+ I18n.format("item.totf:chitin_set_hidden.desc", Settings.miscSettings.chitinArmorMinecrawlerStealthRadius));
		}
	}

	@SideOnly(Side.CLIENT)
	private static boolean isClientPlayerWearingFullSet() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		return player != null && isWearingFullSet(player);
	}

	public static boolean isWearingFullSet(EntityPlayer player) {
		return isChitinPiece(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), ModItems.chitin_helmet)
				&& isChitinPiece(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), ModItems.chitin_chestplate)
				&& isChitinPiece(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS), ModItems.chitin_leggings)
				&& isChitinPiece(player.getItemStackFromSlot(EntityEquipmentSlot.FEET), ModItems.chitin_boots);
	}

	private static boolean isChitinPiece(ItemStack stack, net.minecraft.item.Item expected) {
		return !stack.isEmpty() && stack.getItem() == expected;
	}
}
