package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleLifestealStream extends Particle {
	private final int targetEntityId;

	public ParticleLifestealStream(World worldIn, double x, double y, double z, int targetEntityId) {
		super(worldIn, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX = (this.rand.nextDouble() - 0.5D) * 0.15D;
		this.motionY = (this.rand.nextDouble() - 0.5D) * 0.15D + 0.1D;
		this.motionZ = (this.rand.nextDouble() - 0.5D) * 0.15D;
		this.particleRed = 0.95F;
		this.particleGreen = 0.05F + this.rand.nextFloat() * 0.15F;
		this.particleBlue = 0.15F + this.rand.nextFloat() * 0.1F;
		this.particleAlpha = 0.9F;
		this.particleScale = 0.7F + this.rand.nextFloat() * 0.5F;
		this.particleMaxAge = 40 + this.rand.nextInt(25);
		this.canCollide = false;
		this.targetEntityId = targetEntityId;

		TextureAtlasSprite sprite = ClientProxy.getFrostSprite();
		if (sprite != null) {
			this.setParticleTexture(sprite);
		}
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
			return;
		}

		Entity target = this.world.getEntityByID(this.targetEntityId);
		if (target != null && target.isEntityAlive()) {
			double targetX = target.posX;
			double targetY = target.posY + (double) (target.height * 0.55F);
			double targetZ = target.posZ;

			double dx = targetX - this.posX;
			double dy = targetY - this.posY;
			double dz = targetZ - this.posZ;
			double distSq = dx * dx + dy * dy + dz * dz;

			if (distSq < 0.6D) {
				this.setExpired();
				return;
			}

			double dist = MathHelper.sqrt(distSq);
			double speed = 0.3D + ((double) this.particleAge * 0.03D);
			this.motionX = (dx / dist) * speed + (this.rand.nextDouble() - 0.5D) * 0.05D;
			this.motionY = (dy / dist) * speed + (this.rand.nextDouble() - 0.5D) * 0.05D;
			this.motionZ = (dz / dist) * speed + (this.rand.nextDouble() - 0.5D) * 0.05D;
		} else {
			this.motionY += 0.005D;
			this.motionX *= 0.96D;
			this.motionY *= 0.96D;
			this.motionZ *= 0.96D;
		}

		this.move(this.motionX, this.motionY, this.motionZ);

		if (this.particleAge > this.particleMaxAge - 10) {
			this.particleAlpha = 0.9F * (1.0F - ((float)(this.particleAge - (this.particleMaxAge - 10)) / 10.0F));
		}
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		return 15728880;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
}
