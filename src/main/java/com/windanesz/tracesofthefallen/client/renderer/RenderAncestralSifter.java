package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockAncestralSifter;
import com.windanesz.tracesofthefallen.block.BlockDecoration;
import com.windanesz.tracesofthefallen.entity.EntityAncestralSifter;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderAncestralSifter extends Render<EntityAncestralSifter> {

	private static final String[] FEATHER_PARTS = {
			"left_feather_1",
			"left_feather_2",
			"left_feather_3",
			"left_feather_4",
			"right_feather_1",
			"right_feather_2",
			"right_feather_3",
			"right_feather_4"
	};
	private static final double[] FEATHER_PIVOT_X = {
			-3.0D / 16.0D,
			-3.0D / 16.0D,
			-3.0D / 16.0D,
			-5.0D / 16.0D,
			19.0D / 16.0D,
			19.0D / 16.0D,
			19.0D / 16.0D,
			21.0D / 16.0D
	};
	private static final double[] FEATHER_PIVOT_Y = {
			19.0D / 16.0D,
			14.0D / 16.0D,
			9.0D / 16.0D,
			7.0D / 16.0D,
			19.0D / 16.0D,
			14.0D / 16.0D,
			9.0D / 16.0D,
			7.0D / 16.0D
	};
	private static final float[] FEATHER_DIRECTION = {1.0F, 1.0F, 1.0F, 1.0F, -1.0F, -1.0F, -1.0F, -1.0F};
	private static final float[] FEATHER_PHASE = {0.0F, 0.35F, 0.7F, 1.05F, 0.15F, 0.5F, 0.85F, 1.2F};
	private static final double FEATHER_PIVOT_Z = 8.0D / 16.0D;

	public RenderAncestralSifter(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.0F;
	}

	@Override
	public void doRender(EntityAncestralSifter entity, double x, double y, double z, float entityYaw, float partialTicks) {
		IBlockState state = getRenderState(entity);
		BlockPos pos = entity.getAnchorPos();

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x - 0.5D, y, z - 0.5D);
		GlStateManager.translate(0.5D, 0.0D, 0.5D);
		GlStateManager.rotate(getFacingAngle(entity.getFacing()), 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5D, 0.0D, -0.5D);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		renderModel(entity, state, pos, getModelLocation(entity, "body"));
		renderFeathers(entity, state, pos, partialTicks);

		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityAncestralSifter entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	private void renderModel(EntityAncestralSifter entity, IBlockState state, BlockPos pos, ModelResourceLocation modelLocation) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(modelLocation);
		if (model == null) {
			return;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(entity.world, model, state, pos, buffer, false);
		buffer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
	}

	private void renderFeathers(EntityAncestralSifter entity, IBlockState state, BlockPos pos, float partialTicks) {
		float time = (entity.ticksExisted + partialTicks) * 0.12F;
		for (int i = 0; i < FEATHER_PARTS.length; i++) {
			float featherSway = MathHelper.sin(time + FEATHER_PHASE[i]) * 1.3F * FEATHER_DIRECTION[i];
			float sidewaysSway = MathHelper.cos((time * 1.15F) + FEATHER_PHASE[i]) * 2.2F * FEATHER_DIRECTION[i];
			GlStateManager.pushMatrix();
			GlStateManager.translate(FEATHER_PIVOT_X[i], FEATHER_PIVOT_Y[i], FEATHER_PIVOT_Z);
			GlStateManager.rotate(sidewaysSway, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(featherSway, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(-FEATHER_PIVOT_X[i], -FEATHER_PIVOT_Y[i], -FEATHER_PIVOT_Z);
			renderModel(entity, state, pos, getModelLocation(entity, FEATHER_PARTS[i]));
			GlStateManager.popMatrix();
		}
	}

	private static IBlockState getRenderState(EntityAncestralSifter entity) {
		BlockAncestralSifter block = entity.getVariant() == BlockAncestralSifter.SifterVariant.RED
				? (BlockAncestralSifter) ModBlocks.ancestral_sifter_red
				: (BlockAncestralSifter) ModBlocks.ancestral_sifter_golden;
		return block.getDefaultState()
				.withProperty(BlockDecoration.FACING, EnumFacing.NORTH)
				.withProperty(BlockAncestralSifter.BROKEN, entity.isBroken());
	}

	private static ModelResourceLocation getModelLocation(EntityAncestralSifter entity, String partSuffix) {
		String variant = entity.getVariant() == BlockAncestralSifter.SifterVariant.RED ? "red" : "golden";
		String damageState = entity.isBroken() ? "_broken" : entity.isWeathered() ? "_weathered" : "";
		return new ModelResourceLocation(TracesOfTheFallen.MODID + ":ancestral_sifter_" + variant + damageState + "_" + partSuffix, "inventory");
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
}
