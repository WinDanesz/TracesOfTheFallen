package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGoblinNest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderGoblinNest extends Render<EntityGoblinNest> {

	private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/blocks/hole_eyes_goblin.png");
	private static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(TracesOfTheFallen.MODID + ":goblin_nest", "inventory");

	public RenderGoblinNest(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.5F;
	}

	@Override
	public void doRender(EntityGoblinNest entity, double x, double y, double z, float entityYaw, float partialTicks) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(MODEL_LOCATION);
		if (model == null) {
			return;
		}

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + 0.015D, z);
		GlStateManager.rotate(getFacingAngle(entity.getFacing()), 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, 0.0D, -0.5D);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);

		float eyeAlpha = calculateEyeAlpha(entity);
		if (eyeAlpha > 0.01F) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.5D, 0.5D, 0.5D);
			this.bindTexture(EYES_TEXTURE);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.disableLighting();

			int combinedLight = entity.world.getCombinedLight(entity.getPosition(), 0);
			int blockLight = combinedLight & 65535;
			int skyLight = combinedLight >> 16;

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, eyeAlpha);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			double zOffset = -0.438D;
			buffer.pos(0.5D, 0.5D, zOffset).tex(0.0D, 0.0D).endVertex();
			buffer.pos(0.5D, -0.5D, zOffset).tex(0.0D, 1.0D).endVertex();
			buffer.pos(-0.5D, -0.5D, zOffset).tex(1.0D, 1.0D).endVertex();
			buffer.pos(-0.5D, 0.5D, zOffset).tex(1.0D, 0.0D).endVertex();
			tessellator.draw();

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) blockLight, (float) skyLight);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	private float calculateEyeAlpha(EntityGoblinNest entity) {
		if (entity.world == null) {
			return 0.0F;
		}

		int posX = MathHelper.floor(entity.posX);
		int posY = MathHelper.floor(entity.posY);
		int posZ = MathHelper.floor(entity.posZ);
		int offset = Math.abs((posX * 31 + posY * 11 + posZ * 7) % 1000);
		long time = (entity.world.getTotalWorldTime() + offset) % 240; // 12 second cycle

		// From time 140 to 240 (5 seconds): eyes are hidden in the dark
		if (time >= 140) {
			return 0.0F;
		}

		// Fade in from 0 to 15 ticks
		if (time < 15) {
			return (float) time / 15.0F;
		}

		// Fade out from 125 to 140 ticks
		if (time > 125) {
			return (float) (140 - time) / 15.0F;
		}

		return 1.0F;
	}

	private static float getFacingAngle(EnumFacing facing) {
		switch (facing) {
			case EAST:
				return 270.0F;
			case SOUTH:
				return 180.0F;
			case WEST:
				return 90.0F;
			case NORTH:
			default:
				return 0.0F;
		}
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityGoblinNest entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
}
