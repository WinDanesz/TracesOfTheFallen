package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityDreamcatcher extends TileEntity implements ITickable {

	private static final double EFFECT_RADIUS = 8.0D;
	private static final int TICK_INTERVAL = 20;
	private static final long COOLDOWN_TICKS = 600L; // 30 seconds
	private static final String NBT_LAST_DREAMCATCHER_TICK = "totf:last_dreamcatcher_tick";

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

		AxisAlignedBB area = new AxisAlignedBB(pos).grow(EFFECT_RADIUS, EFFECT_RADIUS, EFFECT_RADIUS);
		List<EntityPlayer> players = currentWorld.getEntitiesWithinAABB(EntityPlayer.class, area, player -> player != null && !player.isDead);
		for (EntityPlayer player : players) {
			applyDreamcatcherEffect(currentWorld, player);
		}
	}

	private static void applyDreamcatcherEffect(World world, EntityPlayer player) {
		long currentTime = world.getTotalWorldTime();
		NBTTagCompound data = player.getEntityData();
		long lastTick = data.getLong(NBT_LAST_DREAMCATCHER_TICK);
		if (lastTick > 0 && currentTime - lastTick < COOLDOWN_TICKS) {
			return;
		}

		HauntingCapability cap = HauntingCapability.get(player);
		if (cap != null && cap.getHauntingProgress() > 20) {
			int current = cap.getHauntingProgress();
			boolean isNight = !world.isDaytime();
			int amount = isNight ? 3 : 1;
			int newProg = Math.max(20, current - amount);
			if (newProg < current) {
				cap.setHauntingProgress(newProg);
				data.setLong(NBT_LAST_DREAMCATCHER_TICK, currentTime);
			}
		}
	}
}
