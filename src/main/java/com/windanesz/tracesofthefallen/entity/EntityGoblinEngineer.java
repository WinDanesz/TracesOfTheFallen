package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIEngineerBreachWall;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIEngineerEscape;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityGoblinEngineer extends EntityGoblin {
	private static final DataParameter<Boolean> POINTING = EntityDataManager.createKey(EntityGoblinEngineer.class, DataSerializers.BOOLEAN);

	private int daggerCooldown = 0;
	private boolean spawnedDaggers = false;

	public EntityGoblinEngineer(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(POINTING, false);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new GoblinAIEngineerEscape(this));
		this.tasks.addTask(2, new GoblinAIEngineerBreachWall(this));
	}

	public boolean isPointing() {
		return this.dataManager.get(POINTING);
	}

	public void setPointing(boolean pointing) {
		this.dataManager.set(POINTING, pointing);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinEngineerMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinEngineerAttackDamage);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ModItems.fetid_dagger, 4));
		this.spawnedDaggers = true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("SpawnedDaggers", this.spawnedDaggers);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("SpawnedDaggers")) {
			this.spawnedDaggers = compound.getBoolean("SpawnedDaggers");
		}
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		if (!this.world.isRemote) {
			EntityWroughtBomb bomb = new EntityWroughtBomb(this.world, this.posX, this.posY, this.posZ, this);
			bomb.setFuse(-1);
			this.world.spawnEntity(bomb);
			bomb.startRiding(this);
		}
		return livingdata;
	}


	@Override
	public double getMountedYOffset() {
		if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof EntityWroughtBomb) {
			return 0.45D;
		}
		return super.getMountedYOffset();
	}

	public EntityWroughtBomb getMountedBomb() {
		for (Entity passenger : this.getPassengers()) {
			if (passenger instanceof EntityWroughtBomb) {
				return (EntityWroughtBomb) passenger;
			}
		}
		return null;
	}

	public boolean hasMountedBomb() {
		return this.getMountedBomb() != null;
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		if ((slotIn == EntityEquipmentSlot.MAINHAND || slotIn == EntityEquipmentSlot.OFFHAND) && this.hasMountedBomb()) {
			return ItemStack.EMPTY;
		}
		return super.getItemStackFromSlot(slotIn);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if (!this.world.isRemote && !this.spawnedDaggers && this.ticksExisted < 20) {
			if (super.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()) {
				this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
			}
			if (super.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).isEmpty()) {
				this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ModItems.fetid_dagger, 4));
			}
			this.spawnedDaggers = true;
		}

		if (!this.world.isRemote && this.ticksExisted % 20 == 0) {
			double followRange = this.hasMountedBomb() ? Settings.goblinSettings.goblinEngineerBombFollowRange : 32.0D;
			if (this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue() != followRange) {
				this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(followRange);
			}
		}

		if (!this.world.isRemote) {
			if (this.hasMountedBomb()) {
				EntityLivingBase target = this.getAttackTarget();
				if (target == null || !target.isEntityAlive() || this.isOwner(target)) {
					EntityPlayer player = this.world.getClosestPlayerToEntity(this, 8.0D);
					if (player != null && !player.isCreative() && !player.isSpectator() && !this.isOwner(player)) {
						target = player;
					}
				}
				if (target != null && target.isEntityAlive() && this.getDistanceSq(target) <= 64.0D && !this.isOwner(target)) {
					if (this.isTargetReachable(target)) {
						EntityWroughtBomb mountedBomb = this.getMountedBomb();
						if (mountedBomb != null) {
							mountedBomb.dismountRidingEntity();
							mountedBomb.setPosition(this.posX, this.posY, this.posZ);
						}
					}
				}
			}

			if (this.daggerCooldown > 0) {
				this.daggerCooldown--;
			}
			if (!this.hasMountedBomb()) {
				EntityLivingBase target = this.getAttackTarget();
				if (target != null && target.isEntityAlive() && this.daggerCooldown == 0) {
					double distSq = this.getDistanceSq(target);
					if (distSq > 9.0D && distSq < 256.0D && this.getEntitySenses().canSee(target)) {
						ItemStack throwStack = super.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
						EntityEquipmentSlot throwSlot = EntityEquipmentSlot.OFFHAND;
						if (throwStack.isEmpty() || throwStack.getItem() != ModItems.fetid_dagger) {
							throwStack = super.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
							throwSlot = EntityEquipmentSlot.MAINHAND;
						}

						if (!throwStack.isEmpty() && throwStack.getItem() == ModItems.fetid_dagger) {
							this.throwDaggerAt(target, throwStack, throwSlot);
							this.daggerCooldown = 30 + this.rand.nextInt(20);
						}
					}
				}
			}
		}
	}

	private void throwDaggerAt(EntityLivingBase target, ItemStack throwStack, EntityEquipmentSlot throwSlot) {
		EntityFetidDagger dagger = new EntityFetidDagger(this.world, this, throwStack);
		double d0 = target.posX - this.posX;
		double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - dagger.posY;
		double d2 = target.posZ - this.posZ;
		double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
		dagger.shoot(d0, d1 + d3 * 0.2D, d2, 1.5F, 2.0F);
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 0.5F, 0.4F / (this.rand.nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(dagger);

		throwStack.shrink(1);
		if (throwStack.isEmpty()) {
			this.setItemStackToSlot(throwSlot, ItemStack.EMPTY);
		}
		this.swingArm(throwSlot == EntityEquipmentSlot.OFFHAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
	}

	private boolean isTargetReachable(EntityLivingBase target) {
		Path path = this.getNavigator().getPathToEntityLiving(target);

		if (path != null && path.getFinalPathPoint() != null) {
			PathPoint endPoint = path.getFinalPathPoint();

			double distSq = target.getDistanceSq(
					endPoint.x + 0.5D,
					endPoint.y,
					endPoint.z + 0.5D
			);

			if (distSq > 4.0D) {
				return false;
			}

			double straightDist = this.getDistance(target);

			return path.getCurrentPathLength() <= straightDist + 6.0D;
		}

		return false;
	}
}
