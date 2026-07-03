package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleIncenseSmoke extends Particle {

	private final double curveX;
	private final double curveZ;
	private final double waveX;
	private final double waveZ;
	private final double wavePhase;
	private final double waveFrequency;
	private final float lineLength;
	private final float lineWidth;

	public ParticleIncenseSmoke(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
		super(world, x, y, z);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.particleMaxAge = 42 + this.rand.nextInt(14);
		float baseRed = ((color >> 16) & 255) / 255.0F;
		float baseGreen = ((color >> 8) & 255) / 255.0F;
		float baseBlue = (color & 255) / 255.0F;
		float tintVariance = 0.03F;
		this.particleRed = clamp01(baseRed + (this.rand.nextFloat() - 0.5F) * tintVariance);
		this.particleGreen = clamp01(baseGreen + (this.rand.nextFloat() - 0.5F) * tintVariance);
		this.particleBlue = clamp01(baseBlue + (this.rand.nextFloat() - 0.5F) * tintVariance);
		this.particleAlpha = 0.78F;
		this.setSize(0.002F, 0.002F);
		this.canCollide = false;
		double curveAngle = this.rand.nextDouble() * Math.PI * 2.0D;
		double curveStrength = 0.00006D + this.rand.nextDouble() * 0.00003D;
		this.curveX = Math.cos(curveAngle) * curveStrength;
		this.curveZ = Math.sin(curveAngle) * curveStrength;
		double waveStrength = 0.00008D + this.rand.nextDouble() * 0.00004D;
		this.waveX = -Math.sin(curveAngle) * waveStrength;
		this.waveZ = Math.cos(curveAngle) * waveStrength;
		this.wavePhase = this.rand.nextDouble() * Math.PI * 2.0D;
		this.waveFrequency = 1.0D + this.rand.nextDouble() * 0.2D;
		this.lineLength = 0.57F + this.rand.nextFloat() * 0.144F;
		this.lineWidth = 0.0035F + this.rand.nextFloat() * 0.001F;
		TextureAtlasSprite sprite = ClientProxy.getIncenseLineSprite();
		if (sprite != null) {
			this.setParticleTexture(sprite);
		}
	}

	private static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
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

		Vec3d direction = new Vec3d(this.motionX, Math.max(this.motionY, 0.01D), this.motionZ);
		if (direction.length() < 1.0E-4D) {
			direction = new Vec3d(this.curveX, 0.02D, this.curveZ);
		}
		direction = direction.normalize();

		Vec3d viewDir = cameraViewDir == null ? new Vec3d(0.0D, 0.0D, 1.0D) : cameraViewDir;
		Vec3d width = direction.crossProduct(viewDir);
		if (width.length() < 1.0E-4D) {
			width = new Vec3d(-rotationX, -rotationZ, -rotationYZ);
		}
		width = width.normalize().scale(this.lineWidth);
		Vec3d length = direction.scale(this.lineLength);

		Vec3d center = new Vec3d(interpX, interpY, interpZ);
		Vec3d head = center.add(length);
		Vec3d tail = center.subtract(length);

		int brightness = this.getBrightnessForRender(partialTicks);
		int lightX = brightness >> 16 & 65535;
		int lightY = brightness & 65535;

		buffer.pos(head.x + width.x, head.y + width.y, head.z + width.z).tex(u1, v1)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
		buffer.pos(head.x - width.x, head.y - width.y, head.z - width.z).tex(u1, v0)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
		buffer.pos(tail.x - width.x, tail.y - width.y, tail.z - width.z).tex(u0, v0)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY).endVertex();
		buffer.pos(tail.x + width.x, tail.y + width.y, tail.z + width.z).tex(u0, v1)
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

		double ageRatio = (double) this.particleAge / (double) this.particleMaxAge;
		double curveFactor = 0.35D + Math.sin(ageRatio * Math.PI) * 0.65D;
		this.motionX += this.curveX * curveFactor;
		this.motionZ += this.curveZ * curveFactor;
		double waveFactor = Math.sin((ageRatio * this.waveFrequency * Math.PI * 2.0D) + this.wavePhase);
		this.motionX += this.waveX * waveFactor;
		this.motionZ += this.waveZ * waveFactor;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.996D;
		this.motionZ *= 0.996D;
		this.motionY = Math.min((this.motionY + 0.00008D) * 0.997D, 0.008D);

		float fadeRatio = (float) ageRatio;
		this.particleAlpha = 0.78F * (1.0F - fadeRatio * 0.75F);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
}
