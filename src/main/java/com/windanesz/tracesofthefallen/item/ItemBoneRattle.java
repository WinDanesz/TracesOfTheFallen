package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class ItemBoneRattle extends Item {

	public ItemBoneRattle() {
		this.setMaxStackSize(1);
		this.setMaxDamage(Settings.miscSettings.boneRattleMaxUses);
		this.setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return Settings.miscSettings.boneRattleMaxUses;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.PLAYERS, 1.2F, 0.8F + worldIn.rand.nextFloat() * 0.2F);
		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.PLAYERS, 1.0F, 1.2F);

		if (!worldIn.isRemote) {
			if (worldIn instanceof WorldServer) {
				((WorldServer) worldIn).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ, 20, 1.5D, 0.5D, 1.5D, 0.02D);
			}

			playerIn.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 0));

			List<EntityPlayer> players = worldIn.getEntitiesWithinAABB(EntityPlayer.class, playerIn.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D));
			for (EntityPlayer p : players) {
				if (p.isEntityAlive() && playerIn.getDistanceSq(p) <= 144.0D) {
					p.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 0));
				}
			}

			List<EntityTameable> pets = worldIn.getEntitiesWithinAABB(EntityTameable.class, playerIn.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D));
			for (EntityTameable pet : pets) {
				if (pet.isEntityAlive() && pet.isTamed() && playerIn.getDistanceSq(pet) <= 144.0D) {
					pet.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 0));
				}
			}

			itemstack.damageItem(1, playerIn);
			playerIn.getCooldownTracker().setCooldown(this, 100);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}
}
