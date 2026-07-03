package com.windanesz.tracesofthefallen.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityIdolVessel extends TileEntity implements ITickable {

	private static final double EFFECT_RADIUS = 4.0D;
	private static final int TICK_INTERVAL = 20;
	private static final float SATURATION_THRESHOLD = 10.0F;
	private static final float FULL_SATURATION = 20.0F;
	private static final int LUCK_REFRESH_TICKS = 40;
	private static final int LUCK_AMPLIFIER = 1; // Luck II
	private int tickCounter = 0;

	@Override
	public void update() {
		World currentWorld = world;
		if (currentWorld == null || currentWorld.isRemote) {
			return;
		}

		if (++tickCounter < TICK_INTERVAL) {
			return;
		}
		tickCounter = 0;

		AxisAlignedBB area = new AxisAlignedBB(pos).grow(EFFECT_RADIUS, 5.0D, EFFECT_RADIUS);
		List<EntityPlayer> players = currentWorld.getEntitiesWithinAABB(EntityPlayer.class, area, player -> player != null && !player.isDead);
		for (EntityPlayer player : players) {
			applyVesselEffect(player);
		}
	}

	private static void applyVesselEffect(EntityPlayer player) {
		float saturation = player.getFoodStats().getSaturationLevel();
		if (saturation > SATURATION_THRESHOLD) {
			player.addPotionEffect(new PotionEffect(MobEffects.LUCK, LUCK_REFRESH_TICKS, LUCK_AMPLIFIER, true, false));
		}

		if (saturation >= FULL_SATURATION && player.getFoodStats().getFoodLevel() > 0) {
			int newFoodLevel = player.getFoodStats().getFoodLevel() - 1;
			player.getFoodStats().setFoodLevel(newFoodLevel);
			player.getFoodStats().setFoodSaturationLevel(Math.min(player.getFoodStats().getSaturationLevel(), newFoodLevel));
		}
	}
}
