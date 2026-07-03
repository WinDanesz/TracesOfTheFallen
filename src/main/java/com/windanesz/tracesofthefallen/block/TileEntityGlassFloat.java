package com.windanesz.tracesofthefallen.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityGlassFloat extends TileEntity implements ITickable {

	private float pitch;
	private float prevPitch;
	private float roll;
	private float prevRoll;
	private float bobOffset;
	private float prevBobOffset;

	@Override
	public void update() {
		prevPitch = pitch;
		prevRoll = roll;
		prevBobOffset = bobOffset;

		if (world == null || !world.isRemote) {
			return;
		}

		float time = world.getTotalWorldTime();
		float phase = (pos.getX() * 7 + pos.getZ() * 13) & 31;
		float targetPitch = (float) Math.sin((time + phase) * 0.08F) * 4.0F;
		float targetRoll = (float) Math.cos((time + phase) * 0.06F) * 3.0F;
		float targetBob = (float) Math.sin((time + phase) * 0.04F) * 0.03F;

		pitch += (targetPitch - pitch) * 0.2F;
		roll += (targetRoll - roll) * 0.2F;
		bobOffset += (targetBob - bobOffset) * 0.15F;
	}

	public float getInterpolatedPitch(float partialTicks) {
		return prevPitch + (pitch - prevPitch) * partialTicks;
	}

	public float getInterpolatedRoll(float partialTicks) {
		return prevRoll + (roll - prevRoll) * partialTicks;
	}

	public float getInterpolatedBobOffset(float partialTicks) {
		return prevBobOffset + (bobOffset - prevBobOffset) * partialTicks;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(0.5D, 0.5D, 0.5D);
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 4096.0D;
	}
}
