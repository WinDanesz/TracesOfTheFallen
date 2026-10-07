package com.windanesz.tracesofthefallen.client.renderer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** The visible back surface of a victim, sampled in the same frame as its own renderer. */
final class SlugAttachmentPose {

	static Vec3d interpolatedPosition(Entity entity, float partialTicks) {
		return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks,
				entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks,
				entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks);
	}

	static float bodyYaw(EntityLivingBase target, float partialTicks) {
		return target.prevRenderYawOffset + MathHelper.wrapDegrees(target.renderYawOffset - target.prevRenderYawOffset) * partialTicks;
	}

	static Vec3d backPosition(EntityLivingBase target, float partialTicks) {
		// A player's torso is four pixels deep, much narrower than its collision box.
		double backDepth = target instanceof EntityPlayer ? 2.0D / 16.0D : target.width * 0.5D;
		return backPosition(interpolatedPosition(target, partialTicks), bodyYaw(target, partialTicks), target.height, backDepth);
	}

	static Vec3d backPosition(Vec3d position, float yaw, float height, double backDepth) {
		double radians = Math.toRadians(yaw);
		return position.add(Math.sin(radians) * backDepth, height * 0.55D, -Math.cos(radians) * backDepth);
	}
}
