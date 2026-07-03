package com.windanesz.tracesofthefallen.item;

import com.google.common.collect.Multimap;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityFetidDagger;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFetidDagger extends ItemSword {

	public ItemFetidDagger() {
		super(ToolMaterial.IRON);
		this.setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", Settings.miscSettings.fetidDaggerMeleeDamage - 1.0D, 0));
		}
		return multimap;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		target.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 0));
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (!worldIn.isRemote) {
			EntityFetidDagger entityDagger = new EntityFetidDagger(worldIn, playerIn, itemstack.copy());
			entityDagger.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
			worldIn.spawnEntity(entityDagger);
		}

		if (!playerIn.capabilities.isCreativeMode) {
			itemstack.shrink(1);
		}

		playerIn.addStat(StatList.getObjectUseStats(this));
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.totf:fetid_dagger.desc"));
	}
}
