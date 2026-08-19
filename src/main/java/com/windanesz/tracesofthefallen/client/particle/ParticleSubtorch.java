package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSubtorch extends Particle {

	public ParticleSubtorch(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(worldIn, x, y, z, motionX, motionY, motionZ);
		this.motionX = this.motionX * 0.01D + motionX;
		this.motionY = this.motionY * 0.01D + motionY;
		this.motionZ = this.motionZ * 0.01D + motionZ;
		x += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
		y += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
		z += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.particleAlpha = 0.99F;
		this.particleScale = (0.5F + this.rand.nextFloat() * 0.5F) * 4.0F; // Increased size to match typical flame particles
		this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
		this.canCollide = false;

		TextureAtlasSprite sprite = ClientProxy.getSubtorchSprite();
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

		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.96D;
		this.motionY *= 0.96D;
		this.motionZ *= 0.96D;
		
		if (this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
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
