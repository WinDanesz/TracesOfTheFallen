package com.windanesz.tracesofthefallen.block;

import net.minecraft.util.EnumFacing;

public class TileEntityIncense extends TileEntityBurningIncense {

	private static final int BURN_DURATION_TICKS = 20 * 20;
	private static final double INITIAL_PARTICLE_OFFSET = 0.2D;
	private static final double PARTICLE_STAGE_STEP = 1.0D / 16.0D;
	private int lastBurnStage = -1;

	public TileEntityIncense() {
		super(false);
	}

	@Override
	protected int getInitialBurnTime() {
		return BURN_DURATION_TICKS;
	}

	@Override
	protected boolean getDefaultLitState() {
		return false;
	}

	@Override
	protected void spawnBurningParticle() {
		EnumFacing facing = world.getBlockState(pos).getValue(BlockDecoration.FACING);
		int burnStage = Math.min(getBurnStage(), BlockIncense.MAX_ACTIVE_BURN_STAGE);
		double particleOffset = Math.max(0.0D, INITIAL_PARTICLE_OFFSET - (burnStage * PARTICLE_STAGE_STEP));
		double baseX = pos.getX() + 0.5D + facing.getXOffset() * particleOffset + (world.rand.nextDouble() - 0.5D) * 0.02D;
		double baseY = pos.getY() + 0.12D + world.rand.nextDouble() * 0.03D;
		double baseZ = pos.getZ() + 0.5D + facing.getZOffset() * particleOffset + (world.rand.nextDouble() - 0.5D) * 0.02D;
		double motionX = (world.rand.nextDouble() - 0.5D) * 0.00018D;
		double motionY = 0.0045D + world.rand.nextDouble() * 0.0012D;
		double motionZ = (world.rand.nextDouble() - 0.5D) * 0.00018D;

		spawnSmokeParticle(baseX, baseY, baseZ, motionX, motionY, motionZ);
	}

	@Override
	protected void onBurnProgressUpdated() {
		if (world == null || world.isRemote) {
			return;
		}

		if (!(world.getBlockState(pos).getBlock() instanceof BlockIncense)) {
			return;
		}

		int burnStage = getBurnStage();
		if (burnStage != lastBurnStage) {
			lastBurnStage = burnStage;
			markDirty();
			syncToClient();
		}
	}

	public int getBurnStage() {
		if (isBurnedOut()) {
			return BlockIncense.BURNT_OUT_STAGE;
		}

		if (!isLit()) {
			return 0;
		}

		int totalBurnTime = Math.max(1, getTotalBurnTime());
		int burnedTime = Math.max(0, totalBurnTime - Math.max(0, getRemainingBurnTime()));
		return Math.min(BlockIncense.MAX_ACTIVE_BURN_STAGE, (burnedTime * (BlockIncense.MAX_ACTIVE_BURN_STAGE + 1)) / totalBurnTime);
	}

	@Override
	protected void onBurnFinished() {
		if (world != null && !world.isRemote && world.getBlockState(pos).getBlock() instanceof BlockIncense) {
			setBurnedOut(true);
			lastBurnStage = BlockIncense.BURNT_OUT_STAGE;
		}
	}

	public boolean light() {
		return ignite();
	}
}
