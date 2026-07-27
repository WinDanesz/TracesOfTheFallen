package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntitySidhe extends EntityMob {

	private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> TILTING = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.BOOLEAN);

	public float standProgress = 0.0F;
	public float prevStandProgress = 0.0F;
	private int tiltTimer = 0;
	private EntityPlayer pendingTarget = null;

	public int rewardTimer = 0;
	public EntityPlayer rewardingPlayer = null;
	public String pendingRewardAction = null;

	public EntitySidhe(World worldIn) {
		super(worldIn);
		this.setSize(0.8F, 0.8F);
		this.experienceValue = 5;
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAISidheReward(this));
		this.tasks.addTask(1, new EntityAISidheRetaliate(this));
		this.tasks.addTask(2, new EntityAISidheTempt(this, 1.0D));
		this.tasks.addTask(3, new net.minecraft.entity.ai.EntityAIWander(this, 1.0D, 120));
		this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(5, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(STANDING, false);
		this.dataManager.register(TILTING, false);
		this.dataManager.register(ANGRY, false);
	}

	public boolean isTilting() {
		return this.dataManager.get(TILTING);
	}

	public boolean isStanding() {
		return this.dataManager.get(STANDING);
	}

	public void setStanding(boolean standing) {
		this.dataManager.set(STANDING, standing);
	}

	public boolean isAngry() {
		return this.dataManager.get(ANGRY);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.world.isRemote) {
			boolean angry = this.getAttackTarget() != null || this.getRevengeTarget() != null;
			if (this.dataManager.get(ANGRY) != angry) {
				this.dataManager.set(ANGRY, angry);
			}

			if (this.tiltTimer > 0) {
				this.tiltTimer--;
				if (this.pendingTarget != null) {
					this.getLookHelper().setLookPositionWithEntity(this.pendingTarget, 30.0F, 30.0F);
				}
				if (this.tiltTimer <= 0) {
					this.dataManager.set(TILTING, false);
					if (this.pendingTarget != null) {
						this.setAttackTarget(this.pendingTarget);
						this.pendingTarget = null;
					}
				}
			} else if (this.rewardTimer <= 0 && this.getAttackTarget() == null) {
				boolean shouldStand = true;
				EntityPlayer nearbyPlayer = this.world.getClosestPlayerToEntity(this, 30.0D);
				if (nearbyPlayer != null) {
					net.minecraft.util.math.Vec3d lookVec = nearbyPlayer.getLook(1.0F).normalize();
					net.minecraft.util.math.Vec3d toSidhe = new net.minecraft.util.math.Vec3d(
							this.posX - nearbyPlayer.posX, 
							this.posY + (double)this.getEyeHeight() - (nearbyPlayer.posY + (double)nearbyPlayer.getEyeHeight()), 
							this.posZ - nearbyPlayer.posZ);
					
					if (toSidhe.length() > 0.0D) {
						toSidhe = toSidhe.normalize();
						double dot = lookVec.dotProduct(toSidhe);
						// If the player is generally looking towards the sidhe and has line of sight
						if (dot > 0.5D && nearbyPlayer.canEntityBeSeen(this)) {
							shouldStand = false;
						}
					}
				}
				this.setStanding(shouldStand);
			}
		}

		this.prevStandProgress = this.standProgress;
		if (this.isStanding()) {
			this.standProgress += 0.1F;
			if (this.standProgress > 1.0F) {
				this.standProgress = 1.0F;
			}
		} else {
			this.standProgress -= 0.1F;
			if (this.standProgress < 0.0F) {
				this.standProgress = 0.0F;
			}
		}
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		String action = getInteractAction(stack);
		if (action != null) {
			if (!this.world.isRemote && this.tiltTimer <= 0 && this.rewardTimer <= 0 && !this.isStanding()) {
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				
				if (action.equals("attack")) {
					this.tiltTimer = 20;
					this.pendingTarget = player;
					this.dataManager.set(TILTING, true);
					this.getNavigator().clearPath();
				} else {
					this.rewardTimer = 50; // 2.5 seconds to stand up before rewarding
					this.rewardingPlayer = player;
					this.pendingRewardAction = action;
					this.getNavigator().clearPath();
				}
			}
			return true;
		}
		return super.processInteract(player, hand);
	}

	public static String getInteractAction(ItemStack stack) {
		if (stack.isEmpty()) return null;
		
		net.minecraft.util.ResourceLocation regName = stack.getItem().getRegistryName();
		if (regName == null) return null;
		String id = regName.toString();
		int meta = stack.getMetadata();
		
		for (String entry : com.windanesz.tracesofthefallen.Settings.miscSettings.sidheInteractItems) {
			String[] parts = entry.split("\\|");
			if (parts.length < 2) continue;
			
			String itemPattern = parts[0]; 
			String[] itemParts = itemPattern.split(":", 4);
			if (itemParts.length >= 2) {
				String configId = itemParts[0] + ":" + itemParts[1];
				if (configId.equals(id)) {
					boolean metaMatches = false;
					if (itemParts.length >= 3) {
						String configMeta = itemParts[2];
						if (configMeta.equals("*") || configMeta.equals(String.valueOf(meta))) {
							metaMatches = true;
						}
					} else {
						if (meta == 0) metaMatches = true;
					}
					
					if (metaMatches) {
						if (itemParts.length >= 4) {
							String nbtString = itemParts[3];
							try {
								net.minecraft.nbt.NBTTagCompound tag = net.minecraft.nbt.JsonToNBT.getTagFromJson(nbtString);
								if (stack.hasTagCompound() && net.minecraft.nbt.NBTUtil.areNBTEquals(tag, stack.getTagCompound(), true)) {
									return parts[1];
								}
							} catch (Exception e) {
							}
						} else {
							return parts[1];
						}
					}
				}
			}
		}
		return null;
	}

	public static ItemStack parseRewardItem(String action) {
		if (action == null || action.equals("attack")) return ItemStack.EMPTY;
		
		String[] qtyParts = action.split("\\*");
		int count = 1;
		if (qtyParts.length > 1) {
			try { count = Integer.parseInt(qtyParts[1]); } catch (Exception e) {}
		}
		
		String[] itemParts = qtyParts[0].split(":", 4);
		if (itemParts.length >= 2) {
			net.minecraft.item.Item item = net.minecraft.item.Item.REGISTRY.getObject(new net.minecraft.util.ResourceLocation(itemParts[0], itemParts[1]));
			if (item != null) {
				int meta = 0;
				if (itemParts.length >= 3) {
					try { meta = Integer.parseInt(itemParts[2]); } catch (Exception e) {}
				}
				ItemStack stack = new ItemStack(item, count, meta);
				if (itemParts.length >= 4) {
					try {
						stack.setTagCompound(net.minecraft.nbt.JsonToNBT.getTagFromJson(itemParts[3]));
					} catch (Exception e) {}
				}
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	public void despawnPoof() {
		if (!this.world.isRemote) {
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 1.0F);
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + this.height / 2.0F, this.posZ, 20, 0.3D, 0.3D, 0.3D, 0.05D);
			}
			this.setDead();
		}
	}

	static class EntityAISidheRetaliate extends EntityAIBase {
		private final EntitySidhe sidhe;
		private int attackTimer;

		public EntityAISidheRetaliate(EntitySidhe sidhe) {
			this.sidhe = sidhe;
			this.setMutexBits(3); // Movement and looking
		}

		@Override
		public boolean shouldExecute() {
			return this.sidhe.getAttackTarget() != null && this.sidhe.getAttackTarget().isEntityAlive();
		}

		@Override
		public void startExecuting() {
			this.sidhe.setStanding(true);
			this.sidhe.getNavigator().clearPath();
			this.attackTimer = 60; // 3 seconds wait
		}

		@Override
		public void updateTask() {
			EntityLivingBase target = this.sidhe.getAttackTarget();
			if (target != null) {
				this.sidhe.getLookHelper().setLookPositionWithEntity(target, 360.0F, 360.0F);
				double d0 = target.posX - this.sidhe.posX;
				double d1 = target.posZ - this.sidhe.posZ;
				float yaw = (float)(net.minecraft.util.math.MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
				this.sidhe.rotationYaw = yaw;
				this.sidhe.rotationYawHead = yaw;
				this.sidhe.renderYawOffset = yaw;
			}

			if (this.attackTimer > 0) {
				this.attackTimer--;
				if (this.attackTimer == 6) {
					this.sidhe.motionY = 0.42D;
					if (target != null) {
						net.minecraft.util.math.Vec3d away = new net.minecraft.util.math.Vec3d(this.sidhe.posX - target.posX, 0.0D, this.sidhe.posZ - target.posZ).normalize();
						this.sidhe.motionX += away.x * 0.37D;
						this.sidhe.motionZ += away.z * 0.37D;
					}
					this.sidhe.isAirBorne = true;
					this.sidhe.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
				}
				if (this.attackTimer == 0 && target != null) {
					if (!this.sidhe.world.isRemote) {
						EntityVoidSlash slash = new EntityVoidSlash(this.sidhe.world, this.sidhe, target);
						this.sidhe.world.spawnEntity(slash);
						this.sidhe.world.playSound(null, this.sidhe.posX, this.sidhe.posY, this.sidhe.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 1.0F);
					}
				}
			} else if (this.attackTimer <= 0) {
				// Poof away after shooting
				this.sidhe.despawnPoof();
			}
		}

		@Override
		public void resetTask() {
			this.sidhe.setStanding(false);
		}
	}

	static class EntityAISidheReward extends EntityAIBase {
		private final EntitySidhe sidhe;

		public EntityAISidheReward(EntitySidhe sidhe) {
			this.sidhe = sidhe;
			this.setMutexBits(3); // Movement and looking
		}

		@Override
		public boolean shouldExecute() {
			return this.sidhe.rewardTimer > 0;
		}

		@Override
		public void startExecuting() {
			this.sidhe.setStanding(true);
			this.sidhe.getNavigator().clearPath();
		}

		@Override
		public void updateTask() {
			if (this.sidhe.rewardingPlayer != null) {
				this.sidhe.getLookHelper().setLookPositionWithEntity(this.sidhe.rewardingPlayer, 30.0F, 30.0F);
			}

			if (this.sidhe.rewardTimer > 0) {
				this.sidhe.rewardTimer--;
				if (this.sidhe.rewardTimer <= 0) {
					if (!this.sidhe.world.isRemote && this.sidhe.pendingRewardAction != null) {
						ItemStack reward = parseRewardItem(this.sidhe.pendingRewardAction);
						if (!reward.isEmpty()) {
							net.minecraft.entity.item.EntityItem entityItem = new net.minecraft.entity.item.EntityItem(this.sidhe.world, this.sidhe.posX, this.sidhe.posY + 0.5D, this.sidhe.posZ, reward);
							this.sidhe.world.spawnEntity(entityItem);
						}
						this.sidhe.world.playSound(null, this.sidhe.posX, this.sidhe.posY, this.sidhe.posZ, net.minecraft.init.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					}
					this.sidhe.despawnPoof();
				}
			}
		}

		@Override
		public void resetTask() {
			this.sidhe.setStanding(false);
		}
	}

	class EntityAISidheTempt extends EntityAIBase {
		private final EntitySidhe sidhe;
		private final double speed;
		private EntityPlayer temptingPlayer;
		private int delayTemptCounter;

		public EntityAISidheTempt(EntitySidhe sidhe, double speedIn) {
			this.sidhe = sidhe;
			this.speed = speedIn;
			this.setMutexBits(3);
		}

		@Override
		public boolean shouldExecute() {
			if (this.delayTemptCounter > 0) {
				this.delayTemptCounter--;
				return false;
			} else {
				this.temptingPlayer = this.sidhe.world.getClosestPlayerToEntity(this.sidhe, 10.0D);
				if (this.temptingPlayer == null) {
					return false;
				} else {
					return isTempting(this.temptingPlayer.getHeldItemMainhand()) || isTempting(this.temptingPlayer.getHeldItemOffhand());
				}
			}
		}

		private boolean isTempting(ItemStack stack) {
			return getInteractAction(stack) != null;
		}

		@Override
		public void startExecuting() {
			this.sidhe.getNavigator().clearPath();
		}

		@Override
		public void resetTask() {
			this.temptingPlayer = null;
			this.sidhe.getNavigator().clearPath();
			this.delayTemptCounter = 100;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.temptingPlayer != null && this.temptingPlayer.isEntityAlive() && (isTempting(this.temptingPlayer.getHeldItemMainhand()) || isTempting(this.temptingPlayer.getHeldItemOffhand())) && this.sidhe.getDistanceSq(this.temptingPlayer) < 256.0D;
		}

		@Override
		public void updateTask() {
			this.sidhe.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, 30.0F, (float) this.sidhe.getVerticalFaceSpeed());
			if (this.sidhe.getDistanceSq(this.temptingPlayer) < 6.25D) {
				this.sidhe.getNavigator().clearPath();
			} else {
				this.sidhe.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
			}
		}
	}
}
