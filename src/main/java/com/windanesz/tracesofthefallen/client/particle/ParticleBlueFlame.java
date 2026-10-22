package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** A small, fullbright blue flame wisp - used as the dash/retreat trail of spirits. */
@SideOnly(Side.CLIENT)
public class ParticleBlueFlame extends Particle {

	private final float baseScale;
	private boolean usesSprite;

	public ParticleBlueFlame(World worldIn, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(worldIn, x, y, z, motionX, motionY, motionZ);
		this.motionX = motionX + (this.rand.nextDouble() - 0.5D) * 0.01D;
		this.motionY = motionY + 0.01D + this.rand.nextDouble() * 0.015D;
		this.motionZ = motionZ + (this.rand.nextDouble() - 0.5D) * 0.01D;
		this.particleRed = 0.2F + this.rand.nextFloat() * 0.15F;
		this.particleGreen = 0.6F + this.rand.nextFloat() * 0.15F;
		this.particleBlue = 1.0F;
		this.particleAlpha = 0.95F;
		this.baseScale = 1.8F + this.rand.nextFloat() * 0.7F;
		this.particleScale = this.baseScale;
		this.particleMaxAge = 10 + this.rand.nextInt(7);
		this.canCollide = false;
		// The vanilla flame sprite is yellow, so any cyan tint multiplies into green; the frost sprite is neutral.
		TextureAtlasSprite sprite = ClientProxy.getFrostSprite();
		if (sprite != null) {
			// setParticleTexture only accepts the call once getFXLayer() reports the block atlas layer.
			this.usesSprite = true;
			this.setParticleTexture(sprite);
		} else {
			this.setParticleTextureIndex(48);
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

		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9D;
		this.motionY *= 0.95D;
		this.motionZ *= 0.9D;

	}

    @Override
    public void renderParticle(BufferBuilder buffer, Entity camera, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        float life = Math.min(1.0F, (this.particleAge + partialTicks) / this.particleMaxAge);
        float remaining = 1.0F - life;
        this.particleScale = this.baseScale * (0.3F + 0.7F * remaining);
        this.particleAlpha = 0.75F * remaining * remaining;
        this.particleRed = 0.35F + 0.15F * remaining;
        this.particleGreen = 0.85F + 0.1F * remaining;
        this.particleBlue = 1.0F;
        super.renderParticle(buffer, camera, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
        // A smaller, hot core gives the flame depth without changing global blend state.
        this.particleScale *= 0.48F;
        this.particleAlpha = 0.9F * remaining;
        this.particleRed = 0.65F + 0.2F * remaining;
        this.particleGreen = 0.95F;
        super.renderParticle(buffer, camera, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

	@Override
	public int getBrightnessForRender(float partialTick) {
		return 15728880;
	}

	@Override
	public int getFXLayer() {
		return this.usesSprite ? 1 : 0;
	}
}
