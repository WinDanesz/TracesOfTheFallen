package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBloodDrop extends Particle {

	public ParticleBloodDrop(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(worldIn, x, y, z, motionX, motionY, motionZ);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.particleRed = 0.65F + this.rand.nextFloat() * 0.15F;
		this.particleGreen = 0.02F;
		this.particleBlue = 0.02F;
		this.particleAlpha = 1.0F;
		this.particleScale = 1.2F + this.rand.nextFloat() * 0.1F;
		this.setParticleTextureIndex(113);
		this.particleMaxAge = 120;
		this.particleGravity = 0.06F;
		this.canCollide = true;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		if (!this.isExpired) {
			this.motionY -= 0.04D * (double) this.particleGravity;
			this.move(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.98D;
			this.motionY *= 0.98D;
			this.motionZ *= 0.98D;

			if (this.onGround) {
				this.setExpired();
				if (this.world.isRemote) {
					TracesOfTheFallen.proxy.spawnBloodPuddleParticle(this.world, this.posX, this.posY, this.posZ);
				}
			}
		}
	}
}
