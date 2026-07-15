package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.item.ItemGraveRose;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class EntityFamiliarSpecter extends EntityCreature implements IEntityOwnable {

	protected static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(EntityFamiliarSpecter.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<String> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityFamiliarSpecter.class, DataSerializers.STRING);
	protected static final DataParameter<String> OWNER_NAME = EntityDataManager.createKey(EntityFamiliarSpecter.class, DataSerializers.STRING);
	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/familiar_specter");

	public EntityFamiliarSpecter(World worldIn) {
		super(worldIn);
		this.setSize(0.6F, 1.8F);
		this.moveHelper = new SpecterMoveHelper(this);
		this.setNoGravity(true);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
		this.tasks.addTask(4, new AIAttack(this));
		this.tasks.addTask(5, new AIFollowOwner(this, 1.0D, 5.0F, 3.0F));
		this.tasks.addTask(6, new AIRandomFly(this));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new AIOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new AIOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.specterAttackDamage);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.specterMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(ATTACKING, false);
		this.dataManager.register(OWNER_UNIQUE_ID, "");
		this.dataManager.register(OWNER_NAME, "");
	}

	@Override
	public UUID getOwnerId() {
		try {
			return UUID.fromString(this.dataManager.get(OWNER_UNIQUE_ID));
		} catch (IllegalArgumentException illegalargumentexception) {
			return null;
		}
	}

	public void setOwnerId(@Nullable UUID ownerId) {
		this.dataManager.set(OWNER_UNIQUE_ID, ownerId == null ? "" : ownerId.toString());
	}

	@Nullable
	@Override
	public EntityLivingBase getOwner() {
		try {
			UUID uuid = this.getOwnerId();
			return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException illegalargumentexception) {
			return null;
		}
	}

	public void setAttacking(boolean attacking) {
		this.dataManager.set(ATTACKING, attacking);
	}

	public boolean isAttacking() {
		return this.dataManager.get(ATTACKING);
	}

	public boolean isOwner(Entity entityIn) {
		return entityIn == this.getOwner();
	}

	@Nullable
	public String getOwnerName() {
		return this.dataManager.get(OWNER_NAME);
	}

	public void setOwner(@Nullable EntityPlayer player) {
		this.setOwnerId(player == null ? null : player.getUniqueID());
		this.dataManager.set(OWNER_NAME, player == null ? "" : player.getName());
	}

	@Override
	public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
		if (this.getOwner() != null && entitylivingbaseIn != null && this.getOwner().equals(entitylivingbaseIn)) {
			super.setAttackTarget(null);
			return;
		}
		super.setAttackTarget(entitylivingbaseIn);
	}


	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isOwner(source.getTrueSource())) {
			return false;
		}
		if (source == DamageSource.IN_WALL) {
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		EntityLivingBase owner = this.getOwner();
		return entityIn.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, owner != null ? owner : this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.noClip = true;

		// Despawn if owner no longer holds the rose with this specter's UUID in main or offhand
		EntityLivingBase owner = getOwner();
		if (owner instanceof EntityPlayer && !owner.world.isRemote) {
			EntityPlayer player = (EntityPlayer) owner;
			boolean found = false;
			UUID myUUID = this.getUniqueID();
			// Check main hand
			net.minecraft.item.ItemStack mainHand = player.getHeldItemMainhand();
			if (mainHand != null && mainHand.getItem() instanceof ItemGraveRose && mainHand.getSubCompound("SpecterUUID") != null) {
				String uuidString = mainHand.getSubCompound("SpecterUUID").getString("UUID");
				if (!uuidString.isEmpty() && myUUID.toString().equals(uuidString)) {
					found = true;
				}
			}
			// Check offhand
			if (!found) {
				net.minecraft.item.ItemStack offHand = player.getHeldItemOffhand();
				if (offHand != null && offHand.getItem() != null && offHand.getSubCompound("SpecterUUID") != null) {
					String uuidString = offHand.getSubCompound("SpecterUUID").getString("UUID");
					if (!uuidString.isEmpty() && myUUID.toString().equals(uuidString)) {
						found = true;
					}
				}
			}
			if (!found) {
				this.setDead();
			}
		}
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.world.checkNoEntityCollision(this.getEntityBoundingBox())
				&& this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()
				&& !this.world.containsAnyLiquid(this.getEntityBoundingBox());
	}

	@Override
	public int getTalkInterval() {
		return 160;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_VEX_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_VEX_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_VEX_HURT;
	}

	@Override
	public boolean isEntityUndead() {
		return true;
	}

	@Override
	public float getEyeHeight() {
		return 1.6F;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState landedState, BlockPos pos) {
	}

	static class AIAttack extends EntityAIBase {
		private final EntityFamiliarSpecter parentEntity;
		private int attackCooldown;
		private int dashCooldown;

		public AIAttack(EntityFamiliarSpecter specter) {
			this.parentEntity = specter;
			this.setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			return this.parentEntity.getAttackTarget() != null;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.parentEntity.getAttackTarget() != null && this.parentEntity.getAttackTarget().isEntityAlive();
		}

		@Override
		public void startExecuting() {
			this.parentEntity.setAttacking(true);
			this.attackCooldown = 0;
			this.dashCooldown = 0;
		}

		@Override
		public void resetTask() {
			this.parentEntity.setAttacking(false);
			if (this.parentEntity.getMoveHelper() instanceof SpecterMoveHelper) {
				((SpecterMoveHelper) this.parentEntity.getMoveHelper()).startCooldown(0);
			}
		}

		@Override
		public void updateTask() {
			EntityLivingBase target = this.parentEntity.getAttackTarget();
			if (target == null) return;

			if (this.dashCooldown > 0) {
				--this.dashCooldown;
			} else {
				this.parentEntity.getLookHelper().setLookPositionWithEntity(target, 10.0F, 10.0F);
				this.parentEntity.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1.0D);

				if (this.attackCooldown > 0) {
					--this.attackCooldown;
				}

				double distanceSq = this.parentEntity.getDistanceSq(target);
				if (distanceSq < 4.0D && this.attackCooldown <= 0) {
					this.parentEntity.attackEntityAsMob(target);
					this.attackCooldown = 20;
					this.dashCooldown = 20;

					Random random = this.parentEntity.getRNG();
					Vec3d targetLook = target.getLookVec();

					Vec3d forward = new Vec3d(targetLook.x, 0, targetLook.z).normalize();
					Vec3d right = new Vec3d(-targetLook.z, 0, targetLook.x).normalize();

					Vec3d[] directions = {
							forward,
							forward.scale(-1),
							right,
							right.scale(-1)
					};

					Vec3d chosenDir = directions[random.nextInt(directions.length)];

					double distance = 3.0; //+ random.nextDouble() * 1.0; // 2-3 blocks from player

					double destX = target.posX + chosenDir.x * distance;
					double destY = target.posY + 1.0;
					double destZ = target.posZ + chosenDir.z * distance;

					double moveVecX = destX - this.parentEntity.posX;
					double moveVecY = destY - this.parentEntity.posY;
					double moveVecZ = destZ - this.parentEntity.posZ;
					double moveLen = MathHelper.sqrt(moveVecX * moveVecX + moveVecY * moveVecY + moveVecZ * moveVecZ);

					if (moveLen > 0) {
						double dashSpeed = 1.8;
						this.parentEntity.motionX = (moveVecX / moveLen) * dashSpeed;
						this.parentEntity.motionY = (moveVecY / moveLen) * dashSpeed * 0.1;
						this.parentEntity.motionZ = (moveVecZ / moveLen) * dashSpeed;

						if (this.parentEntity.getMoveHelper() instanceof SpecterMoveHelper) {
							((SpecterMoveHelper) this.parentEntity.getMoveHelper()).startCooldown(20);
						}
					}
				}
			}
		}
	}

	static class AIRandomFly extends EntityAIBase {
		private final EntityFamiliarSpecter parentEntity;

		public AIRandomFly(EntityFamiliarSpecter specter) {
			this.parentEntity = specter;
			this.setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			// Add a random chance to execute, so it doesn't move constantly
			if (this.parentEntity.getRNG().nextInt(80) != 0) {
				return false;
			}

			EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();
			if (!entitymovehelper.isUpdating()) {
				return true;
			} else {
				double d0 = entitymovehelper.getX() - this.parentEntity.posX;
				double d1 = entitymovehelper.getY() - this.parentEntity.posY;
				double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				return d3 < 1.0D || d3 > 3600.0D;
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
			Random random = this.parentEntity.getRNG();
			double d0 = this.parentEntity.posX + (double) ((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
			double d1 = this.parentEntity.posY + (double) ((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
			double d2 = this.parentEntity.posZ + (double) ((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
			this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
		}
	}

	static class SpecterMoveHelper extends EntityMoveHelper {
		private final EntityFamiliarSpecter parentEntity;
		private int cooldown;

		public SpecterMoveHelper(EntityFamiliarSpecter specter) {
			super(specter);
			this.parentEntity = specter;
		}

		public void startCooldown(int ticks) {
			this.cooldown = ticks;
		}

		@Override
		public void onUpdateMoveHelper() {
			if (this.cooldown > 0) {
				--this.cooldown;
				return;
			}

			if (this.action == Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				if (d3 < 0.1D) {
					this.action = Action.WAIT;
					this.parentEntity.motionX = 0.0D;
					this.parentEntity.motionY = 0.0D;
					this.parentEntity.motionZ = 0.0D;
					return;
				}

				d3 = (double) MathHelper.sqrt(d3);

				d0 /= d3;
				d1 /= d3;
				d2 /= d3;

				float speed = (float) (this.speed * this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());

				this.parentEntity.motionX = d0 * speed;
				this.parentEntity.motionY = d1 * speed;
				this.parentEntity.motionZ = d2 * speed;

				float f = (float) (MathHelper.atan2(this.parentEntity.motionZ, this.parentEntity.motionX) * (180D / Math.PI)) - 90.0F;
				this.parentEntity.rotationYaw = this.limitAngle(this.parentEntity.rotationYaw, f, 90.0F);
				this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;

			} else {
				this.parentEntity.motionX *= 0.5D;
				this.parentEntity.motionY *= 0.5D;
				this.parentEntity.motionZ *= 0.5D;
			}
		}
	}

	static class AIFollowOwner extends EntityAIBase {
		private final EntityFamiliarSpecter specter;
		private EntityLivingBase owner;
		private final World world;
		private final double followSpeed;
		private final float minDist;
		private final float maxDist;
		private int timeToRecalcPath;

		public AIFollowOwner(EntityFamiliarSpecter specter, double speed, float min, float max) {
			this.specter = specter;
			this.world = specter.world;
			this.followSpeed = speed;
			this.minDist = min;
			this.maxDist = max;
			this.setMutexBits(3);
		}

		public boolean shouldExecute() {
			EntityLivingBase owner = this.specter.getOwner();

			if (owner == null) {
				return false;
			} else if (this.specter.getDistanceSq(owner) < (double) (this.minDist * this.minDist)) {
				return false;
			} else {
				this.owner = owner;
				return true;
			}
		}

		public boolean shouldContinueExecuting() {
			return this.specter.getDistanceSq(this.owner) > (double) (this.maxDist * this.maxDist);
		}

		public void startExecuting() {
			this.timeToRecalcPath = 0;
		}

		public void resetTask() {
			this.owner = null;
			this.specter.getMoveHelper().action = EntityMoveHelper.Action.WAIT;
		}

		public void updateTask() {
			this.specter.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.specter.getVerticalFaceSpeed());

			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				this.specter.getMoveHelper().setMoveTo(this.owner.posX, this.owner.posY + 1.5, this.owner.posZ, this.followSpeed);
			}
		}
	}

	static class AIOwnerHurtByTarget extends EntityAITarget {
		EntityFamiliarSpecter specter;
		EntityLivingBase attacker;
		private int timestamp;

		public AIOwnerHurtByTarget(EntityFamiliarSpecter specter) {
			super(specter, false);
			this.specter = specter;
			this.setMutexBits(1);
		}

		public boolean shouldExecute() {
			EntityLivingBase owner = this.specter.getOwner();
			if (owner == null) {
				return false;
			} else {
				this.attacker = owner.getRevengeTarget();
				int i = owner.getRevengeTimer();
				return i != this.timestamp && this.isSuitableTarget(this.attacker, false);
			}
		}

		public void startExecuting() {
			this.taskOwner.setAttackTarget(this.attacker);
			EntityLivingBase owner = this.specter.getOwner();
			if (owner != null) {
				this.timestamp = owner.getRevengeTimer();
			}
			super.startExecuting();
		}
	}

	static class AIOwnerHurtTarget extends EntityAITarget {
		EntityFamiliarSpecter specter;
		EntityLivingBase target;
		private int timestamp;

		public AIOwnerHurtTarget(EntityFamiliarSpecter specter) {
			super(specter, false);
			this.specter = specter;
			this.setMutexBits(1);
		}

		public boolean shouldExecute() {
			EntityLivingBase owner = this.specter.getOwner();
			if (owner == null) {
				return false;
			} else {
				this.target = owner.getLastAttackedEntity();
				int i = owner.getLastAttackedEntityTime();
				return i != this.timestamp && this.isSuitableTarget(this.target, false);
			}
		}

		public void startExecuting() {
			this.taskOwner.setAttackTarget(this.target);
			EntityLivingBase owner = this.specter.getOwner();
			if (owner != null) {
				this.timestamp = owner.getLastAttackedEntityTime();
			}
			super.startExecuting();
		}
	}
}

