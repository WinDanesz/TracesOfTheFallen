package com.windanesz.tracesofthefallen.client.particle;

import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.world.World;

public class ParticleSifterCloud extends ParticleCloud {

	public ParticleSifterCloud(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.particleScale *= 0.5F;
		this.setSize(0.1F, 0.1F);
	}
}
