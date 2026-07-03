package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IncenseEffects {

	public static final int EFFECT_REFRESH_INTERVAL = 20;
	public static final int EFFECT_REFRESH_DURATION = 40;

	private IncenseEffects() {}

	public static AxisAlignedBB getAuraArea(BlockPos pos) {
		int radius = Math.max(0, Settings.miscSettings.incenseSerenityRadius);
		return getAuraArea(pos, radius);
	}

	public static AxisAlignedBB getAuraArea(BlockPos pos, int radius) {
		radius = Math.max(0, radius);
		return new AxisAlignedBB(pos.add(-radius, 0, -radius), pos.add(radius + 1, 3, radius + 1));
	}

	public static void applySerenityAura(World world, AxisAlignedBB area) {
		applyAuraEffects(world, area, Collections.emptyList(), 0);
	}

	public static void applyAuraEffects(World world, AxisAlignedBB area, List<PotionEffect> effects) {
		applyAuraEffects(world, area, effects, 0);
	}

	public static void applyAuraEffects(World world, AxisAlignedBB area, List<PotionEffect> effects, int serenityAmplifier) {
		if (world == null || world.isRemote) {
			return;
		}

		List<EntityLivingBase> nearbyEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, area,
				entity -> entity != null && entity.isEntityAlive());
		boolean hasCustomEffects = effects != null && !effects.isEmpty();

		for (EntityLivingBase entity : nearbyEntities) {
			if (hasCustomEffects) {
				for (PotionEffect effect : effects) {
					if (effect == null || effect.getPotion() == null) {
						continue;
					}
					entity.addPotionEffect(new PotionEffect(effect.getPotion(), EFFECT_REFRESH_DURATION, effect.getAmplifier(),
							effect.getIsAmbient(), effect.doesShowParticles()));
				}
			} else {
				entity.addPotionEffect(new PotionEffect(ModPotions.serenity, EFFECT_REFRESH_DURATION, Math.max(0, serenityAmplifier), false, true));
			}
		}
	}

	public static int getRandomBurnDuration(World world) {
		int minDuration = Math.max(20, Settings.miscSettings.incenseBurnDurationMin);
		int maxDuration = Math.max(minDuration, Settings.miscSettings.incenseBurnDurationMax);
		return minDuration + world.rand.nextInt(maxDuration - minDuration + 1);
	}

	public static void spawnSmokeParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
		TracesOfTheFallen.proxy.spawnIncenseSmokeParticle(world, x, y, z, motionX, motionY, motionZ, color);
	}

	public static boolean isIncenseFuel(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		Item item = stack.getItem();
		return item == ModItems.incense || item == Item.getItemFromBlock(ModBlocks.incense_stick);
	}

	public static int getFuelBurnDuration(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}

		Item item = stack.getItem();
		if (item == ModItems.incense) {
			return Math.max(20, Settings.miscSettings.incenseItemBurnDuration);
		}
		if (item == Item.getItemFromBlock(ModBlocks.incense_stick)) {
			return Math.max(20, Settings.miscSettings.incenseStickBurnDuration);
		}
		return 0;
	}

	public static boolean isPotionApplicator(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		Item item = stack.getItem();
		return item == Items.POTIONITEM || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
	}

	public static List<PotionEffect> getWhitelistedPotionEffects(ItemStack stack) {
		if (!isPotionApplicator(stack)) {
			return Collections.emptyList();
		}

		List<PotionEffect> effects = new ArrayList<>();
		for (PotionEffect effect : PotionUtils.getEffectsFromStack(stack)) {
			if (effect == null || effect.getPotion() == null || effect.getPotion().isInstant()) {
				continue;
			}
			if (!isPotionWhitelisted(effect.getPotion())) {
				continue;
			}

			effects.add(new PotionEffect(effect.getPotion(), effect.getDuration(), effect.getAmplifier(),
					effect.getIsAmbient(), effect.doesShowParticles()));
		}

		return effects;
	}

	public static boolean isPotionWhitelisted(Potion potion) {
		if (potion == null || potion.getRegistryName() == null) {
			return false;
		}

		String potionName = potion.getRegistryName().toString();
		for (String whitelistEntry : Settings.miscSettings.incensePotionEffectWhitelist) {
			if (whitelistEntry != null && potionName.equalsIgnoreCase(whitelistEntry.trim())) {
				return true;
			}
		}

		return false;
	}

	public static boolean isFireStarter(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		return stack.getItem() == Items.FLINT_AND_STEEL
				|| stack.getItem() == Items.FIRE_CHARGE
				|| stack.getItem().getToolClasses(stack).contains("firestarter");
	}
}
