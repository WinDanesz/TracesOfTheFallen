package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityIncenseBurner extends TileEntityBurningIncense {

	private static final int BURNER_AURA_RADIUS = 7; // 15x15 area

	public TileEntityIncenseBurner() {
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
		double baseX = pos.getX() + 0.5D + (world.rand.nextDouble() - 0.5D) * 0.08D;
		double baseY = pos.getY() + 0.72D + world.rand.nextDouble() * 0.08D;
		double baseZ = pos.getZ() + 0.5D + (world.rand.nextDouble() - 0.5D) * 0.08D;
		double motionX = (world.rand.nextDouble() - 0.5D) * 0.00018D;
		double motionY = 0.0045D + world.rand.nextDouble() * 0.0012D;
		double motionZ = (world.rand.nextDouble() - 0.5D) * 0.00018D;

		spawnSmokeParticle(baseX, baseY, baseZ, motionX, motionY, motionZ);
	}

	@Override
	protected AxisAlignedBB getAuraArea() {
		return IncenseEffects.getAuraArea(pos, BURNER_AURA_RADIUS);
	}

	public int getAuraRadius() {
		return BURNER_AURA_RADIUS;
	}

	@Override
	protected int getSerenityAmplifier() {
		return 1; // Serenity II, hunger drains at 50%
	}
}
