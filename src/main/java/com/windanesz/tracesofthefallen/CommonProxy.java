package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.packet.PacketPlayerSync;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
	}

	public void registerColorHandlers() {
	}

	public void handlePlayerSyncPacket(PacketPlayerSync.Message message) {
	}

	public void renderFur() {
	}

	public void renderItemActivation(ItemStack stack) {
	}

	public void spawnSifterCloudParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
	}

	public void spawnIncenseSmokeParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
	}

	public void spawnIncenseFloorMistParticle(World world, double x, double y, double z, double motionX, double motionY,
			double motionZ, int color) {
	}

	public void spawnBloodDropParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
	}

	public void spawnBloodPuddleParticle(World world, double x, double y, double z) {
	}

	public void spawnChillParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
	}

	public void spawnCyanCinderParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
	}

	public void spawnLifestealParticle(World world, double x, double y, double z, int targetEntityId) {
	}

	public void spawnCrumbsParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
	}
}

