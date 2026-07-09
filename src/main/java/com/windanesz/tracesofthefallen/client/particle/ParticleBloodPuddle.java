package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBloodPuddle extends Particle {

	private final float yOffset;

	public ParticleBloodPuddle(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.setPosition(x, y, z);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.particleMaxAge = 18;
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.particleAlpha = 1.0F;
		this.particleScale = 3.5F + this.rand.nextFloat() * 1.5F;
		this.canCollide = false;
		this.yOffset = 0.015F + this.rand.nextFloat() * 0.03F;

		TextureAtlasSprite sprite = ClientProxy.getPuddleBloodSprite();
		if (sprite != null) {
			this.setParticleTexture(sprite);
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		float u0 = this.particleTexture == null ? (float) this.particleTextureIndexX / 16.0F : this.particleTexture.getMinU();
		float u1 = this.particleTexture == null ? u0 + 0.0624375F : this.particleTexture.getMaxU();
		float v0 = this.particleTexture == null ? (float) this.particleTextureIndexY / 16.0F : this.particleTexture.getMinV();
		float v1 = this.particleTexture == null ? v0 + 0.0624375F : this.particleTexture.getMaxV();

		double interpX = this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX;
		double interpY = this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY;
		double interpZ = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ;

		float size = this.particleScale * 0.1F;

		int brightness = this.getBrightnessForRender(partialTicks);
		int lightX = brightness >> 16 & 65535;
		int lightY = brightness & 65535;

		buffer.pos(interpX - size, interpY + (double) this.yOffset, interpZ - size).tex(u0, v0)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
		buffer.pos(interpX - size, interpY + (double) this.yOffset, interpZ + size).tex(u0, v1)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
		buffer.pos(interpX + size, interpY + (double) this.yOffset, interpZ + size).tex(u1, v1)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
		buffer.pos(interpX + size, interpY + (double) this.yOffset, interpZ - size).tex(u1, v0)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		if (this.isExpired) {
			return;
		}

		float progress = (float) this.particleAge / (float) this.particleMaxAge;
		if (progress > 0.7F) {
			this.particleAlpha = 1.0F - ((progress - 0.7F) / 0.3F);
		} else {
			this.particleAlpha = 1.0F;
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
}
