package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityCenser extends TileEntityBurningIncense {

	private static final int CENSER_AURA_RADIUS = 2; // 5x5 area

	public TileEntityCenser() {
		super(false);
	}

	@Override
	protected boolean supportsFuelItems() {
		return true;
	}

	@Override
	protected boolean getDefaultLitState() {
		return false;
	}

	@Override
	protected void spawnBurningParticle() {
		double baseX = pos.getX() + 0.5D + (world.rand.nextDouble() - 0.5D) * 0.03D;
		double baseY = pos.getY() + 0.53D + world.rand.nextDouble() * 0.04D;
		double baseZ = pos.getZ() + 0.5D + (world.rand.nextDouble() - 0.5D) * 0.03D;
		double motionX = (world.rand.nextDouble() - 0.5D) * 0.00018D;
		double motionY = 0.0045D + world.rand.nextDouble() * 0.0012D;
		double motionZ = (world.rand.nextDouble() - 0.5D) * 0.00018D;

		spawnSmokeParticle(baseX, baseY, baseZ, motionX, motionY, motionZ);
	}

	@Override
	protected AxisAlignedBB getAuraArea() {
		return IncenseEffects.getAuraArea(pos, CENSER_AURA_RADIUS);
	}

	public int getAuraRadius() {
		return CENSER_AURA_RADIUS;
	}
}
