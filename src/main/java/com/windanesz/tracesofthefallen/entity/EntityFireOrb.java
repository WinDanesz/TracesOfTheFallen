package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityFireOrb extends EntityThrowable {

	public EntityFireOrb(World worldIn) {
		super(worldIn);
	}

	public EntityFireOrb(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
	}

	public EntityFireOrb(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected float getGravityVelocity() {
		return 0.01F;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.world.isRemote && this.ticksExisted > 100) {
			this.setDead();
		}
		if (this.world.isRemote) {
			for (int i = 0; i < 3; i++) {
				this.world.spawnParticle(EnumParticleTypes.FLAME,
						this.posX + (this.rand.nextDouble() - 0.5D) * 0.3D,
						this.posY + (this.rand.nextDouble() - 0.5D) * 0.3D,
						this.posZ + (this.rand.nextDouble() - 0.5D) * 0.3D,
						0.0D, 0.0D, 0.0D);
			}
			this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
					this.posX + (this.rand.nextDouble() - 0.5D) * 0.3D,
					this.posY + (this.rand.nextDouble() - 0.5D) * 0.3D,
					this.posZ + (this.rand.nextDouble() - 0.5D) * 0.3D,
					0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.entityHit != null) {
			if (result.entityHit == this.getThrower() || result.entityHit instanceof EntityGoblin) {
				return;
			}
			result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()).setFireDamage(), (float) Settings.miscSettings.shamanFireOrbDamage);
			result.entityHit.setFire(Settings.miscSettings.shamanFireOrbIgnitionDuration);
		}
		if (!this.world.isRemote) {
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY, this.posZ, 20, 0.3D, 0.3D, 0.3D, 0.08D);
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY, this.posZ, 8, 0.3D, 0.3D, 0.3D, 0.0D);
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY, this.posZ, 5, 0.2D, 0.2D, 0.2D, 0.02D);
			}
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 0.5F, 2.6F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.8F);
			this.setDead();
		}
	}

	@Override
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}
}
