package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFloorMist extends ParticleBase {

	private static final ResourceLocation MIST_TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/particle/mist.png");

	public ParticleFloorMist(TextureManager textureManager, World world, double x, double y, double z, double movementX,
			double movementY, double movementZ, int color) {
		super(textureManager, world, x, y, z, movementX, movementY, movementZ, MIST_TEXTURE, 0);
		this.textureManager = textureManager;
		this.motionX = movementX;
		this.motionY = movementY;
		this.motionZ = movementZ;
		this.canCollide = true;
		this.particleMaxAge = 220 + this.rand.nextInt(60);
		this.texSheetSeg = 1;
		this.renderYOffset = 0.001F;
		this.particleScale = 10.0F * (this.rand.nextFloat() + 0.5F);
		this.particleGravity = 0.011F;
		this.setAlphaF(0.0F);

		float[] colors = decimalIntToRGB(color);
		float shade = 0.5F;
		setRBGColorF(1.0F - (1.0F - colors[0]) * shade, 1.0F - (1.0F - colors[1]) * shade,
				1.0F - (1.0F - colors[2]) * shade);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		float alpha;
		float halfLife = this.particleMaxAge * 0.5F;

		if (this.particleAge <= halfLife) {
			float progress = this.particleAge / halfLife;
			alpha = 0.5F * progress;
		} else {
			float progress = (this.particleAge - halfLife) / halfLife;
			alpha = 0.5F * (1.0F - progress);
		}
		this.setAlphaF(alpha);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.depthMask(false);
		super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		GlStateManager.depthMask(true);
	}

	@Override
	public Vec3d[] particleVertexRendering(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ, float particleSize) {
		double tilt = Math.toRadians(90);
		double cos = Math.cos(tilt);
		double sin = Math.sin(tilt);
		return new Vec3d[] { new Vec3d(-particleSize, particleSize * cos, -particleSize * sin),
				new Vec3d(-particleSize, -particleSize * cos, particleSize * sin),
				new Vec3d(particleSize, -particleSize * cos, particleSize * sin),
				new Vec3d(particleSize, particleSize * cos, -particleSize * sin) };
	}
}
