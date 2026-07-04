package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityJawTrap extends Entity {
	private static final DataParameter<Boolean> SNAPPED = EntityDataManager.createKey(EntityJawTrap.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> TRAPPED_ENTITY_ID = EntityDataManager.createKey(EntityJawTrap.class, DataSerializers.VARINT);

	private float trapHealth = 8.0F;
	private int snappedTicks = 0;

	public EntityJawTrap(World worldIn) {
		super(worldIn);
		this.setSize(0.8F, 0.25F);
	}

	public EntityJawTrap(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y, z);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(SNAPPED, false);
		this.dataManager.register(TRAPPED_ENTITY_ID, -1);
	}

	public boolean isSnapped() {
		return this.dataManager.get(SNAPPED);
	}

	public void setSnapped(boolean snapped) {
		this.dataManager.set(SNAPPED, snapped);
	}

	public int getTrappedEntityId() {
		return this.dataManager.get(TRAPPED_ENTITY_ID);
	}

	public void setTrappedEntityId(int entityId) {
		this.dataManager.set(TRAPPED_ENTITY_ID, entityId);
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
			this.trapHealth -= amount;
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_HURT, SoundCategory.BLOCKS, 0.8F, 1.2F);
			if (this.trapHealth <= 0) {
				this.destroyTrap();
			}
		}
		return true;
	}

	private void destroyTrap() {
		if (!this.world.isRemote) {
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
			this.setDead();
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.world.isRemote && this.ticksExisted == 1) {
			int chunkX = MathHelper.floor(this.posX) >> 4;
			int chunkZ = MathHelper.floor(this.posZ) >> 4;
			AxisAlignedBB chunkBounds = new AxisAlignedBB(chunkX << 4, 0.0D, chunkZ << 4, (chunkX + 1) << 4, 256.0D, (chunkZ + 1) << 4);
			List<EntityJawTrap> traps = this.world.getEntitiesWithinAABB(EntityJawTrap.class, chunkBounds);
			int count = 0;
			for (EntityJawTrap t : traps) {
				if (t != this && !t.isDead) {
					count++;
				}
			}
			if (count >= 3) {
				this.setDead();
				return;
			}
		}

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

		if (!this.world.isRemote) {
			if (!this.isSnapped()) {
				List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.15D, 0.5D, 0.15D));
				for (EntityLivingBase entity : list) {
					if (!(entity instanceof EntityGoblin) && !(entity instanceof EntityPlayer && (((EntityPlayer) entity).isSpectator() || ((EntityPlayer) entity).isCreative()))) {
						this.snapOn(entity);
						break;
					}
				}
			} else {
				this.snappedTicks++;
				Entity entity = this.world.getEntityByID(this.getTrappedEntityId());
				if (entity instanceof EntityLivingBase && !entity.isDead && ((EntityLivingBase) entity).getHealth() > 0 && this.snappedTicks < 200) {
					EntityLivingBase trapped = (EntityLivingBase) entity;

					trapped.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 10, true, false));
					trapped.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 128, true, false));

					double dx = this.posX - trapped.posX;
					double dz = this.posZ - trapped.posZ;
					trapped.motionX = dx * 0.4D;
					trapped.motionZ = dz * 0.4D;

					if (this.snappedTicks % 25 == 0) {
						trapped.attackEntityFrom(DamageSource.CACTUS, 1.5F);
					}
				} else {
					this.destroyTrap();
				}
			}
		}
	}

	private void snapOn(EntityLivingBase target) {
		this.setSnapped(true);
		this.setTrappedEntityId(target.getEntityId());
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0F, 1.5F);
		target.attackEntityFrom(DamageSource.CACTUS, 6.0F);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.setSnapped(compound.getBoolean("Snapped"));
		this.setTrappedEntityId(compound.getInteger("TrappedEntityId"));
		this.trapHealth = compound.getFloat("TrapHealth");
		this.snappedTicks = compound.getInteger("SnappedTicks");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setBoolean("Snapped", this.isSnapped());
		compound.setInteger("TrappedEntityId", this.getTrappedEntityId());
		compound.setFloat("TrapHealth", this.trapHealth);
		compound.setInteger("SnappedTicks", this.snappedTicks);
	}
}
