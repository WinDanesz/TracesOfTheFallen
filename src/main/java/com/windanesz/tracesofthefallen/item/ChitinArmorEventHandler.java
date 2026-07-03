package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class ChitinArmorEventHandler {

	private ChitinArmorEventHandler() {
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)) {
			return;
		}

		EntityPlayer player = (EntityPlayer) event.getEntityLiving();
		if (!ChitinArmor.isWearingFullSet(player)) {
			return;
		}

		EntityLivingBase attacker = event.getSource().getTrueSource() instanceof EntityLivingBase
				? (EntityLivingBase) event.getSource().getTrueSource() : null;

		if (attacker == null) {
			return;
		}

		ItemStack weapon = attacker.getHeldItemMainhand();
		int baneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, weapon);
		if (baneLevel <= 0) {
			return;
		}

		event.setAmount(event.getAmount() + (2.5F * baneLevel));
	}
}
