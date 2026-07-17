package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.packet.PacketSpawnCrumbs;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityGoblinSkeleton extends EntityMob implements IEntityOwnable {

	protected static final DataParameter<String> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityGoblinSkeleton.class, DataSerializers.STRING);
	protected static final DataParameter<Integer> LUNGE_STATE = EntityDataManager.createKey(EntityGoblinSkeleton.class, DataSerializers.VARINT);
	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/goblin_skeleton");
	private EntityLivingBase cachedOwner;
	private int lifespan = 3600; // 3 minutes lifespan
	public int lungeTicks = 0;
	public int lungeCooldown = 20;
	public boolean hasHitInCurrentLunge = false;
	private int clientPrevLungeState = 0;

	public EntityGoblinSkeleton(World worldIn) {
		super(worldIn);
		this.setSize(0.6F, 1.3F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(OWNER_UNIQUE_ID, "");
		this.dataManager.register(LUNGE_STATE, Integer.valueOf(0));
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.25D, false));
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
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinSkeletonMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinSkeletonAttackDamage);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}

	private float spawnRotationYaw = 0.0F;
	private boolean hasSpawnRotation = false;

	public int getLungeState() {
		return this.dataManager.get(LUNGE_STATE).intValue();
	}

	public void setLungeState(int state) {
		this.dataManager.set(LUNGE_STATE, Integer.valueOf(state));
	}

	@Override
	protected boolean isMovementBlocked() {
		return this.ticksExisted <= 60 || this.getLungeState() != 0 || super.isMovementBlocked();
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (this.getLungeState() != 0 || this.lungeCooldown > 25) {
			this.fallDistance = 0.0F;
			return;
		}
		super.fall(distance, damageMultiplier);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (this.ticksExisted <= 60 || this.getLungeState() != 0) {
			return false;
		}
		boolean flag = super.attackEntityAsMob(entityIn);
		if (flag) {
			this.swingArm(EnumHand.MAIN_HAND);
			if (!this.world.isRemote) {
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_STEP, this.getSoundCategory(), 1.0F, 1.2F);
			}
		}
		return flag;
	}

	protected boolean shouldBurnInDay() {
		return true;
	}

	@Override
	public void onLivingUpdate() {
		if (this.world.isDaytime() && !this.world.isRemote && !this.isChild() && this.shouldBurnInDay()) {
			float f = this.getBrightness();
			if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ))) {
				boolean flag = true;
				ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				if (!itemstack.isEmpty()) {
					if (itemstack.isItemStackDamageable()) {
						itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));
						if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
							this.renderBrokenItemStack(itemstack);
							this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}
					flag = false;
				}
				if (flag) {
					this.setFire(8);
				}
			}
		}

		if (!this.hasSpawnRotation && this.ticksExisted > 0) {
			this.spawnRotationYaw = this.rotationYaw;
			this.hasSpawnRotation = true;
		}

		if (this.ticksExisted <= 60) {
			this.motionX = 0.0D;
			this.motionZ = 0.0D;
			if (this.getNavigator() != null) {
				this.getNavigator().clearPath();
			}
			this.setMoveForward(0.0F);
			this.setMoveStrafing(0.0F);
		}

		super.onLivingUpdate();

		if (this.ticksExisted <= 60 && this.hasSpawnRotation) {
			this.rotationYaw = this.spawnRotationYaw;
			this.prevRotationYaw = this.spawnRotationYaw;
			this.renderYawOffset = this.spawnRotationYaw;
			this.prevRenderYawOffset = this.spawnRotationYaw;
			this.rotationYawHead = this.spawnRotationYaw;
			this.prevRotationYawHead = this.spawnRotationYaw;
			this.rotationPitch = 0.0F;
			this.prevRotationPitch = 0.0F;
		}

		if (!this.world.isRemote) {
			if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}

			if (this.ticksExisted <= 60) {
				this.motionX = 0.0D;
				this.motionZ = 0.0D;
				if (this.world.isRemote) {
					if (this.ticksExisted % 2 == 0) {
						for (int i = 0; i < 3; i++) {
							TracesOfTheFallen.proxy.spawnCrumbsParticle(this.world, this.posX + (this.rand.nextDouble() - 0.5D) * 0.4D, this.posY + 0.1D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.4D, (this.rand.nextDouble() - 0.5D) * 0.1D, 0.05D + this.rand.nextDouble() * 0.05D, (this.rand.nextDouble() - 0.5D) * 0.1D);
						}
					}
					if (this.ticksExisted > 12 && this.ticksExisted <= 45 && this.ticksExisted % 2 == 0) {
						for (int i = 0; i < 2; i++) {
							TracesOfTheFallen.proxy.spawnCrumbsParticle(this.world, this.posX + (this.rand.nextDouble() - 0.5D) * 0.3D, this.posY + 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.3D, (this.rand.nextDouble() - 0.5D) * 0.1D, 0.07D + this.rand.nextDouble() * 0.04D, (this.rand.nextDouble() - 0.5D) * 0.1D);
						}
					}
				} else {
					if (this.ticksExisted % 4 == 0) {
						this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GRAVEL_BREAK, this.getSoundCategory(), 0.8F, 0.8F + this.rand.nextFloat() * 0.4F);
					}
				}
			}

			if (--this.lifespan <= 0 || (this.getOwner() == null && this.ticksExisted > 600)) {
				if (this.world instanceof WorldServer) {
					PacketHandler.net.sendToAllAround(
							new PacketSpawnCrumbs(this.posX, this.posY + 0.6D, this.posZ, 25, 0.3D, 0.5D, 0.3D),
							new NetworkRegistry.TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 64.0D)
					);
				}
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
				this.setDead();
				return;
			}

			if (this.ticksExisted % 15 == 0 && this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.TOWN_AURA, this.posX + (this.rand.nextDouble() - 0.5D) * 0.4D, this.posY + 0.5D + this.rand.nextDouble() * 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}

			this.updateMobCombat();
		} else if (this.ticksExisted <= 60) {
			this.motionX = 0.0D;
			this.motionZ = 0.0D;
		} else if (this.world.isRemote && this.ticksExisted > 60) {
			this.updateClientCombat();
		}
	}

	protected void updateMobCombat() {
		if (this.lungeCooldown > 0) {
			this.lungeCooldown--;
		}
		int state = this.getLungeState();
		if (state != 0 || this.lungeCooldown > 25) {
			this.fallDistance = 0.0F;
		}
		if (state == 1) {
			this.lungeTicks++;
			if (!this.hasHitInCurrentLunge) {
				java.util.List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.2D, 0.3D, 0.2D));
				for (EntityLivingBase target : targets) {
					if (target != this && target != this.getOwner() && !this.isOnSameTeam(target) && target.isEntityAlive()) {
						target.attackEntityFrom(net.minecraft.util.DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
						this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.1F);
						this.hasHitInCurrentLunge = true;
						this.motionX *= 0.3D;
						this.motionZ *= 0.3D;
						break;
					}
				}
			}
			if (this.onGround && this.lungeTicks > 5 || this.lungeTicks > 22) {
				this.setLungeState(2);
				this.lungeTicks = 0;
				this.motionX = 0.0D;
				this.motionZ = 0.0D;
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SMALL_FALL, this.getSoundCategory(), 1.0F, 0.8F);
			}
		} else if (state == 2) {
			this.lungeTicks++;
			this.motionX = 0.0D;
			this.motionZ = 0.0D;
			if (this.getNavigator() != null) {
				this.getNavigator().clearPath();
			}
			if (this.lungeTicks > 30) {
				this.setLungeState(0);
				this.lungeCooldown = 100;
			}
		} else if (state == 0 && this.lungeCooldown == 0 && this.onGround) {
			EntityLivingBase target = this.getAttackTarget();
			if (target != null && target.isEntityAlive() && this.getEntitySenses().canSee(target)) {
				double distSq = this.getDistanceSq(target);
				if (distSq >= 2.25D && distSq <= 25.0D) {
					this.setLungeState(1);
					this.lungeTicks = 0;
					this.hasHitInCurrentLunge = false;
					double dx = target.posX - this.posX;
					double dz = target.posZ - this.posZ;
					double dist = Math.sqrt(dx * dx + dz * dz);
					this.motionX = (dx / dist) * 1.15D;
					this.motionZ = (dz / dist) * 1.15D;
					this.motionY = 0.32D;
					this.rotationYaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
					this.renderYawOffset = this.rotationYaw;
					this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_STEP, this.getSoundCategory(), 1.2F, 1.4F);
				}
			}
		}
	}

	protected void updateClientCombat() {
		int state = this.getLungeState();
		if (state != this.clientPrevLungeState) {
			this.clientPrevLungeState = state;
			this.lungeTicks = 0;
		}
		if (state == 1 || state == 2) {
			this.lungeTicks++;
		}
	}

	@Override
	@Nullable
	public UUID getOwnerId() {
		try {
			String s = this.dataManager.get(OWNER_UNIQUE_ID);
			return s.isEmpty() ? null : UUID.fromString(s);
		} catch (IllegalArgumentException illegalargumentexception) {
			return null;
		}
	}

	public void setOwnerId(@Nullable UUID ownerId) {
		this.dataManager.set(OWNER_UNIQUE_ID, ownerId == null ? "" : ownerId.toString());
	}

	@Override
	@Nullable
	public EntityLivingBase getOwner() {
		try {
			UUID uuid = this.getOwnerId();
			if (uuid == null) return null;
			if (this.cachedOwner != null && !this.cachedOwner.isDead && uuid.equals(this.cachedOwner.getUniqueID())) {
				return this.cachedOwner;
			}
			EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
			if (player != null) {
				this.cachedOwner = player;
				return player;
			}
			for (Entity entity : this.world.loadedEntityList) {
				if (entity instanceof EntityLivingBase && uuid.equals(entity.getUniqueID())) {
					this.cachedOwner = (EntityLivingBase) entity;
					return this.cachedOwner;
				}
			}
			return null;
		} catch (IllegalArgumentException illegalargumentexception) {
			return null;
		}
	}

	public void setOwner(@Nullable EntityLivingBase owner) {
		this.cachedOwner = owner;
		this.setOwnerId(owner == null ? null : owner.getUniqueID());
	}

	public boolean isOwner(Entity entityIn) {
		return entityIn != null && entityIn == this.getOwner();
	}

	@Override
	public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
		if (this.isOwner(entitylivingbaseIn) || entitylivingbaseIn instanceof EntityGoblin || entitylivingbaseIn instanceof EntityGoblinShaman || entitylivingbaseIn instanceof EntityGoblinSkeleton) {
			super.setAttackTarget(null);
			return;
		}
		super.setAttackTarget(entitylivingbaseIn);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		}
		if (source.getTrueSource() instanceof EntityGoblinSkeleton || source.getTrueSource() instanceof EntityGoblinShaman || source.getTrueSource() instanceof EntityGoblin || this.isOwner(source.getTrueSource())) {
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (entityIn instanceof EntityGoblinSkeleton || entityIn instanceof EntityGoblinShaman || entityIn instanceof EntityGoblin || this.isOwner(entityIn)) {
			return true;
		}
		return super.isOnSameTeam(entityIn);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("Lifespan", this.lifespan);
		if (this.getOwnerId() == null) {
			compound.setString("OwnerUUID", "");
		} else {
			compound.setString("OwnerUUID", this.getOwnerId().toString());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("Lifespan")) {
			this.lifespan = compound.getInteger("Lifespan");
		}
		if (compound.hasKey("OwnerUUID", 8)) {
			String s = compound.getString("OwnerUUID");
			if (!s.isEmpty()) {
				this.setOwnerId(UUID.fromString(s));
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SKELETON_DEATH;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	public static class AISkeletonFollowOwner extends EntityAIBase {
		private final EntityGoblinSkeleton skeleton;
		private EntityLivingBase owner;
		private final double followSpeed;
		private final PathNavigate petPathfinder;
		private int timeToRecalcPath;
		float maxDist;
		float minDist;

		public AISkeletonFollowOwner(EntityGoblinSkeleton skeleton, double followSpeed, float minDist, float maxDist) {
			this.skeleton = skeleton;
			this.followSpeed = followSpeed;
			this.petPathfinder = skeleton.getNavigator();
			this.minDist = minDist;
			this.maxDist = maxDist;
			this.setMutexBits(3);
			if (!(skeleton.getNavigator() instanceof PathNavigateGround)) {
				throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
			}
		}

		@Override
		public boolean shouldExecute() {
			EntityLivingBase entitylivingbase = this.skeleton.getOwner();
			if (entitylivingbase == null) {
				return false;
			} else if (this.skeleton.getDistanceSq(entitylivingbase) < (double)(this.minDist * this.minDist)) {
				return false;
			} else {
				this.owner = entitylivingbase;
				return true;
			}
		}

		@Override
		public boolean shouldContinueExecuting() {
			return !this.petPathfinder.noPath() && this.skeleton.getDistanceSq(this.owner) > (double)(this.maxDist * this.maxDist);
		}

		@Override
		public void startExecuting() {
			this.timeToRecalcPath = 0;
		}

		@Override
		public void resetTask() {
			this.owner = null;
			this.petPathfinder.clearPath();
		}

		@Override
		public void updateTask() {
			this.skeleton.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float)this.skeleton.getVerticalFaceSpeed());
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				if (!this.petPathfinder.tryMoveToEntityLiving(this.owner, this.followSpeed)) {
					if (!this.skeleton.getLeashed() && !this.skeleton.isRiding() && this.skeleton.getDistanceSq(this.owner) >= 144.0D) {
						int i = MathHelper.floor(this.owner.posX) - 2;
						int j = MathHelper.floor(this.owner.posZ) - 2;
						int k = MathHelper.floor(this.owner.getEntityBoundingBox().minY);
						for (int l = 0; l <= 4; ++l) {
							for (int i1 = 0; i1 <= 4; ++i1) {
								if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.skeleton.world.getBlockState(new BlockPos(i + l, k - 1, j + i1)).isTopSolid() && this.skeleton.world.isAirBlock(new BlockPos(i + l, k, j + i1)) && this.skeleton.world.isAirBlock(new BlockPos(i + l, k + 1, j + i1))) {
									this.skeleton.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.skeleton.rotationYaw, this.skeleton.rotationPitch);
									this.petPathfinder.clearPath();
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	public static class AISkeletonOwnerHurtByTarget extends EntityAITarget {
		EntityGoblinSkeleton skeleton;
		EntityLivingBase attacker;
		private int timestamp;

		public AISkeletonOwnerHurtByTarget(EntityGoblinSkeleton skeleton) {
			super(skeleton, false);
			this.skeleton = skeleton;
			this.setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			EntityLivingBase owner = this.skeleton.getOwner();
			if (owner == null) {
				return false;
			} else {
				this.attacker = owner.getRevengeTarget();
				int i = owner.getRevengeTimer();
				return i != this.timestamp && this.isSuitableTarget(this.attacker, false);
			}
		}

		@Override
		public void startExecuting() {
			this.taskOwner.setAttackTarget(this.attacker);
			EntityLivingBase owner = this.skeleton.getOwner();
			if (owner != null) {
				this.timestamp = owner.getRevengeTimer();
			}
			super.startExecuting();
		}
	}

	public static class AISkeletonOwnerHurtTarget extends EntityAITarget {
		EntityGoblinSkeleton skeleton;
		EntityLivingBase target;
		private int timestamp;

		public AISkeletonOwnerHurtTarget(EntityGoblinSkeleton skeleton) {
			super(skeleton, false);
			this.skeleton = skeleton;
			this.setMutexBits(1);
		}

		@Override
		public boolean shouldExecute() {
			EntityLivingBase owner = this.skeleton.getOwner();
			if (owner == null) {
				return false;
			} else {
				this.target = owner.getLastAttackedEntity();
				int i = owner.getLastAttackedEntityTime();
				return i != this.timestamp && this.isSuitableTarget(this.target, false);
			}
		}

		@Override
		public void startExecuting() {
			this.taskOwner.setAttackTarget(this.target);
			EntityLivingBase owner = this.skeleton.getOwner();
			if (owner != null) {
				this.timestamp = owner.getLastAttackedEntityTime();
			}
			super.startExecuting();
		}
	}

	public static class AISkeletonTargetPlayer extends EntityAINearestAttackableTarget<EntityPlayer> {
		private final EntityGoblinSkeleton skeleton;

		public AISkeletonTargetPlayer(EntityGoblinSkeleton skeleton) {
			super(skeleton, EntityPlayer.class, true);
			this.skeleton = skeleton;
		}

		@Override
		public boolean shouldExecute() {
			return !(this.skeleton.getOwner() instanceof EntityPlayer) && super.shouldExecute();
		}

		@Override
		protected boolean isSuitableTarget(EntityLivingBase target, boolean ignoreDisabled) {
			if (!super.isSuitableTarget(target, ignoreDisabled)) {
				return false;
			}
			if (target instanceof EntityPlayer) {
				if (((EntityPlayer) target).isCreative() || ((EntityPlayer) target).isSpectator()) {
					return false;
				}
			}
			return !this.skeleton.isOwner(target);
		}
	}
}
