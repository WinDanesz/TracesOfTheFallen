package com.windanesz.tracesofthefallen.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBase extends Particle {

	protected TextureManager textureManager;
	public ResourceLocation texture;
	private static final VertexFormat VERTEX_FORMAT = (new VertexFormat())
			.addElement(DefaultVertexFormats.POSITION_3F)
			.addElement(DefaultVertexFormats.TEX_2F)
			.addElement(DefaultVertexFormats.COLOR_4UB)
			.addElement(DefaultVertexFormats.TEX_2S)
			.addElement(DefaultVertexFormats.NORMAL_3B)
			.addElement(DefaultVertexFormats.PADDING_1B);

	public float renderYOffset;
	public int texSpot;
	public int texSheetSeg;

	public ParticleBase(TextureManager textureManager, World world, double x, double y, double z, double speedX,
			double speedY, double speedZ, ResourceLocation resource, int texSpotIn) {
		this(textureManager, world, x, y, z, speedX, speedY, speedZ, resource, texSpotIn, 4);
	}

	public ParticleBase(TextureManager textureManager, World world, double x, double y, double z, double speedX,
			double speedY, double speedZ, ResourceLocation resource, int texSpotIn, int texSheetSeg) {
		super(world, x, y, z, speedX, speedY, speedZ);
		this.textureManager = textureManager;
		this.renderYOffset = 0;
		this.texture = resource;
		this.texSpot = texSpotIn;
		this.texSheetSeg = texSheetSeg;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		this.textureManager.bindTexture(texture);
		float minU = (float) (texSpot % texSheetSeg) / texSheetSeg;
		float maxU = minU + (1.0F / texSheetSeg);
		float minV = (float) (texSpot / texSheetSeg) / texSheetSeg;
		float maxV = minV + (1.0F / texSheetSeg);
		float particleSize = this.particleScale * 0.1F;
		float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks
				- (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks));
		float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks
				- (entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks) + renderYOffset);
		float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks
				- (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks));
		int brightness = getBrightnessForRender(partialTicks);
		int lightX = brightness >> 16 & 65535;
		int lightY = brightness & 65535;
		Vec3d[] vertices = particleVertexRendering(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ,
				rotationXY, rotationXZ, particleSize);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		buffer.begin(7, VERTEX_FORMAT);
		buffer.pos(x + vertices[0].x, y + vertices[0].y, z + vertices[0].z).tex(maxU, maxV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY)
				.normal(0.0F, 1.0F, 0.0F).endVertex();
		buffer.pos(x + vertices[1].x, y + vertices[1].y, z + vertices[1].z).tex(maxU, minV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY)
				.normal(0.0F, 1.0F, 0.0F).endVertex();
		buffer.pos(x + vertices[2].x, y + vertices[2].y, z + vertices[2].z).tex(minU, minV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY)
				.normal(0.0F, 1.0F, 0.0F).endVertex();
		buffer.pos(x + vertices[3].x, y + vertices[3].y, z + vertices[3].z).tex(minU, maxV)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightX, lightY)
				.normal(0.0F, 1.0F, 0.0F).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.disableBlend();
	}

	public Vec3d[] particleVertexRendering(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ, float particleSize) {
		return new Vec3d[] {
				new Vec3d(-rotationX * particleSize - rotationXY * particleSize, -rotationZ * particleSize,
						-rotationYZ * particleSize - rotationXZ * particleSize),
				new Vec3d(-rotationX * particleSize + rotationXY * particleSize, rotationZ * particleSize,
						-rotationYZ * particleSize + rotationXZ * particleSize),
				new Vec3d(rotationX * particleSize + rotationXY * particleSize, rotationZ * particleSize,
						rotationYZ * particleSize + rotationXZ * particleSize),
				new Vec3d(rotationX * particleSize - rotationXY * particleSize, -rotationZ * particleSize,
						rotationYZ * particleSize - rotationXZ * particleSize) };
	}

	public int brightnessIncreaseToFull(float partialTicks) {
		float progress = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge;
		progress = MathHelper.clamp(progress, 0.0F, 1.0F);
		int brightness = super.getBrightnessForRender(partialTicks);
		int blockLight = brightness & 255;
		int skyLight = brightness >> 16 & 255;
		blockLight = blockLight + (int) (progress * 15.0F * 16.0F);

		if (blockLight > 240) {
			blockLight = 240;
		}

		return blockLight | skyLight << 16;
	}

	public float[] decimalIntToRGB(int color) {
		int red = (color & 16711680) >> 16;
		int green = (color & 65280) >> 8;
		int blue = color & 255;
		return new float[] { red / 255.0F, green / 255.0F, blue / 255.0F };
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
}
