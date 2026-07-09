package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCyanCinder extends Particle {

	public ParticleCyanCinder(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(worldIn, x, y, z, motionX, motionY, motionZ);
		this.motionX = motionX + (this.rand.nextDouble() - 0.5D) * 0.03D;
		this.motionY = motionY + (this.rand.nextDouble() - 0.5D) * 0.03D + 0.015D;
		this.motionZ = motionZ + (this.rand.nextDouble() - 0.5D) * 0.03D;
		this.particleRed = 0.05F + this.rand.nextFloat() * 0.15F;
		this.particleGreen = 0.88F + this.rand.nextFloat() * 0.12F;
		this.particleBlue = 0.95F + this.rand.nextFloat() * 0.05F;
		this.particleAlpha = 0.9F;
		this.particleScale = 0.7F + this.rand.nextFloat() * 0.6F;
		this.particleMaxAge = 25 + this.rand.nextInt(15);
		this.particleGravity = -0.01F;
		this.canCollide = false;

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
		}

		this.motionY -= 0.02D * (double) this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.96D;
		this.motionY *= 0.96D;
		this.motionZ *= 0.96D;

		if (this.particleAge > this.particleMaxAge / 2) {
			this.particleAlpha = 0.9F * (1.0F - ((float)(this.particleAge - this.particleMaxAge / 2) / (float)(this.particleMaxAge / 2)));
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
