package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.entity.living.EntitySlug;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Shared renderer for the slug family: on top of the usual yaw rotation, it tips the model to lie flat
 * against whatever wall or ceiling the slug is currently clinging to.
 */
public abstract class RenderSlug<T extends EntitySlug> extends RenderLiving<T> {

	private final Map<T, SlugSurfaceTransition> surfaceTransitions = new WeakHashMap<>();

	protected RenderSlug(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
		super(renderManagerIn, modelBaseIn, shadowSizeIn);
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		EntityLivingBase target = entity.getAttachedTarget();
		if (target != null && entity.deathTime == 0) {
			// Use the victim's visible position instead of the slug's independently interpolated packets.
			Vec3d offset = SlugAttachmentPose.backPosition(target, partialTicks)
					.subtract(SlugAttachmentPose.interpolatedPosition(entity, partialTicks));
			x += offset.x;
			y += offset.y;
			z += offset.z;
		}
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void applyRotations(T entity, float ageInTicks, float rotationYaw, float partialTicks) {
		EnumFacing facing = entity.getClimbFacing();
		if (entity.deathTime > 0) {
			super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
			return;
		}
		if (entity.isAttached()) {
			this.surfaceTransitions.remove(entity);
			EntityLivingBase target = entity.getAttachedTarget();
			if (target != null) {
				rotationYaw = SlugAttachmentPose.bodyYaw(target, partialTicks);
			}
			// Match the same body yaw used to position the belly, rather than the slug's body helper.
			GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
			if (target == null) {
				// Until the victim is loaded, retain the fallback based on the server attachment position.
				GlStateManager.translate(0.0F, 0.0F, -entity.width * 0.35F);
			}
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			return;
		}

		SlugSurfaceTransition transition = this.surfaceTransitions.computeIfAbsent(entity, key -> new SlugSurfaceTransition());
		SlugSurfaceTransition.Pose pose = transition.sample(facing, entity.width, entity.height, entity.ticksExisted + partialTicks);
		GlStateManager.translate(pose.offsetX, pose.offsetY, pose.offsetZ);
		float axisLength = (float) Math.sqrt(pose.x * pose.x + pose.y * pose.y + pose.z * pose.z);
		if (axisLength > 1.0E-6F) {
			float angle = (float) Math.toDegrees(2.0D * Math.atan2(axisLength, pose.w));
			GlStateManager.rotate(angle, pose.x / axisLength, pose.y / axisLength, pose.z / axisLength);
		}
		if (facing == EnumFacing.UP) {
			super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
		} else {
			GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
		}
	}
}
