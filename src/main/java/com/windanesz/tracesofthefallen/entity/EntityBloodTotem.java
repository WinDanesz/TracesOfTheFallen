package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class EntityBloodTotem extends Entity {

	private float totemHealth = 10.0F;

	public EntityBloodTotem(World worldIn) {
		super(worldIn);
		this.setSize(0.6F, 0.8F);
	}

	public EntityBloodTotem(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y, z);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		}
		if (!this.world.isRemote) {
			this.totemHealth -= amount;
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 0.8F, 1.2F);
			if (this.totemHealth <= 0) {
				this.destroyTotem();
			} else if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + 0.4D, this.posZ, 5, 0.2D, 0.3D, 0.2D, 0.0D);
			}
		}
		return true;
	}

	private void destroyTotem() {
		if (!this.world.isRemote) {
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + 0.4D, this.posZ, 20, 0.3D, 0.4D, 0.3D, 0.0D);
			}
			this.setDead();
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.04D;
		}
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.8D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.8D;
		if (this.onGround) {
			this.motionX = 0;
			this.motionZ = 0;
		}

		if (this.world.isRemote) {
			if (this.rand.nextInt(3) == 0) {
				double d0 = this.posX + (this.rand.nextDouble() - 0.5D) * 0.5D;
				double d1 = this.posY + this.rand.nextDouble() * 0.8D;
				double d2 = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.5D;
				this.world.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			}
			if (this.rand.nextInt(18) == 0) {
				double d0 = this.posX + (this.rand.nextDouble() - 0.5D) * 0.4D;
				double d1 = this.posY + 0.2D + this.rand.nextDouble() * 0.5D;
				double d2 = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.4D;
				TracesOfTheFallen.proxy.spawnBloodDropParticle(this.world, d0, d1, d2, (this.rand.nextDouble() - 0.5D) * 0.02D, -0.05D, (this.rand.nextDouble() - 0.5D) * 0.02D);
			}
		} else {
			if (this.ticksExisted % 20 == 0) {
				List<EntityGoblin> nearbyGoblins = this.world.getEntitiesWithinAABB(
						EntityGoblin.class,
						this.getEntityBoundingBox().grow(15.0D, 15.0D, 15.0D)
				);
				for (EntityGoblin goblin : nearbyGoblins) {
					if (goblin.isEntityAlive() && this.getDistanceSq(goblin) <= 225.0D) {
						goblin.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 80, 0));
						goblin.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 80, 0));
					}
				}
			}
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("TotemHealth")) {
			this.totemHealth = compound.getFloat("TotemHealth");
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setFloat("TotemHealth", this.totemHealth);
	}
}
