package com.windanesz.tracesofthefallen.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Stationary, surface-aligned mucus, kept alive independently of its slug. */
@SideOnly(Side.CLIENT)
public class ParticleSlugMucus extends Particle {

	private final Vec3d normal;
	private final Vec3d tangent;
	private final Vec3d bitangent;
	private final double surfaceOffset;
	// Adjacent strips form one uneven silhouette without stacking translucent layers.
	private static final float[][] STRIPS = {
			{-0.15F, 0.16F, -0.19F, -0.10F},
			{-0.22F, 0.22F, -0.10F, 0.10F},
			{-0.17F, 0.14F, 0.10F, 0.19F}
	};

	public ParticleSlugMucus(World world, double x, double y, double z, EnumFacing facing) {
		super(world, x, y, z);
		this.particleMaxAge = 100; // Five seconds at 20 game ticks per second.
		this.particleAlpha = 0.85F;
		this.particleRed = 0.65F;
		this.particleGreen = 0.8F;
		this.particleBlue = 0.25F;
		this.canCollide = false;
		this.normal = new Vec3d(facing.getDirectionVec());
		this.tangent = facing.getAxis() == EnumFacing.Axis.X ? new Vec3d(0, 0, 1) : new Vec3d(1, 0, 0);
		this.bitangent = this.normal.crossProduct(this.tangent);
		// Keep each patch on a slightly different plane, fixed for its entire lifetime.
		this.surfaceOffset = 0.008D + this.rand.nextDouble() * 0.016D;
		this.setParticleTexture(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
				.getTexture(Blocks.SLIME_BLOCK.getDefaultState()));
	}

	@Override
	public void onUpdate() {
		if (++this.particleAge >= this.particleMaxAge) {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity camera, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		float alpha = this.particleAlpha * Math.min(1.0F, Math.max(0.0F, (this.particleMaxAge - this.particleAge - partialTicks) / 20.0F));
		int light = this.getBrightnessForRender(partialTicks);
		Vec3d center = new Vec3d(this.posX - interpPosX, this.posY - interpPosY, this.posZ - interpPosZ)
				.add(this.normal.scale(this.surfaceOffset));
		for (float[] strip : STRIPS) {
			vertex(buffer, center, strip[0], strip[2], alpha, light);
			vertex(buffer, center, strip[1], strip[2], alpha, light);
			vertex(buffer, center, strip[1], strip[3], alpha, light);
			vertex(buffer, center, strip[0], strip[3], alpha, light);
		}
	}

	private void vertex(BufferBuilder buffer, Vec3d center, float u, float v, float alpha, int light) {
		float textureU = this.particleTexture.getInterpolatedU((u + 0.22F) / 0.44F * 16.0F);
		float textureV = this.particleTexture.getInterpolatedV((v + 0.19F) / 0.38F * 16.0F);
		buffer.pos(center.x + this.tangent.x * u + this.bitangent.x * v,
				center.y + this.tangent.y * u + this.bitangent.y * v,
				center.z + this.tangent.z * u + this.bitangent.z * v).tex(textureU, textureV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, alpha)
				.lightmap(light >> 16 & 65535, light & 65535).endVertex();
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
}
