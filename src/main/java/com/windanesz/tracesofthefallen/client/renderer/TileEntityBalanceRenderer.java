package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BalanceMode;
import com.windanesz.tracesofthefallen.block.BlockBalance;
import com.windanesz.tracesofthefallen.block.TileEntityBalance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntityBalanceRenderer extends TileEntitySpecialRenderer<TileEntityBalance> {

	private static final ModelResourceLocation BEAM_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_beam", "inventory");
	private static final ModelResourceLocation LEFT_PAN_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_left_pan", "inventory");
	private static final ModelResourceLocation RIGHT_PAN_MODEL = new ModelResourceLocation(TracesOfTheFallen.MODID + ":balance_right_pan", "inventory");
	private static final double PIVOT_X = 0.5D;
	private static final double PIVOT_Y = 13.0D / 16.0D;
	private static final double PIVOT_Z = 0.5D;
	private static final double LEFT_PAN_ORIGIN_X = 2.2D / 16.0D;
	private static final double RIGHT_PAN_ORIGIN_X = 15.2D / 16.0D;
	private static final double LEFT_PAN_ITEM_X = 1.6D / 16.0D;
	private static final double RIGHT_PAN_ITEM_X = 14.6D / 16.0D;
	private static final double PAN_ORIGIN_Y = 12.0D / 16.0D;
	private static final double PAN_ORIGIN_Z = 0.5D;
	private static final double PAN_Y = 0.36D;
	private static final double FULL_MODEL_SCALE_MULTIPLIER = 0.73828125D;
	private static final double FULL_ITEM_Y_OFFSET = ((1.0D / 16.0D) + (1.0D / 80.0D) + (1.0D / 32.0D)) * FULL_MODEL_SCALE_MULTIPLIER;
	private static final double FLAT_ITEM_Y_OFFSET = 1.0D / 64.0D;
	private static final double ITEM_SCALE = 0.38D;
	private static final double PAN_HEIGHT = 4.42388D / 16.0D;
	private static final double TEXT_HEIGHT = 0.38D;
	private static final double TEXT_SCALE = 0.007D;
	private static final double BASE_TEXT_HEIGHT = 0.14D;
	private static final double LEFT_PAN_TEXT_X = 1.6D / 16.0D;
	private static final double RIGHT_PAN_TEXT_X = 14.6D / 16.0D;

	@Override
	public void render(TileEntityBalance te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		float yaw = getFacingAngle(state.getValue(BlockBalance.FACING));
		float tilt = te.getInterpolatedTilt(partialTicks);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		renderBeam(te, state, yaw, tilt);
		renderPanAssembly(te, state, LEFT_PAN_MODEL, te.getLeftStack(), LEFT_PAN_ORIGIN_X, LEFT_PAN_ITEM_X, yaw, tilt);
		renderPanAssembly(te, state, RIGHT_PAN_MODEL, te.getRightStack(), RIGHT_PAN_ORIGIN_X, RIGHT_PAN_ITEM_X, yaw, tilt);
		renderLabels(te, state, tilt);

		GlStateManager.popMatrix();
	}

	private void renderBeam(TileEntityBalance te, IBlockState state, float yaw, float tilt) {
		GlStateManager.pushMatrix();
		applyBeamTransform(yaw, tilt);
		renderModel(te, state, BEAM_MODEL);
		GlStateManager.popMatrix();
	}

	private void renderPanAssembly(TileEntityBalance te, IBlockState state, ModelResourceLocation modelLocation, ItemStack stack, double panOriginX,
			double panItemX, float yaw, float tilt) {
		GlStateManager.pushMatrix();
		applyBeamTransform(yaw, tilt);
		GlStateManager.translate(panOriginX, PAN_ORIGIN_Y, PAN_ORIGIN_Z);
		GlStateManager.rotate(-tilt, 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(-panOriginX, -PAN_ORIGIN_Y, -PAN_ORIGIN_Z);
		renderModel(te, state, modelLocation);
		renderPanItem(stack, panItemX);
		GlStateManager.popMatrix();
	}

	private static void applyBeamTransform(float yaw, float tilt) {
		GlStateManager.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(tilt, 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);
	}

	private void renderModel(TileEntityBalance te, IBlockState state, ModelResourceLocation modelLocation) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(modelLocation);
		if (model == null) {
			return;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		BlockPos pos = te.getPos();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, buffer, false);
		buffer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}

	private void renderPanItem(ItemStack stack, double panItemX) {
		if (stack.isEmpty()) {
			return;
		}

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
		boolean fullModel = model != null && model.isGui3d();

		GlStateManager.pushMatrix();
		GlStateManager.translate(panItemX, getPanItemY(fullModel), PIVOT_Z);
		double itemScale = getPanItemScale(fullModel);
		GlStateManager.scale(itemScale, itemScale, itemScale);
		GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popMatrix();
	}

	private static double getPanItemY(boolean fullModel) {
		return PAN_Y + (fullModel ? FULL_ITEM_Y_OFFSET : FLAT_ITEM_Y_OFFSET);
	}

	private static double getPanItemScale(boolean fullModel) {
		return fullModel ? ITEM_SCALE * FULL_MODEL_SCALE_MULTIPLIER : ITEM_SCALE;
	}

	private void renderLabels(TileEntityBalance te, IBlockState state, float tilt) {
		if (!shouldRenderLabels(te)) {
			return;
		}

		if (te.getMode() == BalanceMode.VENDING && !te.getOwnerName().isEmpty()) {
			renderLabel(getBaseAnchor(), getSellerText(te));
		}
		if (!te.getLeftStack().isEmpty()) {
			renderLabel(getPanAnchor(state.getValue(BlockBalance.FACING), true, tilt), getLeftText(te));
		}
		if (!te.getRightStack().isEmpty()) {
			renderLabel(getPanAnchor(state.getValue(BlockBalance.FACING), false, tilt), getRightText(te));
		}
	}

	private static boolean shouldRenderLabels(TileEntityBalance te) {
		RayTraceResult target = Minecraft.getMinecraft().objectMouseOver;
		return target != null && target.typeOfHit == RayTraceResult.Type.BLOCK && te.getPos().equals(target.getBlockPos());
	}

	private static String getLeftText(TileEntityBalance balance) {
		return balance.getLeftStack().getCount() + " " + balance.getLeftStack().getDisplayName();
	}

	private static String getRightText(TileEntityBalance balance) {
		ItemStack stack = balance.getRightStack();
		String text = stack.getCount() + " " + stack.getDisplayName();
		if (balance.getMode() == BalanceMode.VENDING) {
			return I18n.format("totf:balance.price", text);
		}
		return text;
	}

	private static String getSellerText(TileEntityBalance balance) {
		return I18n.format("totf:balance.offered_by", balance.getOwnerName());
	}

	private static Vec3d getPanAnchor(EnumFacing facing, boolean left, float tiltDegrees) {
		Vec3d forward = new Vec3d(facing.getXOffset(), 0.0D, facing.getZOffset());
		Vec3d rightVector = new Vec3d(-forward.z, 0.0D, forward.x);
		double horizontalDisplacement = getPanHorizontalDisplacement(left, tiltDegrees);
		double verticalDisplacement = getPanVerticalDisplacement(left, tiltDegrees);
		double localX = getPanTextX(left) + horizontalDisplacement;
		Vec3d base = new Vec3d(0.5D, PAN_HEIGHT + TEXT_HEIGHT + verticalDisplacement, 0.5D);
		return base.add(rightVector.scale(localX - 0.5D));
	}

	private static double getPanHorizontalDisplacement(boolean left, float tiltDegrees) {
		double anchorOffsetX = getPanOriginX(left) - PIVOT_X;
		double radians = Math.toRadians(tiltDegrees);
		return (anchorOffsetX * Math.cos(radians)) - anchorOffsetX;
	}

	private static double getPanVerticalDisplacement(boolean left, float tiltDegrees) {
		double anchorOffsetX = getPanOriginX(left) - PIVOT_X;
		double anchorOffsetY = PAN_ORIGIN_Y - PIVOT_Y;
		double radians = Math.toRadians(tiltDegrees);
		return (anchorOffsetX * Math.sin(radians)) + (anchorOffsetY * Math.cos(radians)) - anchorOffsetY;
	}

	private static double getPanOriginX(boolean left) {
		return left ? LEFT_PAN_ORIGIN_X : RIGHT_PAN_ORIGIN_X;
	}

	private static double getPanTextX(boolean left) {
		return left ? LEFT_PAN_TEXT_X : RIGHT_PAN_TEXT_X;
	}

	private static Vec3d getBaseAnchor() {
		return new Vec3d(0.5D, BASE_TEXT_HEIGHT, 0.5D);
	}

	private static void renderLabel(Vec3d position, String text) {
		Minecraft minecraft = Minecraft.getMinecraft();
		FontRenderer fontRenderer = minecraft.fontRenderer;

		GlStateManager.pushMatrix();
		GlStateManager.translate(position.x, position.y, position.z);
		GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(minecraft.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();

		int halfWidth = fontRenderer.getStringWidth(text) / 2;
		drawBackground(-halfWidth - 3, -2, halfWidth + 3, 9);
		fontRenderer.drawStringWithShadow(text, -halfWidth, 0, 0xFFFFFF);

		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	private static void drawBackground(int left, int top, int right, int bottom) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.disableTexture2D();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(left, bottom, 0.0D).color(0.0F, 0.0F, 0.0F, 0.35F).endVertex();
		buffer.pos(right, bottom, 0.0D).color(0.0F, 0.0F, 0.0F, 0.35F).endVertex();
		buffer.pos(right, top, 0.0D).color(0.0F, 0.0F, 0.0F, 0.35F).endVertex();
		buffer.pos(left, top, 0.0D).color(0.0F, 0.0F, 0.0F, 0.35F).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
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
