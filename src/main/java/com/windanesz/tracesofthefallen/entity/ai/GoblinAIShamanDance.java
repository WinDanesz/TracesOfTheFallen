package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.List;

public class GoblinAIShamanDance extends EntityAIBase {
	private final EntityGoblinShaman shaman;
	private int danceTimer;
	private boolean isCircleDance;
	private double centerX, centerY, centerZ;
	private double circleRadius;
	private int orbitDirection;
	private int danceType;
	private BlockPos targetFirePos;

	public GoblinAIShamanDance(EntityGoblinShaman shaman) {
		this.shaman = shaman;
		this.setMutexBits(7); // Block movement, looking, and swimming while dancing
	}

	@Override
	public boolean shouldExecute() {
		if (this.shaman.danceCooldown > 0) {
			return false;
		}
		if (this.shaman.getAttackTarget() != null || this.shaman.isInWater() || !this.shaman.onGround) {
			return false;
		}
		// ~1% chance per tick when idle
		return this.shaman.getRNG().nextInt(100) == 0;
	}

	@Override
	public boolean isInterruptible() {
		return false;
	}

	@Override
	public void startExecuting() {
		BlockPos shamanPos = this.shaman.getPosition();
		BlockPos foundFire = null;
		double closestDistSq = Double.MAX_VALUE;

		for (int x = -8; x <= 8; x++) {
			for (int y = -3; y <= 3; y++) {
				for (int z = -8; z <= 8; z++) {
					BlockPos checkPos = shamanPos.add(x, y, z);
					Block block = this.shaman.world.getBlockState(checkPos).getBlock();
					if (block == Blocks.FIRE || block == Blocks.TORCH) {
						double distSq = shamanPos.distanceSq(checkPos);
						if (distSq < closestDistSq) {
							closestDistSq = distSq;
							foundFire = checkPos;
						}
					}
				}
			}
		}

		this.targetFirePos = foundFire;
		if (this.targetFirePos != null) {
			this.danceType = this.shaman.getRNG().nextInt(3) + 1; // 1: Circle, 2: Shake, 3: Fire Throw
		} else {
			this.danceType = this.shaman.getRNG().nextBoolean() ? 1 : 2;
		}

		if (this.danceType == 3) {
			this.danceTimer = 120; // 6 seconds (3 fire charge throws)
		} else if (this.danceType == 2) {
			this.danceTimer = 360 + this.shaman.getRNG().nextInt(60);
		} else {
			this.danceTimer = 240 + this.shaman.getRNG().nextInt(120);
		}

		this.shaman.setDanceType(this.danceType);
		this.isCircleDance = (this.danceType == 1);

		if (this.isCircleDance) {
			this.orbitDirection = this.shaman.getRNG().nextBoolean() ? 1 : -1; // Pick consistent direction

			if (foundFire != null) {
				this.centerX = foundFire.getX() + 0.5D;
				this.centerY = foundFire.getY();
				this.centerZ = foundFire.getZ() + 0.5D;
				double distToCenter = Math.sqrt(this.shaman.getDistanceSq(this.centerX, this.centerY, this.centerZ));
				this.circleRadius = Math.max(1.8D, Math.min(distToCenter, 3.5D));
			} else {
				this.circleRadius = 1.8D + this.shaman.getRNG().nextDouble() * 0.7D; // radius ~2.0 blocks
				float radYaw = (float) Math.toRadians(this.shaman.rotationYaw);
				this.centerX = this.shaman.posX - Math.sin(radYaw) * this.circleRadius;
				this.centerY = this.shaman.posY;
				this.centerZ = this.shaman.posZ + Math.cos(radYaw) * this.circleRadius;
			}
		} else {
			this.shaman.getNavigator().clearPath();
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.danceTimer > 0 && this.shaman.getAttackTarget() == null && !this.shaman.isInWater();
	}

	@Override
	public void updateTask() {
		this.danceTimer--;

		if (this.isCircleDance) {
			if (this.danceTimer % 10 == 0 || this.shaman.getNavigator().noPath()) {
				double actualAngle = Math.atan2(this.shaman.posZ - this.centerZ, this.shaman.posX - this.centerX);
				double targetAngle = actualAngle + this.orbitDirection * 1.2D; // ~70 degrees ahead along perimeter
				double targetX = this.centerX + Math.cos(targetAngle) * this.circleRadius;
				double targetZ = this.centerZ + Math.sin(targetAngle) * this.circleRadius;
				this.shaman.getNavigator().tryMoveToXYZ(targetX, this.centerY, targetZ, 1.25D);
			}
			this.shaman.getLookHelper().setLookPosition(this.centerX, this.centerY + (double) this.shaman.getEyeHeight(), this.centerZ, 30.0F, 30.0F);
		} else if (this.danceType == 3 && this.targetFirePos != null) {
			this.shaman.getLookHelper().setLookPosition(this.targetFirePos.getX() + 0.5D, this.targetFirePos.getY() + 0.5D, this.targetFirePos.getZ() + 0.5D, 30.0F, 30.0F);
			if (!this.shaman.world.isRemote && (this.danceTimer % 40 == 20) && this.shaman.world instanceof WorldServer) {
				WorldServer worldServer = (WorldServer) this.shaman.world;
				double startX = this.shaman.posX + this.shaman.getLookVec().x * 0.5D;
				double startY = this.shaman.posY + (double) this.shaman.getEyeHeight() - 0.2D;
				double startZ = this.shaman.posZ + this.shaman.getLookVec().z * 0.5D;
				double endX = this.targetFirePos.getX() + 0.5D;
				double endY = this.targetFirePos.getY() + 0.5D;
				double endZ = this.targetFirePos.getZ() + 0.5D;

				for (int i = 0; i <= 10; i++) {
					double f = i / 10.0D;
					double pX = startX + (endX - startX) * f;
					double pY = startY + (endY - startY) * f;
					double pZ = startZ + (endZ - startZ) * f;
					worldServer.spawnParticle(EnumParticleTypes.FLAME, pX, pY, pZ, 2, 0.05D, 0.05D, 0.05D, 0.01D);
				}
				worldServer.spawnParticle(EnumParticleTypes.LAVA, endX, endY, endZ, 4, 0.2D, 0.2D, 0.2D, 0.0D);
				this.shaman.world.playSound(null, this.shaman.posX, this.shaman.posY, this.shaman.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0F, (this.shaman.getRNG().nextFloat() - this.shaman.getRNG().nextFloat()) * 0.2F + 1.0F);
			}
		}

		// Spawn tribal dance particles
		if (this.shaman.world.isRemote) {
			double d0 = this.shaman.posX + (this.shaman.getRNG().nextDouble() - 0.5D) * 1.2D;
			double d1 = this.shaman.posY + this.shaman.getRNG().nextDouble() * 1.5D;
			double d2 = this.shaman.posZ + (this.shaman.getRNG().nextDouble() - 0.5D) * 1.2D;
			
			if (this.shaman.getRNG().nextBoolean()) {
				this.shaman.world.spawnParticle(EnumParticleTypes.NOTE, d0, d1 + 0.5D, d2, this.shaman.getRNG().nextDouble() / 24.0D, 0.0D, 0.0D);
			} else {
				this.shaman.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, d0, d1, d2, 0.0D, 0.05D, 0.0D);
			}

			if (this.isCircleDance && this.shaman.getRNG().nextInt(3) == 0) {
				this.shaman.world.spawnParticle(EnumParticleTypes.FLAME,
						this.centerX + (this.shaman.getRNG().nextDouble() - 0.5D) * 0.5D,
						this.centerY + 0.2D + this.shaman.getRNG().nextDouble() * 0.5D,
						this.centerZ + (this.shaman.getRNG().nextDouble() - 0.5D) * 0.5D,
						0.0D, 0.02D, 0.0D);
			}
		} else {
			// Every 20 ticks, bless nearby friendly goblins
			if (this.danceTimer % 20 == 0) {
				List<EntityGoblin> nearbyGoblins = this.shaman.world.getEntitiesWithinAABB(
						EntityGoblin.class,
						this.shaman.getEntityBoundingBox().grow(10.0D, 4.0D, 10.0D)
				);
				for (EntityGoblin goblin : nearbyGoblins) {
					if (goblin.isEntityAlive() && goblin != this.shaman) {
						goblin.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0));
					}
				}
			}
		}
	}

	@Override
	public void resetTask() {
		this.shaman.setDancing(false);
		this.shaman.danceCooldown = 200 + this.shaman.getRNG().nextInt(100); // 10 to 15 seconds cooldown
	}
}
