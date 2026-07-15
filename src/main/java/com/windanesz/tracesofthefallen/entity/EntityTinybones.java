package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityTinybones extends EntityGoblinSkeleton {

	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/tinybones");
	protected static final DataParameter<Integer> FEEDING_TARGET_ID = EntityDataManager.createKey(EntityTinybones.class, DataSerializers.VARINT);

	public EntityTinybones(World worldIn) {
		super(worldIn);
		this.setSize(0.55F, 1.1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(FEEDING_TARGET_ID, -1);
	}

	public void setFeedingTargetId(int id) {
		this.dataManager.set(FEEDING_TARGET_ID, id);
	}

	public int getFeedingTargetId() {
		return this.dataManager.get(FEEDING_TARGET_ID);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.tinybonesMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.tinybonesAttackDamage);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.222D);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new AITinybonesLifeLeech(this, 1.25D, 1.5F));
		this.tasks.addTask(3, new AISkeletonFollowOwner(this, 1.1D, 5.0F, 2.0F));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new AISkeletonOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new AISkeletonOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(4, new AISkeletonTargetPlayer(this));
	}

	@Override
	protected boolean shouldBurnInDay() {
		return false;
	}

	@Override
	protected void updateMobCombat() {
		int feedingId = this.getFeedingTargetId();
		if (feedingId > 0) {
			Entity targetEntity = this.world.getEntityByID(feedingId);
			if (!(targetEntity instanceof EntityLivingBase) || !targetEntity.isEntityAlive() || !this.canEntityBeSeen(targetEntity)) {
				this.setFeedingTargetId(-1);
				return;
			}
			EntityLivingBase target = (EntityLivingBase) targetEntity;
			double distSq = this.getDistanceSq(target);
			double maxDistSq = 2.25D + target.width * target.width;
			if (distSq > maxDistSq + 1.0D) {
				this.setFeedingTargetId(-1);
				return;
			}

			this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
			this.motionX *= 0.1D;
			this.motionZ *= 0.1D;

			if (this.ticksExisted % 10 == 0) {
				float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
				EntityLivingBase owner = this.getOwner();
				DamageSource source = DamageSource.causeIndirectMagicDamage(null, owner != null ? owner : this);

				int prevResist = target.hurtResistantTime;
				target.hurtResistantTime = Math.min(target.hurtResistantTime, 10);

				double oldMX = target.motionX;
				double oldMY = target.motionY;
				double oldMZ = target.motionZ;

				if (target.attackEntityFrom(source, damage)) {
					this.heal(damage);
					target.motionX = oldMX;
					target.motionY = oldMY;
					target.motionZ = oldMZ;
					target.velocityChanged = false;
					this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EAT, this.getSoundCategory(), 0.6F, 1.4F + this.rand.nextFloat() * 0.4F);
				} else {
					target.hurtResistantTime = prevResist;
				}
			}
		} else {
			EntityLivingBase target = this.getAttackTarget();
			if (target != null && target.isEntityAlive() && this.getEntitySenses().canSee(target)) {
				double distSq = this.getDistanceSq(target);
				double triggerDistSq = 2.25D + target.width * target.width;
				if (distSq <= triggerDistSq) {
					if (this.getNavigator() != null) {
						this.getNavigator().clearPath();
					}
					this.setFeedingTargetId(target.getEntityId());
				}
			}
		}
	}

	@Override
	protected void updateClientCombat() {
		int feedingId = this.getFeedingTargetId();
		if (feedingId > 0 && this.world.isRemote && this.ticksExisted > 60) {
			Entity target = this.world.getEntityByID(feedingId);
			if (target != null && target.isEntityAlive()) {
				double px = target.posX + (this.rand.nextDouble() - 0.5D) * (target.width * 0.8D);
				double py = target.posY + target.height * 0.55D + (this.rand.nextDouble() - 0.5D) * (target.height * 0.4D);
				double pz = target.posZ + (this.rand.nextDouble() - 0.5D) * (target.width * 0.8D);
				TracesOfTheFallen.proxy.spawnLifestealParticle(this.world, px, py, pz, this.getEntityId());
			}
		}
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	public static class AITinybonesLifeLeech extends EntityAIBase {
		private final EntityTinybones tinybones;
		private final double moveSpeed;
		private final float attackDist;
		private int pathRecalcTimer;

		public AITinybonesLifeLeech(EntityTinybones tinybones, double speed, float dist) {
			this.tinybones = tinybones;
			this.moveSpeed = speed;
			this.attackDist = dist;
			this.setMutexBits(3);
		}

		@Override
		public boolean shouldExecute() {
			EntityLivingBase target = this.tinybones.getAttackTarget();
			if (target == null || !target.isEntityAlive()) {
				return false;
			}
			if (this.tinybones.getFeedingTargetId() > 0) {
				return false;
			}
			return true;
		}

		@Override
		public boolean shouldContinueExecuting() {
			EntityLivingBase target = this.tinybones.getAttackTarget();
			if (target == null || !target.isEntityAlive()) {
				return false;
			}
			if (this.tinybones.getFeedingTargetId() > 0) {
				return false;
			}
			return !this.tinybones.getNavigator().noPath();
		}

		@Override
		public void startExecuting() {
			this.pathRecalcTimer = 0;
		}

		@Override
		public void resetTask() {
			if (this.tinybones.getNavigator() != null && this.tinybones.getFeedingTargetId() <= 0) {
				this.tinybones.getNavigator().clearPath();
			}
		}

		@Override
		public void updateTask() {
			EntityLivingBase target = this.tinybones.getAttackTarget();
			if (target == null) return;

			this.tinybones.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

			double distSq = this.tinybones.getDistanceSq(target);
			double triggerDistSq = (double)(this.attackDist * this.attackDist) + (target.width * target.width);

			if (distSq <= triggerDistSq) {
				if (this.tinybones.getNavigator() != null) {
					this.tinybones.getNavigator().clearPath();
				}
				this.tinybones.setFeedingTargetId(target.getEntityId());
			} else if (--this.pathRecalcTimer <= 0) {
				this.pathRecalcTimer = 5;
				this.tinybones.getNavigator().tryMoveToEntityLiving(target, this.moveSpeed);
			}
		}
	}
}
