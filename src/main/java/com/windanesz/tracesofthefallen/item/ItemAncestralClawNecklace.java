package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityVoidSlash;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ItemAncestralClawNecklace extends Item {

	public ItemAncestralClawNecklace() {
		this.setMaxStackSize(1);
		this.setMaxDamage(Settings.miscSettings.ancestralClawNecklaceDurability);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		int usedTicks = this.getMaxItemUseDuration(stack) - count;
		World world = player.world;
		
		if (usedTicks == 30) {
			if (!world.isRemote) {
				EntityVoidSlash slash = new EntityVoidSlash(world, player);
				slash.setPosition(player.posX, player.posY + player.getEyeHeight() - 0.1D, player.posZ);
				world.spawnEntity(slash);

				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.8F + world.rand.nextFloat() * 0.3F);
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 0.7F);

				if (world instanceof WorldServer) {
					((WorldServer) world).spawnParticle(EnumParticleTypes.SPELL_WITCH, player.posX, player.posY + player.getEyeHeight(), player.posZ, 25, 0.4D, 0.4D, 0.4D, 0.15D);
					((WorldServer) world).spawnParticle(EnumParticleTypes.PORTAL, player.posX, player.posY + player.getEyeHeight(), player.posZ, 15, 0.3D, 0.3D, 0.3D, 0.2D);
				}

				stack.damageItem(1, player);
			}
			
			player.resetActiveHand();
		} else if (usedTicks < 30) {
			if (world.isRemote && world.rand.nextBoolean()) {
				world.spawnParticle(EnumParticleTypes.SPELL_WITCH, player.posX + (world.rand.nextDouble() - 0.5D) * 1.2D, player.posY + 1.2D + world.rand.nextDouble(), player.posZ + (world.rand.nextDouble() - 0.5D) * 1.2D, 0.0D, 0.03D, 0.0D);
			}
			if (usedTicks % 10 == 0) {
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.5F, 1.5F);
			}
		}
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemEnchantability() {
		return 10;
	}
}
