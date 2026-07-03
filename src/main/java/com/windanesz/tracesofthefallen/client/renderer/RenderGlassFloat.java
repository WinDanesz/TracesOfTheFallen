package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGlassFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderGlassFloat extends Render<EntityGlassFloat> {

	private static final ModelResourceLocation ROPE_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":rope_mossy", "facing=south");
	private static final ModelResourceLocation HOOKED_ROPE_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":rope_mossy_hook", "facing=south");
	private static final double ROPE_Y_OFFSET = 7.0D / 16.0D;
	private static final double ROPE_X_OFFSET = 0.0D;
	private static final double ROPE_Z_OFFSET = 0.0D;

	public RenderGlassFloat(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public boolean shouldRender(EntityGlassFloat livingEntity, ICamera camera, double camX, double camY, double camZ) {
		if (super.shouldRender(livingEntity, camera, camX, camY, camZ)) {
			return true;
		}

		if (!livingEntity.hasRope() || !livingEntity.isInRangeToRender3d(camX, camY, camZ)) {
			return false;
		}

		AxisAlignedBB ropeBounds = livingEntity.getEntityBoundingBox().grow(0.25D);
		ropeBounds = new AxisAlignedBB(
				ropeBounds.minX,
				ropeBounds.minY - livingEntity.getRopeCount() - 1.0D,
				ropeBounds.minZ,
				ropeBounds.maxX,
				ropeBounds.maxY,
				ropeBounds.maxZ);

		return livingEntity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(ropeBounds);
	}

	@Override
	public void doRender(EntityGlassFloat entity, double x, double y, double z, float entityYaw, float partialTicks) {
		IBakedModel floatTopModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
				.getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":" + entity.getVariant().getWaterTopModelName(), "inventory"));
		IBakedModel floatBottomModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
				.getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":glass_float_water_bottom", "inventory"));
		if (floatTopModel == null) {
			return;
		}

		IBakedModel ropeModel = entity.hasRope()
				? Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(ROPE_MODEL)
				: null;
		IBakedModel hookedRopeModel = entity.hasRope()
				? Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(HOOKED_ROPE_MODEL)
				: null;

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		setLightmap(entity, entity.getAnchorPos().down());
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		renderFloatBottom(floatBottomModel, entity, x, y, z, partialTicks);
		renderFloatTop(floatTopModel, entity, x, y, z, partialTicks);
		GlStateManager.disableBlend();
		renderRope(ropeModel, hookedRopeModel, entity, x, y, z, partialTicks);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
	}

	private static void renderModel(IBakedModel model) {
		if (model != null) {
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private static void renderFloatTop(IBakedModel model, EntityGlassFloat entity, double x, double y, double z, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + entity.getBobOffset(partialTicks), z);
		GlStateManager.rotate(entity.getWavePitch(partialTicks), 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(entity.getWaveRoll(partialTicks), 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);
		renderModel(model);
		GlStateManager.popMatrix();
	}

	private static void renderFloatBottom(IBakedModel model, EntityGlassFloat entity, double x, double y, double z, float partialTicks) {
		if (model == null) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + entity.getBobOffset(partialTicks), z);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);
		renderModel(model);
		GlStateManager.popMatrix();
	}

	private static void renderRope(IBakedModel ropeModel, IBakedModel hookedRopeModel, EntityGlassFloat entity, double x, double y, double z, float partialTicks) {
		if (ropeModel == null) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + entity.getBobOffset(partialTicks), z);
		GlStateManager.translate(-0.5D, -0.5D, -0.5D);

		for (int i = 1; i <= entity.getRopeCount(); i++) {
			setLightmap(entity, entity.getAnchorPos().down(i + 1));
			GlStateManager.pushMatrix();
			GlStateManager.translate(ROPE_X_OFFSET, -(i + ROPE_Y_OFFSET), ROPE_Z_OFFSET);
			if (entity.isHookedSegment(i)) {
				GlStateManager.translate(0.5D, 0.5D, 0.5D);
				GlStateManager.rotate(getHookRotation(i), 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-0.5D, -0.5D, -0.5D);
				renderModel(hookedRopeModel);
				if (entity.hasSquidOnHookForRender((i / 2) - 1)) {
					renderHookSquid(entity, i, partialTicks);
				} else {
					renderHookLoot(entity, i);
				}
			} else {
				renderModel(ropeModel);
			}
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

	private static void setLightmap(EntityGlassFloat entity, BlockPos pos) {
		int combinedLight = entity.world.getCombinedLight(pos, 0);
		int blockLight = combinedLight & 65535;
		int skyLight = combinedLight >> 16;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
	}

	private static void renderHookLoot(EntityGlassFloat entity, int ropeSegment) {
		ItemStack stack = entity.getHookLootForRender((ropeSegment / 2) - 1);
		if (stack.isEmpty()) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, 0.5625D, 0.8875D);
		GlStateManager.scale(0.4375F, 0.4375F, 0.4375F);
		GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.popMatrix();
	}

	private static void renderHookSquid(EntityGlassFloat entity, int ropeSegment, float partialTicks) {
		Render<? super EntitySquid> squidRenderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(EntitySquid.class);
		if (squidRenderer == null) {
			return;
		}

		EntitySquid squid = new EntitySquid(entity.world);
		squid.setPosition(entity.posX, entity.posY - ropeSegment, entity.posZ);
		squid.ticksExisted = entity.ticksExisted;

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5D, 0.25D, 0.75D);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		squidRenderer.doRender(squid, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.popMatrix();
	}

	private static float getHookRotation(int ropeSegment) {
		int hookedSegmentIndex = (ropeSegment / 2) - 1;
		return (hookedSegmentIndex & 3) * 90.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGlassFloat entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
}
