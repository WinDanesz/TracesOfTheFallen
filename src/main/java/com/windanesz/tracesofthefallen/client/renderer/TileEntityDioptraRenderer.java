package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockDecoration;
import com.windanesz.tracesofthefallen.block.TileEntityDioptra;
import com.windanesz.tracesofthefallen.entity.EntityDioptraSeat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TileEntityDioptraRenderer extends TileEntitySpecialRenderer<TileEntityDioptra> {

	private static final ModelResourceLocation ROTATABLE_MODEL_LOCATION = new ModelResourceLocation(TracesOfTheFallen.MODID + ":dioptra_moving", "inventory");
	private static final ModelResourceLocation VERTICAL_ROTATABLE_MODEL_LOCATION = new ModelResourceLocation(TracesOfTheFallen.MODID + ":dioptra_vertical_rotation", "inventory");
	private static final double ROTATION_ORIGIN_X = 0.5D;
	private static final double ROTATION_ORIGIN_Y = 15.0D / 16.0D;
	private static final double ROTATION_ORIGIN_Z = 0.5D;
	private static final float VERTICAL_AIM_BIAS = 13.5F;

	@Override
	public void render(TileEntityDioptra te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		EntityDioptraSeat seat = this.findSeat(te);
		float renderYaw = this.getRenderYaw(seat, state, partialTicks);
		float renderPitch = this.getRenderPitch(seat, partialTicks) + VERTICAL_AIM_BIAS;

		this.renderModel(te, state, this.getRotatingModel(), x, y, z, renderYaw, 0.0F);
		this.renderModel(te, state, this.getVerticalRotatingModel(), x, y, z, renderYaw, -renderPitch);
	}

	@Nullable
	private IBakedModel getRotatingModel() {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(ROTATABLE_MODEL_LOCATION);
	}

	@Nullable
	private IBakedModel getVerticalRotatingModel() {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(VERTICAL_ROTATABLE_MODEL_LOCATION);
	}

	private void renderModel(TileEntityDioptra te, IBlockState state, @Nullable IBakedModel model, double x, double y, double z, float yaw, float pitch) {
		if (model == null) {
			return;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		BlockPos pos = te.getPos();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(ROTATION_ORIGIN_X, ROTATION_ORIGIN_Y, ROTATION_ORIGIN_Z);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		if (pitch != 0.0F) {
			GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		}
		GlStateManager.translate(-ROTATION_ORIGIN_X, -ROTATION_ORIGIN_Y, -ROTATION_ORIGIN_Z);
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, buffer, false);
		buffer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	private float getRenderYaw(@Nullable EntityDioptraSeat seat, IBlockState state, float partialTicks) {

		if (seat != null && !seat.getPassengers().isEmpty()) {
			Entity rider = seat.getPassengers().get(0);
			float riderYaw = rider.prevRotationYaw + (rider.rotationYaw - rider.prevRotationYaw) * partialTicks;
			return 180.0F - riderYaw;
		}

		return getFacingAngle(state.getValue(BlockDecoration.FACING));
	}

	private float getRenderPitch(@Nullable EntityDioptraSeat seat, float partialTicks) {
		if (seat != null && !seat.getPassengers().isEmpty()) {
			Entity rider = seat.getPassengers().get(0);
			return rider.prevRotationPitch + (rider.rotationPitch - rider.prevRotationPitch) * partialTicks;
		}

		return 0.0F;
	}

	@Nullable
	private EntityDioptraSeat findSeat(TileEntityDioptra te) {
		List<EntityDioptraSeat> seats = te.getWorld().getEntitiesWithinAABB(EntityDioptraSeat.class, new AxisAlignedBB(te.getPos()).grow(0.6D),
				seat -> seat != null && !seat.isDead && te.getPos().equals(seat.getAnchorPos()));
		return seats.isEmpty() ? null : seats.get(0);
	}

	private static float getFacingAngle(EnumFacing facing) {
		switch (facing) {
			case EAST:
				return 90.0F;
			case SOUTH:
				return 180.0F;
			case WEST:
				return 270.0F;
			case NORTH:
			default:
				return 0.0F;
		}
	}
}
