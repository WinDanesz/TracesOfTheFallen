package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class EntitySeekingOrb extends EntityThrowable {

	private static final DataParameter<Integer> THROWER_ID = EntityDataManager.createKey(EntitySeekingOrb.class, DataSerializers.VARINT);
	private EntityLivingBase target;

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(THROWER_ID, -1);
	}

	public EntitySeekingOrb(World worldIn) {
		super(worldIn);
	}

	public EntitySeekingOrb(World worldIn, EntityLivingBase throwerIn, EntityLivingBase targetIn) {
		super(worldIn, throwerIn);
		this.target = targetIn;
		this.dataManager.set(THROWER_ID, throwerIn.getEntityId());
	}

	public EntitySeekingOrb(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected float getGravityVelocity() {
		return 0.0F;
	}

	@Override
	public void onUpdate() {
		if (this.inGround) {
			this.inGround = false;
		}

		super.onUpdate();

		if (!this.world.isRemote && this.ticksExisted > 300) {
			this.setDead();
			return;
		}

		if (!this.world.isRemote) {
			// Find new target if current target is invalid
			if (this.target == null || !this.target.isEntityAlive() || this.rand.nextInt(40) == 0) {
				List<EntityLivingBase> candidates = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(16.0D, 16.0D, 16.0D));
				double closestSq = Double.MAX_VALUE;
				EntityLivingBase best = null;
				for (EntityLivingBase e : candidates) {
					if (e != this.getThrower() && e.isEntityAlive() && !this.isOwner(e) && !(e instanceof EntityGoblin)) {
						double dSq = this.getDistanceSq(e);
						if (dSq < closestSq) {
							closestSq = dSq;
							best = e;
						}
					}
				}
				if (best != null) {
					this.target = best;
				}
			}

			// Very slow homing towards target
			if (this.target != null && this.target.isEntityAlive()) {
				double dirX = this.target.posX - this.posX;
				double dirY = (this.target.posY + (double) (this.target.height * 0.5F)) - this.posY;
				double dirZ = this.target.posZ - this.posZ;
				double dist = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
				if (dist > 0.001D) {
					dirX /= dist;
					dirY /= dist;
					dirZ /= dist;
					this.motionX = this.motionX * 0.8D + dirX * 0.01D;
					this.motionY = this.motionY * 0.8D + dirY * 0.01D;
					this.motionZ = this.motionZ * 0.8D + dirZ * 0.01D;
				}
			}

			// Shock nearby enemies periodically
			if (this.ticksExisted % 25 == 0) {
				List<EntityLivingBase> nearbyEnemies = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5D, 2.5D, 2.5D));
				boolean shocked = false;
				for (EntityLivingBase e : nearbyEnemies) {
					if (e != this.getThrower() && e.isEntityAlive() && !this.isOwner(e) && !(e instanceof EntityGoblin)) {
						if (this.getDistanceSq(e) <= 6.25D) {
							e.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), (float) Settings.miscSettings.shamanSeekingOrbDamage);
							this.world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 0.8F, 1.4F + this.rand.nextFloat() * 0.4F);
							if (this.world instanceof WorldServer) {
								((WorldServer) this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX, e.posY + e.height * 0.5D, e.posZ, 15, 0.3D, 0.5D, 0.3D, 0.1D);
							}
							shocked = true;
						}
					}
				}
				if (shocked) {
					this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.3F);
					if (this.world instanceof WorldServer) {
						((WorldServer) this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY, this.posZ, 12, 0.3D, 0.3D, 0.3D, 0.05D);
					}
				}
			}
		}

		if (this.world.isRemote) {
			for (int i = 0; i < 2; i++) {
				this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
						this.posX + (this.rand.nextDouble() - 0.5D) * 0.4D,
						this.posY + (this.rand.nextDouble() - 0.5D) * 0.4D,
						this.posZ + (this.rand.nextDouble() - 0.5D) * 0.4D,
						0.0D, 0.0D, 0.0D);
			}

			// Render faint particle sphere (radius 2.5) to indicate attack distance
			for (int i = 0; i < 4; i++) {
				double u = this.rand.nextDouble();
				double v = this.rand.nextDouble();
				double theta = u * 2.0D * Math.PI;
				double phi = Math.acos(2.0D * v - 1.0D);
				double rx = Math.sin(phi) * Math.cos(theta) * 2.5D;
				double ry = Math.sin(phi) * Math.sin(theta) * 2.5D;
				double rz = Math.cos(phi) * 2.5D;
				this.world.spawnParticle(EnumParticleTypes.TOWN_AURA,
						this.posX + rx,
						this.posY + ry,
						this.posZ + rz,
						0.0D, 0.0D, 0.0D);
			}

			// Render rotating faint magic particles along the 2.5 block perimeter rings at a gentle speed
			double ringAngle = this.ticksExisted * 0.065D;
			for (int i = 0; i < 6; i++) {
				double angle = ringAngle + i * (Math.PI / 3.0D);
				this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
						this.posX + Math.cos(angle) * 2.5D,
						this.posY + Math.sin(this.ticksExisted * 0.04D + i) * 0.6D,
						this.posZ + Math.sin(angle) * 2.5D,
						0.0D, 0.0D, 0.0D);
			}
			double tiltAngle = -this.ticksExisted * 0.055D;
			for (int i = 0; i < 6; i++) {
				double angle = tiltAngle + i * (Math.PI / 3.0D);
				this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
						this.posX + Math.cos(angle) * 2.5D,
						this.posY + Math.sin(angle) * 2.5D,
						this.posZ + Math.cos(this.ticksExisted * 0.03D + i) * 0.6D,
						0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			IBlockState state = this.world.getBlockState(result.getBlockPos());
			Block block = state.getBlock();
			if (block instanceof BlockBush || block instanceof BlockVine || block.isPassable(this.world, result.getBlockPos())) {
				return;
			}
			if (result.sideHit != null) {
				switch (result.sideHit) {
					case UP:
					case DOWN:
						this.motionY = -this.motionY * 0.6D;
						break;
					case NORTH:
					case SOUTH:
						this.motionZ = -this.motionZ * 0.6D;
						break;
					case WEST:
					case EAST:
						this.motionX = -this.motionX * 0.6D;
						break;
				}
			}
			return;
		}

		if (result.entityHit != null) {
			if (result.entityHit == this.getThrower() || this.isOwner(result.entityHit) || result.entityHit instanceof EntityGoblin) {
				return;
			}
			if (!this.world.isRemote) {
				result.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), (float) Settings.miscSettings.shamanSeekingOrbDamage);
				this.world.playSound(null, result.entityHit.posX, result.entityHit.posY, result.entityHit.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0F, 1.2F + this.rand.nextFloat() * 0.4F);
				if (this.world instanceof WorldServer) {
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY, this.posZ, 25, 0.4D, 0.4D, 0.4D, 0.15D);
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY, this.posZ, 15, 0.3D, 0.3D, 0.3D, 0.1D);
				}
				this.setDead();
			}
		}
	}

	public boolean isOwner(Entity entityIn) {
		if (entityIn == null) return false;
		if (entityIn == this.getThrower()) return true;
		if (this.dataManager.get(THROWER_ID) == entityIn.getEntityId()) return true;
		if (this.getThrower() instanceof EntityGoblinShaman) {
			return ShamanSpells.isAlly((EntityGoblinShaman) this.getThrower(), entityIn);
		}
		if (this.getThrower() instanceof EntityGoblin && entityIn instanceof EntityGoblin) {
			return ((EntityGoblin) this.getThrower()).isOwner(entityIn);
		}
		return false;
	}
}
