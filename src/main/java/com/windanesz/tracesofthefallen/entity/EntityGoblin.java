package com.windanesz.tracesofthefallen.entity;

import com.google.common.base.Optional;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.ai.*;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityGoblin extends EntityMob implements IEntityOwnable {

	// Goblins have 3 states:
	// 1. hostile - default, attacks player on sight (not owned)
	// 2. neutral - won't initiate combat (not owned)
	// 3. friendly - owned, follows player and fights for player (needs the player to keep holding a minecraft:stick)
	protected static final DataParameter<Boolean> NEUTRAL = EntityDataManager.createKey(EntityGoblin.class, DataSerializers.BOOLEAN);
	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/goblin");
	protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityGoblin.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<Float> TARGET_Y = EntityDataManager.<Float>createKey(EntityGoblin.class, DataSerializers.FLOAT);

	private int ownershipLossTimer = 0;
	private int hostilityTimer = 0;
	private BlockPos bombDeliveryTarget = null;
	private boolean equipmentInitialized = false;

	public EntityGoblin(World worldIn) {
		super(worldIn);
		this.setSize(0.6F, 1F);
		this.stepHeight = 1.0F;
		this.setCanPickUpLoot(true);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new GoblinAIPickupIdol(this));
		this.tasks.addTask(1, new GoblinAIFleeTickingBomb(this));
		this.tasks.addTask(1, new GoblinAIBroodDeliverBomb(this));
		this.tasks.addTask(1, new GoblinAIPickupBomb(this));
		this.tasks.addTask(1, new GoblinAIWaitForEngineer(this));
		this.tasks.addTask(2, new GoblinAIRunBehindTarget(this, 2.0D));
		this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.3D, false));
		this.tasks.addTask(3, new GoblinAIFollowOwner(this, 1.3D, 5.0F, 3.0F));
		this.tasks.addTask(3, new GoblinAIMountMinecrawler(this, 1.2D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new GoblinAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new GoblinAIOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true) {
			@Override
			public boolean shouldExecute() {
				// Always hostile when holding an idol, otherwise follow normal rules
				if (EntityGoblin.this.isHoldingIdol()) {
					return super.shouldExecute();
				}
				return !EntityGoblin.this.hasOwner() && !EntityGoblin.this.isNeutral() && super.shouldExecute();
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
				return !EntityGoblin.this.isOwner(target);
			}
		});
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinBroodMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinBroodAttackDamage);
	}

	public boolean isImmuneToIdol() {
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(NEUTRAL, false);
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
		this.dataManager.register(TARGET_Y, -999.0F);
	}

	public boolean isNeutral() {
		return this.dataManager.get(NEUTRAL);
	}

	public void setNeutral(boolean neutral) {
		this.dataManager.set(NEUTRAL, neutral);
	}

	public boolean isOwner(Entity entityIn) {
		return entityIn != null && entityIn.equals(this.getOwner());
	}

	@Override
	public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
		if (this.isOwner(entitylivingbaseIn)) {
			super.setAttackTarget(null);
			return;
		}
		
		// Play aggro sound when acquiring a new target
		if (entitylivingbaseIn != null && this.getAttackTarget() != entitylivingbaseIn && !this.world.isRemote) {
			this.playSound(ModSounds.GOBLIN_AGGRO, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		}
		
		super.setAttackTarget(entitylivingbaseIn);
	}


	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		}
		if (this.isOwner(source.getTrueSource())) {
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = super.attackEntityAsMob(entityIn);

		if (flag) {
			if (entityIn instanceof EntityLivingBase) {
				ItemStack itemstack = this.getHeldItemMainhand();
				if (!itemstack.isEmpty()) {
					itemstack.getItem().hitEntity(itemstack, (EntityLivingBase) entityIn, this);
				}
			}
			this.swingArm(EnumHand.MAIN_HAND);
		}
		return flag;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);

		// Interaction with stick no longer needed - goblins auto-ally based on active idol
		return super.processInteract(player, hand);
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("EquipmentInitialized", this.equipmentInitialized);

		if (this.getOwnerId() == null) {
			compound.setString("OwnerUUID", "");
		} else {
			compound.setString("OwnerUUID", this.getOwnerId().toString());
		}
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.equipmentInitialized = compound.getBoolean("EquipmentInitialized");
		String s;

		if (compound.hasKey("OwnerUUID", 8)) {
			s = compound.getString("OwnerUUID");
		} else {
			String s1 = compound.getString("Owner");
			s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
		}
		if (!s.isEmpty()) {
			this.setOwnerId(UUID.fromString(s));
		}
	}

	@Override
	public boolean isOnLadder() {
		return super.isOnLadder() || this.getActiveLadderPos() != null;
	}

	public BlockPos getActiveLadderPos() {
		BlockPos pos = new BlockPos(this);
		if (this.world.getBlockState(pos).getBlock() == net.minecraft.init.Blocks.LADDER) return pos;
		for (net.minecraft.util.EnumFacing side : net.minecraft.util.EnumFacing.HORIZONTALS) {
			if (this.world.getBlockState(pos.offset(side)).getBlock() == net.minecraft.init.Blocks.LADDER) {
				return pos.offset(side);
			}
		}
		
		pos = pos.down();
		if (this.world.getBlockState(pos).getBlock() == net.minecraft.init.Blocks.LADDER) return pos;
		for (net.minecraft.util.EnumFacing side : net.minecraft.util.EnumFacing.HORIZONTALS) {
			if (this.world.getBlockState(pos.offset(side)).getBlock() == net.minecraft.init.Blocks.LADDER) {
				return pos.offset(side);
			}
		}
		return null;
	}

	public boolean isWorking() {
		return this.isCarryingBomb() || this.isHoldingIdol() || this.getBombDeliveryTarget() != null;
	}

	public double getAITargetY() {
		net.minecraft.entity.EntityLivingBase target = this.getAttackTarget();
		if (target != null && !this.isWorking()) {
			return target.posY;
		} else if (this.getNavigator().getPath() != null && !this.getNavigator().getPath().isFinished()) {
			return this.getNavigator().getPath().getFinalPathPoint().y;
		} else if (this.getOwner() != null && this.getDistanceSq(this.getOwner()) > 25.0D) {
			return this.getOwner().posY;
		}
		return -999;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if (!this.world.isRemote && !this.equipmentInitialized && this.ticksExisted < 20) {
			this.initBroodEquipment();
		}

		if (this.isOnLadder() || this.collidedHorizontally) {
			double targetY;
			if (!this.world.isRemote) {
				targetY = this.getAITargetY();
				this.dataManager.set(TARGET_Y, (float) targetY);
			} else {
				targetY = this.dataManager.get(TARGET_Y);
			}

			if (targetY > this.posY + 0.5D) {
				if (this.isOnLadder()) {
					BlockPos ladderPos = this.getActiveLadderPos();
					if (ladderPos == null) ladderPos = new BlockPos(this);
					boolean isTopLadder = this.world.getBlockState(ladderPos.up()).getBlock() != net.minecraft.init.Blocks.LADDER;
					
					if (!isTopLadder || this.posY - ladderPos.getY() < 1.5D) {
						net.minecraft.block.state.IBlockState ladderState = this.world.getBlockState(ladderPos);
						if (ladderState.getBlock() == net.minecraft.init.Blocks.LADDER) {
							net.minecraft.util.EnumFacing facing = ladderState.getValue(net.minecraft.block.BlockLadder.FACING);
							float targetYaw = facing.getHorizontalAngle();
							this.rotationYaw = targetYaw;
							this.rotationYawHead = targetYaw;
							this.renderYawOffset = targetYaw;
							double targetX = ladderPos.getX() + 0.5D + facing.getXOffset() * 2.0D;
							double targetZ = ladderPos.getZ() + 0.5D + facing.getZOffset() * 2.0D;
							this.getMoveHelper().setMoveTo(targetX, this.posY, targetZ, 1.0D);
							this.motionY = 0.2D;
						}
					}
				} else {
					BlockPos pos = new BlockPos(this);
					for (net.minecraft.util.EnumFacing side : net.minecraft.util.EnumFacing.HORIZONTALS) {
						if (this.world.getBlockState(pos.offset(side)).getBlock() == net.minecraft.init.Blocks.LADDER) {
							float targetYaw = side.getHorizontalAngle();
							this.rotationYaw = targetYaw;
							this.rotationYawHead = targetYaw;
							this.renderYawOffset = targetYaw;
							double targetX = this.posX + side.getXOffset() * 2.0D;
							double targetZ = this.posZ + side.getZOffset() * 2.0D;
							this.getMoveHelper().setMoveTo(targetX, this.posY, targetZ, 1.0D);
							this.motionY = 0.2D;
							break;
						}
					}
				}
			} else if (targetY < this.posY - 0.5D) {
				if (this.isOnLadder()) {
					BlockPos ladderPos = this.getActiveLadderPos();
					if (ladderPos != null) {
						net.minecraft.block.state.IBlockState ladderState = this.world.getBlockState(ladderPos);
						if (ladderState.getBlock() == net.minecraft.init.Blocks.LADDER) {
							net.minecraft.util.EnumFacing facing = ladderState.getValue(net.minecraft.block.BlockLadder.FACING);
							float targetYaw = facing.getHorizontalAngle();
							this.rotationYaw = targetYaw;
							this.rotationYawHead = targetYaw;
							this.renderYawOffset = targetYaw;
						}
						double centerX = ladderPos.getX() + 0.5D;
						double centerZ = ladderPos.getZ() + 0.5D;
						this.getMoveHelper().setMoveTo(centerX, this.posY, centerZ, 0.5D);
						this.motionY = -0.15D;
					}
				}
			}
		}

		// Check if there are enough goblins nearby for group hostility behavior
		int threshold = Settings.miscSettings.goblinGroupHostilityThreshold;
		boolean inLargeGroup = false;
		
		if (threshold > 0) {
			int nearbyGoblins = this.world.getEntitiesWithinAABB(
				EntityGoblin.class,
				this.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D)
			).size();
			
			inLargeGroup = nearbyGoblins >= threshold;
		}

		// Goblins holding idols ignore ownership mechanics
		if (isHoldingIdol()) {
			if (hasOwner()) {
				setOwnerId(null);
				setNeutral(false);
				ownershipLossTimer = 0;
				hostilityTimer = 0;
			}
			return;
		}

		// If in a large group, turn hostile and ignore idol alliances
		if (inLargeGroup) {
			if (hasOwner()) {
				// Break alliance and turn hostile
				setOwnerId(null);
				setNeutral(false);
				ownershipLossTimer = 0;
				hostilityTimer = 0;
			}
			// Skip the normal alliance logic below
			return;
		}

		if (this.isImmuneToIdol()) {
			if (this.hasOwner()) {
				this.setOwnerId(null);
				this.setNeutral(false);
			}
		} else if (hasOwner()) {
			EntityLivingBase owner = getOwner();
			boolean ownerMissingOrNotHoldingActiveIdol = true;
			if (owner instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) owner;
				if (player.isEntityAlive()) {
					// Check both hands for active goblin idol
					ItemStack mainHand = player.getHeldItemMainhand();
					ItemStack offHand = player.getHeldItemOffhand();
					
					if (isActiveGoblinIdol(mainHand) || isActiveGoblinIdol(offHand)) {
						ownerMissingOrNotHoldingActiveIdol = false;
					}
				}
			}

			if (ownerMissingOrNotHoldingActiveIdol) {
				ownershipLossTimer++;
				if (ownershipLossTimer > 20) { // Instant transition to neutral
					setOwnerId(null);
					setNeutral(true);
					ownershipLossTimer = 0;
					hostilityTimer = 0;
				}
			} else {
				ownershipLossTimer = 0;
			}
		} else if (isNeutral()) {
			hostilityTimer++;
			if (hostilityTimer > 60) { // 3 seconds (60 ticks) before becoming hostile
				setNeutral(false);
				hostilityTimer = 0;
				// Spawn angry particles
				if (this.world.isRemote) {
					for (int i = 0; i < 5; i++) {
						double d0 = this.rand.nextGaussian() * 0.02D;
						double d1 = this.rand.nextGaussian() * 0.02D;
						double d2 = this.rand.nextGaussian() * 0.02D;
						this.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY,
								this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width,
								this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height),
								this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width,
								d0, d1, d2);
					}
				}
			}
		} else {
			// Check if any nearby player is holding an active goblin idol
			EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, 16.0D);
			if (nearestPlayer != null && !nearestPlayer.isCreative() && !nearestPlayer.isSpectator()) {
				ItemStack mainHand = nearestPlayer.getHeldItemMainhand();
				ItemStack offHand = nearestPlayer.getHeldItemOffhand();
				
				if (isActiveGoblinIdol(mainHand) || isActiveGoblinIdol(offHand)) {
					// Ally with this player
					this.setTamedBy(nearestPlayer);
					this.setNeutral(false);
					this.hostilityTimer = 0;
					this.setAttackTarget(null);
				}
			}
		}
	}

	private boolean isActiveGoblinIdol(ItemStack stack) {
		if (stack.getItem() == ModItems.goblin_idol) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("active")) {
				return stack.getTagCompound().getBoolean("active");
			}
		}
		return false;
	}

	public boolean isHoldingIdol() {
		ItemStack offhand = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
		return !offhand.isEmpty() && offhand.getItem() == ModItems.goblin_idol;
	}

	public boolean isCarryingBomb() {
		for (Entity passenger : this.getPassengers()) {
			if (passenger instanceof EntityWroughtBomb) {
				return true;
			}
		}
		ItemStack offhand = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
		ItemStack mainhand = this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
		return (!offhand.isEmpty() && offhand.getItem() == Item.getItemFromBlock(ModBlocks.wrought_bomb)) ||
		       (!mainhand.isEmpty() && mainhand.getItem() == Item.getItemFromBlock(ModBlocks.wrought_bomb));
	}

	@Override
	public double getMountedYOffset() {
		if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof EntityWroughtBomb) {
			return (double)this.height + 0.1D;
		}
		return super.getMountedYOffset();
	}

	@Override
	public void applyEntityCollision(net.minecraft.entity.Entity entityIn) {
		if (entityIn instanceof EntityGoblin) {
			return;
		}
		super.applyEntityCollision(entityIn);
	}

	public boolean hasOwner() {
		return this.getOwnerId() != null;
	}

	private void setTamedBy(EntityPlayer player) {
		this.setOwnerId(player.getUniqueID());
	}

	public void setOwnerId(@Nullable UUID p_184754_1_) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}


	@Override
	public int getTalkInterval() {
		return 160;
	}

	@Override
	@Nullable
	protected SoundEvent getAmbientSound() {
		return this.rand.nextInt(3) == 0 ? ModSounds.GOBLIN_IDLE : null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.GOBLIN_DIE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.GOBLIN_HURT;
	}

	@Override
	public float getEyeHeight() {
		return 0.9F;
	}

	@Nullable
	public UUID getOwnerId() {
		return (UUID) ((Optional) this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
	}

	@Nullable
	public EntityLivingBase getOwner() {
		try {
			UUID uuid = this.getOwnerId();
			return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {
		if (!(target instanceof EntityCreeper) && !(target instanceof EntityGhast)) {
			if (target instanceof EntityWolf) {
				EntityWolf entitywolf = (EntityWolf) target;

				if (entitywolf.isTamed() && entitywolf.getOwner() == owner) {
					return false;
				}
			}

			if (target instanceof EntityPlayer && owner instanceof EntityPlayer && !((EntityPlayer) owner).canAttackPlayer((EntityPlayer) target)) {
				return false;
			} else {
				return !(target instanceof AbstractHorse) || !((AbstractHorse) target).isTame();
			}
		} else {
			return false;
		}
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(net.minecraft.world.DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.initBroodEquipment();
	}

	private void initBroodEquipment() {
		if (!this.equipmentInitialized) {
			this.equipmentInitialized = true;
			if (this.getClass() == EntityGoblin.class) {
				if (this.rand.nextFloat() < 0.7F) {
					this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.crooked_bone));
				}
			}
		}
	}

	@Override
	public boolean canPickUpLoot() {
		return this.getClass() == EntityGoblin.class;
	}

	@Override
	protected void updateEquipmentIfNeeded(net.minecraft.entity.item.EntityItem itemEntity) {
		ItemStack itemstack = itemEntity.getItem();
		
		if (itemstack.getItem() == Item.getItemFromBlock(ModBlocks.wrought_bomb)) {
			return;
		}

		// Prioritize picking up goblin idols
		if (itemstack.getItem() == ModItems.goblin_idol) {
			// Drop current offhand item if present
			ItemStack currentOffhand = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
			if (!currentOffhand.isEmpty()) {
				this.entityDropItem(currentOffhand, 0.0F);
			}
			
			// Pick up the idol in offhand regardless of current equipment
			this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, itemstack.copy());
			this.onItemPickup(itemEntity, itemstack.getCount());
			itemEntity.setDead();
			return;
		}
		
		super.updateEquipmentIfNeeded(itemEntity);
	}

	public BlockPos getBombDeliveryTarget() {
		return this.bombDeliveryTarget;
	}

	public void setBombDeliveryTarget(BlockPos target) {
		this.bombDeliveryTarget = target;
	}
}
