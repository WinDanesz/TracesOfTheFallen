package com.windanesz.tracesofthefallen.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityTelescope extends TileEntity {

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1.0D, this.pos.getY() + 2.0D, this.pos.getZ() + 1.0D);
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 4096.0D;
	}
}
