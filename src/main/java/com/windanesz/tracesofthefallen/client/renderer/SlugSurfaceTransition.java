package com.windanesz.tracesofthefallen.client.renderer;

import net.minecraft.util.EnumFacing;

/** Per-slug visual pose; physics can latch immediately while the body eases around the corner. */
final class SlugSurfaceTransition {

	static final float DURATION_TICKS = 8.0F;
	private EnumFacing facing;
	private Pose from;
	private Pose to;
	private float startTime;

	Pose sample(EnumFacing facing, float width, float height, float time) {
		if (this.facing == null) {
			this.facing = facing;
			this.from = this.to = Pose.forFacing(facing, width, height);
			this.startTime = time;
		} else if (this.facing != facing) {
			// Retarget from the visible pose even if another corner interrupts the previous transition.
			this.from = this.interpolate(time);
			this.to = Pose.forFacing(facing, width, height);
			this.facing = facing;
			this.startTime = time;
		}
		return this.interpolate(time);
	}

	private Pose interpolate(float time) {
		float t = Math.max(0.0F, Math.min(1.0F, (time - this.startTime) / DURATION_TICKS));
		t = t * t * (3.0F - 2.0F * t);
		return Pose.interpolate(this.from, this.to, t);
	}

	static final class Pose {
		final float offsetX, offsetY, offsetZ;
		final float x, y, z, w;

		private Pose(float offsetX, float offsetY, float offsetZ, float x, float y, float z, float w) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
			float length = (float) Math.sqrt(x * x + y * y + z * z + w * w);
			this.x = x / length;
			this.y = y / length;
			this.z = z / length;
			this.w = w / length;
		}

		private static Pose forFacing(EnumFacing facing, float width, float height) {
			// Preserve the one-pixel inset and put the belly at the touching hitbox edge.
			float offset = facing == EnumFacing.UP ? 0.0F
					: 1.0F / 16.0F + (facing == EnumFacing.DOWN ? height : width * 0.5F);
			float halfSqrt = (float) Math.sqrt(0.5D);
			float x = 0.0F, z = 0.0F, w = halfSqrt;
			switch (facing) {
				case NORTH: x = -halfSqrt; break;
				case SOUTH: x = halfSqrt; break;
				case EAST: z = -halfSqrt; break;
				case WEST: z = halfSqrt; break;
				case DOWN: x = 1.0F; w = 0.0F; break;
				default: w = 1.0F; break;
			}
			return new Pose(-facing.getXOffset() * offset, -facing.getYOffset() * offset,
					-facing.getZOffset() * offset, x, 0.0F, z, w);
		}

		private static Pose interpolate(Pose a, Pose b, float t) {
			// Normalized quaternion interpolation avoids Euler flips and takes the short rotation path.
			float sign = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w < 0.0F ? -1.0F : 1.0F;
			float s = 1.0F - t;
			return new Pose(s * a.offsetX + t * b.offsetX, s * a.offsetY + t * b.offsetY,
					s * a.offsetZ + t * b.offsetZ, s * a.x + t * sign * b.x,
					s * a.y + t * sign * b.y, s * a.z + t * sign * b.z, s * a.w + t * sign * b.w);
		}
	}
}
