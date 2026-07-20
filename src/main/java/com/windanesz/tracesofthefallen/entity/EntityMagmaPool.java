package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class EntityMagmaPool extends Entity {
	private static final DataParameter<Integer> TIMER = EntityDataManager.createKey(EntityMagmaPool.class, DataSerializers.VARINT);
	private EntityGoblinShaman owner;

	public EntityMagmaPool(World worldIn) {
		super(worldIn);
		this.setSize(5.5F, 0.5F);
	}

	public EntityMagmaPool(World worldIn, double x, double y, double z, EntityGoblinShaman owner) {
		this(worldIn);
		this.setPosition(x, y, z);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		this.owner = owner;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(TIMER, 0);
	}

	public int getTimer() {
		return this.dataManager.get(TIMER);
	}

	public EntityGoblinShaman getOwner() {
		return this.owner;
	}

	public void setOwner(EntityGoblinShaman owner) {
		this.owner = owner;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.world.isRemote) {
			this.dataManager.set(TIMER, this.ticksExisted);
			if (!this.onGround) {
				this.motionY -= 0.05D;
				this.move(MoverType.SELF, 0.0D, this.motionY, 0.0D);
			} else {
				this.motionY = 0.0D;
			}
		}

		int timer = this.getTimer();

		if (timer <= 10) {
			// Phase 1: Seeping lava, which rumbles for half a second before exploding
			if (timer == 1 && !this.world.isRemote) {
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.8F);
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.6F);
			}
			if (timer > 0 && timer % 3 == 0 && !this.world.isRemote) {
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.3F, 1.8F + this.rand.nextFloat() * 0.2F);
			}
			if (this.world.isRemote) {
				for (int i = 0; i < 4; i++) {
					double px = this.posX + (this.rand.nextDouble() - 0.5D) * 4.5D;
					double py = this.posY + 0.1D + this.rand.nextDouble() * 0.3D;
					double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 4.5D;
					this.world.spawnParticle(EnumParticleTypes.DRIP_LAVA, px, py, pz, 0.0D, 0.0D, 0.0D);
					this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, 0.0D, 0.03D, 0.0D);
				}
			} else if (this.world instanceof WorldServer && timer % 2 == 0) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY + 0.2D, this.posZ, 4, 1.8D, 0.1D, 1.8D, 0.0D);
			}
		}

		if (timer == 10 && !this.world.isRemote) {
			// Phase 2: Exploding half second earlier into fire and lava particles causing AOE damage
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F + this.rand.nextFloat() * 0.2F);
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.2F, 0.6F);
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + 0.5D, this.posZ, 4, 0.6D, 0.2D, 0.6D, 0.0D);
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY + 0.5D, this.posZ, 35, 1.6D, 0.4D, 1.6D, 0.1D);
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 0.5D, this.posZ, 50, 1.8D, 0.5D, 1.8D, 0.08D);
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.5D, this.posZ, 25, 1.7D, 0.5D, 1.7D, 0.05D);
			}
			List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5D, 2.0D, 2.5D));
			for (EntityLivingBase entity : targets) {
				boolean isImmune = entity instanceof EntityGoblinShaman && ((EntityGoblinShaman) entity).getPyromancyMastery() == 3;
				if (entity.isEntityAlive() && !isImmune) {
					if (this.getDistanceSq(entity) <= 20.25D) {
						entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.owner != null ? this.owner : this).setFireDamage().setExplosion(), (float) Settings.miscSettings.shamanMagmaBlastDamage);
						entity.setFire(Settings.miscSettings.shamanMagmaBlastIgnitionDuration);
					}
				}
			}
		}

		if (timer >= 10 && timer < 135) {
			// Phase 3: Flat cubes/shapes of various sizes forming together a large pool of lava (+3 seconds duration)
			if (!this.world.isRemote && timer % 10 == 0) {
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_LAVA_POP, SoundCategory.HOSTILE, 0.8F, 0.8F + this.rand.nextFloat() * 0.4F);
			}
			if (this.world.isRemote && timer % 2 == 0) {
				double px = this.posX + (this.rand.nextDouble() - 0.5D) * 4.8D;
				double py = this.posY + 0.2D + this.rand.nextDouble() * 0.3D;
				double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 4.8D;
				this.world.spawnParticle(EnumParticleTypes.LAVA, px, py, pz, 0.0D, 0.0D, 0.0D);
			} else if (!this.world.isRemote && this.world instanceof WorldServer && timer % 4 == 0) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - 0.5D) * 4.6D, this.posY + 0.3D, this.posZ + (this.rand.nextDouble() - 0.5D) * 4.6D, 4, 0.5D, 0.2D, 0.5D, 0.02D);
			}

			if (!this.world.isRemote) {
				List<EntityLivingBase> contacts = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.3D, 0.5D, 0.3D));
				for (EntityLivingBase entity : contacts) {
					boolean isImmune = entity instanceof EntityGoblinShaman && ((EntityGoblinShaman) entity).getPyromancyMastery() == 3;
					if (entity.isEntityAlive() && !isImmune) {
						entity.attackEntityFrom(DamageSource.LAVA, (float) Settings.miscSettings.shamanMagmaBlastContactDamage);
						entity.setFire(3);
					}
				}
			}
		}

		if (!this.world.isRemote && timer >= 135) {
			this.setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.ticksExisted = compound.getInteger("TicksExisted");
		this.dataManager.set(TIMER, this.ticksExisted);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("TicksExisted", this.ticksExisted);
	}
}
