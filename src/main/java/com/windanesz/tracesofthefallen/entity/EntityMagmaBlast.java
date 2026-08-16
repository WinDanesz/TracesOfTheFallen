package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityMagmaBlast extends EntityThrowable {

	public EntityMagmaBlast(World worldIn) {
		super(worldIn);
		this.setSize(0.75F, 0.75F);
	}

	public EntityMagmaBlast(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
		this.setSize(0.75F, 0.75F);
	}

	public EntityMagmaBlast(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.setSize(0.75F, 0.75F);
	}

	@Override
	protected float getGravityVelocity() {
		return 0.04F;
	}

	public void launchLobTo(EntityLivingBase target) {
		double dx = target.posX - this.posX;
		double dz = target.posZ - this.posZ;
		double dy = (target.getEntityBoundingBox().minY + target.height * 0.25D) - this.posY;
		double horizDist = MathHelper.sqrt(dx * dx + dz * dz);
		int flightTime = Math.max(16, (int) (horizDist * 1.4D));
		double g = (double) this.getGravityVelocity();
		this.motionX = dx / flightTime;
		this.motionZ = dz / flightTime;
		this.motionY = (dy / flightTime) + 0.5D * g * (flightTime - 1);
		this.velocityChanged = true;
	}

	@Override
	public void onUpdate() {
		double preX = this.motionX;
		double preY = this.motionY;
		double preZ = this.motionZ;
		super.onUpdate();
		if (!this.world.isRemote && this.ticksExisted > 200) {
			this.setDead();
			return;
		}
		if (!this.isInWater() && !this.inGround && !this.isDead) {
			// Counteract EntityThrowable's 0.99 horizontal drag and default gravity subtraction,
			// keeping horizontal motion constant and vertical acceleration purely linear (-g) for a true parabolic arc
			this.motionX = preX;
			this.motionY = preY - (double) this.getGravityVelocity();
			this.motionZ = preZ;
		}
		if (this.world.isRemote) {
			for (int i = 0; i < 3; i++) {
				this.world.spawnParticle(EnumParticleTypes.FLAME,
						this.posX + (this.rand.nextDouble() - 0.5D) * 0.6D,
						this.posY + (this.rand.nextDouble() - 0.5D) * 0.6D,
						this.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D,
						0.0D, -0.02D, 0.0D);
			}
			this.world.spawnParticle(EnumParticleTypes.DRIP_LAVA,
					this.posX + (this.rand.nextDouble() - 0.5D) * 0.5D,
					this.posY + (this.rand.nextDouble() - 0.5D) * 0.5D,
					this.posZ + (this.rand.nextDouble() - 0.5D) * 0.5D,
					0.0D, 0.0D, 0.0D);
		} else if (this.world instanceof WorldServer && this.ticksExisted % 2 == 0) {
			((WorldServer) this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY, this.posZ, 2, 0.2D, 0.2D, 0.2D, 0.0D);
			((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 3, 0.3D, 0.3D, 0.3D, 0.02D);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			return;
		}
		if (!this.world.isRemote) {
			double hitX = result.hitVec != null ? result.hitVec.x : this.posX;
			double hitY = result.hitVec != null ? result.hitVec.y : this.posY;
			double hitZ = result.hitVec != null ? result.hitVec.z : this.posZ;
			if (result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos() != null) {
				if (result.sideHit == EnumFacing.UP) {
					hitY = result.getBlockPos().getY() + 1.0D;
				}
			}
			EntityGoblinShaman shamanOwner = this.getThrower() instanceof EntityGoblinShaman ? (EntityGoblinShaman) this.getThrower() : null;
			EntityMagmaPool pool = new EntityMagmaPool(this.world, hitX, hitY, hitZ, shamanOwner);
			pool.rotationYaw = this.rand.nextFloat() * 360.0F;
			this.world.spawnEntity(pool);

			this.world.playSound(null, hitX, hitY, hitZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.LAVA, hitX, hitY, hitZ, 15, 0.5D, 0.5D, 0.5D, 0.05D);
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, hitX, hitY, hitZ, 10, 0.4D, 0.4D, 0.4D, 0.05D);
			}
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
