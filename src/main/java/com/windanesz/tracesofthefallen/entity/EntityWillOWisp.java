package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityWillOWisp extends EntityThrowable {

	private static final DataParameter<Integer> THROWER_ID = EntityDataManager.createKey(EntityWillOWisp.class, DataSerializers.VARINT);
	private EntityLivingBase target;

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(THROWER_ID, -1);
	}

	public EntityWillOWisp(World worldIn) {
		super(worldIn);
	}

	public EntityWillOWisp(World worldIn, EntityLivingBase throwerIn, EntityLivingBase targetIn) {
		super(worldIn, throwerIn);
		this.target = targetIn;
		this.dataManager.set(THROWER_ID, throwerIn.getEntityId());
	}

	public EntityWillOWisp(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected float getGravityVelocity() {
		return 0.0F;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.world.isRemote && this.ticksExisted > 200) {
			this.setDead();
			return;
		}

		if (!this.world.isRemote) {
			// Grace window check for bad aim
			List<EntityLivingBase> nearbyTargets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1.5D, 1.5D, 1.5D));
			for (EntityLivingBase entity : nearbyTargets) {
				if (entity != this.getThrower() && entity.isEntityAlive() && !(entity instanceof EntityGoblin)) {
					if (this.getDistanceSq(entity) <= 3.0D) {
						this.hitTarget(entity);
						return;
					}
				}
			}

			// Slight homing towards target
			if (this.target != null && this.target.isEntityAlive()) {
				double dirX = this.target.posX - this.posX;
				double dirY = (this.target.posY + (double) (this.target.height * 0.5F)) - this.posY;
				double dirZ = this.target.posZ - this.posZ;
				double dist = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
				if (dist > 0.001D) {
					dirX /= dist;
					dirY /= dist;
					dirZ /= dist;

					double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					if (speed < 0.1D) speed = 0.35D;

					double turnRate = 0.08D;
					double newX = this.motionX + (dirX * speed - this.motionX) * turnRate;
					double newY = this.motionY + (dirY * speed - this.motionY) * turnRate;
					double newZ = this.motionZ + (dirZ * speed - this.motionZ) * turnRate;
					double newSpeed = Math.sqrt(newX * newX + newY * newY + newZ * newZ);
					if (newSpeed > 0.0001D) {
						this.motionX = (newX / newSpeed) * speed;
						this.motionY = (newY / newSpeed) * speed;
						this.motionZ = (newZ / newSpeed) * speed;
					}
				}
			} else if (this.ticksExisted % 20 == 0) {
				// Try to find a new target if lost
				List<EntityLivingBase> candidates = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D));
				double closestDist = Double.MAX_VALUE;
				for (EntityLivingBase candidate : candidates) {
					if (candidate != this.getThrower() && candidate.isEntityAlive() && !(candidate instanceof EntityGoblin)) {
						double dSq = this.getDistanceSq(candidate);
						if (dSq < closestDist) {
							closestDist = dSq;
							this.target = candidate;
						}
					}
				}
			}
		} else {
			// Client side: Trail of cyan cinder
			for (int i = 0; i < 3; i++) {
				double mx = (this.rand.nextDouble() - 0.5D) * 0.02D;
				double my = (this.rand.nextDouble() - 0.5D) * 0.02D;
				double mz = (this.rand.nextDouble() - 0.5D) * 0.02D;
				TracesOfTheFallen.proxy.spawnCyanCinderParticle(this.world,
						this.posX + (this.rand.nextDouble() - 0.5D) * 0.25D,
						this.posY + (this.rand.nextDouble() - 0.5D) * 0.25D,
						this.posZ + (this.rand.nextDouble() - 0.5D) * 0.25D,
						mx, my, mz);
			}
			this.world.spawnParticle(EnumParticleTypes.REDSTONE,
					this.posX + (this.rand.nextDouble() - 0.5D) * 0.2D,
					this.posY + (this.rand.nextDouble() - 0.5D) * 0.2D,
					this.posZ + (this.rand.nextDouble() - 0.5D) * 0.2D,
					0.05D, 0.9D, 1.0D);
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (this.world.isRemote || this.isDead) return;
		if (result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos() != null) {
			IBlockState state = this.world.getBlockState(result.getBlockPos());
			Block block = state.getBlock();
			if (block instanceof BlockBush || block instanceof BlockVine || block.isPassable(this.world, result.getBlockPos()) || state.getCollisionBoundingBox(this.world, result.getBlockPos()) == Block.NULL_AABB) {
				return;
			}
		}
		if (result.entityHit != null) {
			if (result.entityHit == this.getThrower() || result.entityHit instanceof EntityGoblin) {
				return;
			}
			if (this.getThrower() instanceof com.windanesz.tracesofthefallen.entity.EntityGoblinShaman && com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells.isAlly((com.windanesz.tracesofthefallen.entity.EntityGoblinShaman) this.getThrower(), result.entityHit)) {
				return;
			}
			if (result.entityHit instanceof EntityLivingBase) {
				this.hitTarget((EntityLivingBase) result.entityHit);
				return;
			}
		}
		if (this.world instanceof WorldServer) {
			for (int i = 0; i < 15; i++) {
				double px = this.posX + (this.rand.nextDouble() - 0.5D) * 0.5D;
				double py = this.posY + (this.rand.nextDouble() - 0.5D) * 0.5D;
				double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.5D;
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, 0.05D, 0.9D, 1.0D, 1.0D);
			}
		}
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 0.6F, 1.8F + this.rand.nextFloat() * 0.4F);
		this.world.setEntityState(this, (byte) 16);
		this.setDead();
	}

	protected void hitTarget(EntityLivingBase hitTarget) {
		if (this.world.isRemote || this.isDead) return;
		float damage = (float) Settings.miscSettings.shamanWillOWispDamage;
		if (hitTarget.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()).setMagicDamage(), damage)) {
			if (this.getThrower() instanceof EntityLivingBase && this.getThrower().isEntityAlive()) {
				this.getThrower().heal(damage * 0.5F);
			}
		}
		if (this.world instanceof WorldServer) {
			for (int i = 0; i < 25; i++) {
				double px = this.posX + (this.rand.nextDouble() - 0.5D) * 0.6D;
				double py = this.posY + (this.rand.nextDouble() - 0.5D) * 0.6D;
				double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D;
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, 0.05D, 0.9D, 1.0D, 1.0D);
			}
		}
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 0.8F, 1.5F + this.rand.nextFloat() * 0.3F);
		this.world.setEntityState(this, (byte) 16);
		this.setDead();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 16 && this.world.isRemote) {
			int throwerId = this.dataManager.get(THROWER_ID);
			for (int i = 0; i < 18; i++) {
				double px = this.posX + (this.rand.nextDouble() - 0.5D) * 0.6D;
				double py = this.posY + (this.rand.nextDouble() - 0.5D) * 0.6D;
				double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D;
				TracesOfTheFallen.proxy.spawnLifestealParticle(this.world, px, py, pz, throwerId);
			}
		} else {
			super.handleStatusUpdate(id);
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
