package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCrumbs extends Particle {

	public ParticleCrumbs(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(worldIn, x, y, z, motionX, motionY, motionZ);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		float shade = 0.85F + this.rand.nextFloat() * 0.15F;
		this.particleRed = shade;
		this.particleGreen = shade;
		this.particleBlue = shade;
		this.particleAlpha = 1.0F;
		this.particleScale = 0.6F + this.rand.nextFloat() * 0.5F;
		this.particleMaxAge = 25 + this.rand.nextInt(20);
		this.particleGravity = 0.06F;
		this.canCollide = true;

		TextureAtlasSprite sprite = ClientProxy.getCrumbsSprite();
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

		if (!this.isExpired) {
			this.motionY -= 0.04D * (double) this.particleGravity;
			this.move(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.98D;
			this.motionY *= 0.98D;
			this.motionZ *= 0.98D;

			if (this.onGround) {
				this.motionX *= 0.7D;
				this.motionZ *= 0.7D;
			}
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
}
