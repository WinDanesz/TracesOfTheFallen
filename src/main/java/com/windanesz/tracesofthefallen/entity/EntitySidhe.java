package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.SidheBarter;
import com.windanesz.tracesofthefallen.SidheDiscoveries;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

public class EntitySidhe extends EntityMob {

	private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> TILTING = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.BOOLEAN);
	private static final DataParameter<ItemStack> MOUTH_ITEM = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.ITEM_STACK);
	private static final DataParameter<ItemStack> HAND_ITEM = EntityDataManager.createKey(EntitySidhe.class, DataSerializers.ITEM_STACK);

	/** How long the Sidhe holds a given item in its mouth before deciding, in ticks (2 seconds). */
	private static final int MOUTH_HOLD_TICKS = 40;
	/** Ticks spent pulling the reward out of its pocket, and waiting after dropping it before poofing away. */
	private static final int POCKET_TICKS = 25;
	private static final int AFTER_DROP_TICKS = 15;

	public float standProgress = 0.0F;
	public float prevStandProgress = 0.0F;
	private int tiltTimer = 0;
	private EntityPlayer pendingTarget = null;
	/** Ticks until the disliked item is spat back at {@link #spitTarget}; -1 when nothing is pending. */
	private int spitDelay = -1;
	private EntityPlayer spitTarget = null;

	public int rewardTimer = 0;
	public EntityPlayer rewardingPlayer = null;
	public String pendingRewardAction = null;
	public String pendingRewardKey = null;
	/** The reward action picked for this trade, one of the alternatives in pendingRewardAction. */
	private String pendingRewardChosen = null;
	/** 0 = holding the offering in its mouth, 1 = taking the reward out of its pocket, 2 = reward dropped, about to poof. */
	private int rewardStage = 0;

	public EntitySidhe(World worldIn) {
		super(worldIn);
		this.setSize(0.8F, 0.8F);
		this.experienceValue = 5;
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAISidheHoldMouthful(this));
		this.tasks.addTask(1, new EntityAISidheReward(this));
		this.tasks.addTask(1, new EntityAISidheRetaliate(this));
		this.tasks.addTask(2, new EntityAISidheTempt(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0D, 120));
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
		this.dataManager.register(MOUTH_ITEM, ItemStack.EMPTY);
		this.dataManager.register(HAND_ITEM, ItemStack.EMPTY);
	}

	/** The item currently held in the Sidhe's mouth (synced to the client for rendering). */
	public ItemStack getMouthItem() {
		return this.dataManager.get(MOUTH_ITEM);
	}

	private void setMouthItem(ItemStack stack) {
		this.dataManager.set(MOUTH_ITEM, stack);
	}

	/** The reward currently held in the Sidhe's hand (synced to the client for rendering). */
	public ItemStack getHandItem() {
		return this.dataManager.get(HAND_ITEM);
	}

	private void setHandItem(ItemStack stack) {
		this.dataManager.set(HAND_ITEM, stack);
	}

	/** A few of the void slash's purple particles around the paw the reward is held in. */
	private void spawnHandParticles() {
		if (!(this.world instanceof WorldServer)) return;
		Vec3d side = Vec3d.fromPitchYaw(0.0F, this.renderYawOffset + 90.0F);
		Vec3d front = Vec3d.fromPitchYaw(0.0F, this.renderYawOffset);
		((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH,
				this.posX + side.x * 0.25D + front.x * 0.3D, this.posY + 0.55D, this.posZ + side.z * 0.25D + front.z * 0.3D,
				6, 0.15D, 0.15D, 0.15D, 0.05D);
	}

	/** Drops the reward held in its hand on the ground beside it. */
	private void dropHandItem() {
		ItemStack held = getHandItem();
		if (held.isEmpty()) return;
		setHandItem(ItemStack.EMPTY);

		Vec3d side = Vec3d.fromPitchYaw(0.0F, this.renderYawOffset + 90.0F);
		EntityItem dropped = new EntityItem(this.world, this.posX + side.x * 0.7D, this.posY + 0.5D, this.posZ + side.z * 0.7D, held);
		dropped.motionX = side.x * 0.1D;
		dropped.motionY = 0.15D;
		dropped.motionZ = side.z * 0.1D;
		dropped.setPickupDelay(10);
		this.world.spawnEntity(dropped);
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
	}

	/** Spits the held item out in front of the Sidhe's face. */
	private void spitOutMouthItem() {
		ItemStack held = getMouthItem();
		if (held.isEmpty()) return;
		setMouthItem(ItemStack.EMPTY);

		// The mouth is higher up while the Sidhe stands on its hind legs
		double mouthY = this.posY + (this.isStanding() ? 0.9D : 0.4D);
		Vec3d dir = Vec3d.fromPitchYaw(0.0F, this.rotationYawHead);
		if (this.spitTarget != null && this.spitTarget.isEntityAlive()) {
			Vec3d toTarget = new Vec3d(this.spitTarget.posX - this.posX, 0.0D, this.spitTarget.posZ - this.posZ);
			if (toTarget.lengthSquared() > 1.0E-4D) dir = toTarget.normalize();
		}
		EntityItem spat = new EntityItem(this.world, this.posX + dir.x * 0.6D, mouthY, this.posZ + dir.z * 0.6D, held);
		spat.motionX = dir.x * 0.35D;
		spat.motionY = 0.2D;
		spat.motionZ = dir.z * 0.35D;
		spat.setPickupDelay(40);
		this.world.spawnEntity(spat);
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.HOSTILE, 1.0F, 1.0F);
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote) {
			// An accepted offering is consumed, so only a refused one is dropped
			if (this.rewardStage == 0) spitOutMouthItem();
			dropHandItem();
		}
		super.onDeath(cause);
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

			if (this.spitDelay >= 0 && --this.spitDelay < 0) {
				spitOutMouthItem();
				this.spitTarget = null;
			}

			if (this.tiltTimer > 0) {
				this.tiltTimer--;
				if (this.tiltTimer <= 0) {
					this.dataManager.set(TILTING, false);
					// The retaliation stands the Sidhe up (0.5s); the item is spat back once it is on its hind legs
					this.spitDelay = 10;
					this.spitTarget = this.pendingTarget;
					if (this.pendingTarget != null) {
						this.setAttackTarget(this.pendingTarget);
						this.pendingTarget = null;
					}
				}
			} else if (this.rewardTimer <= 0 && this.getAttackTarget() == null) {
				boolean shouldStand = true;
				EntityPlayer nearbyPlayer = this.world.getClosestPlayerToEntity(this, 30.0D);
				if (nearbyPlayer != null) {
					Vec3d lookVec = nearbyPlayer.getLook(1.0F).normalize();
					Vec3d toSidhe = new Vec3d(
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
		String[] entry = getInteractEntry(stack);
		// The outcome depends on the world seed, which only the server knows; the client just needs to know the item is accepted
		String action = entry == null ? null : this.world.isRemote ? entry[1] : SidheBarter.resolveAction(player, stack, entry);
		if (action != null) {
			if (!this.world.isRemote && this.tiltTimer <= 0 && this.rewardTimer <= 0 && !this.isStanding()) {
				// Capture before shrinking: a depleted stack reports as air
				String discoveryKey = getDiscoveryKey(stack);
				ItemStack mouthStack = stack.copy();
				mouthStack.setCount(1);
				setMouthItem(mouthStack);
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				
				if (action.equals("attack")) {
					SidheDiscoveries.discover(player, discoveryKey, action, null);
					this.tiltTimer = MOUTH_HOLD_TICKS; // hold the item, tilting its head, then spit it out
					this.pendingTarget = player;
					this.dataManager.set(TILTING, true);
					this.getNavigator().clearPath();
				} else {
					this.rewardTimer = MOUTH_HOLD_TICKS; // hold the item in its mouth before rewarding
					this.rewardStage = 0;
					this.rewardingPlayer = player;
					this.pendingRewardAction = action;
					this.pendingRewardKey = discoveryKey;
					this.getNavigator().clearPath();
				}
			}
			return true;
		}
		return super.processInteract(player, hand);
	}

	/**
	 * Discovery key of a concrete item ('modid:name:meta'). Config entries may use a wildcard meta, but what a player has
	 * discovered is tracked per item variant, so feeding one variant doesn't reveal the others.
	 */
	public static String getDiscoveryKey(ItemStack stack) {
		return stack.getItem().getRegistryName() + ":" + stack.getMetadata();
	}

	public static String getInteractAction(ItemStack stack) {
		String[] entry = getInteractEntry(stack);
		return entry == null ? null : entry[1];
	}

	/** @return {input pattern, action} of the first matching Sidhe interaction config entry, or null. */
	public static String[] getInteractEntry(ItemStack stack) {
		if (stack.isEmpty()) return null;
		
		ResourceLocation regName = stack.getItem().getRegistryName();
		if (regName == null) return null;
		String id = regName.toString();
		int meta = stack.getMetadata();
		
		for (String entry : Settings.miscSettings.sidheInteractItems) {
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
								NBTTagCompound tag = JsonToNBT.getTagFromJson(nbtString);
								if (stack.hasTagCompound() && NBTUtil.areNBTEquals(tag, stack.getTagCompound(), true)) {
									return new String[] {parts[0], parts[1]};
								}
							} catch (Exception e) {
							}
						} else {
							return new String[] {parts[0], parts[1]};
						}
					}
				}
			}
		}
		return null;
	}

	/** All rewards an action can give: a single stack, or several separated by ';' of which one is picked per trade. */
	public static List<ItemStack> parseRewardItems(String action) {
		List<ItemStack> rewards = new ArrayList<>();
		if (action == null || action.equals(SidheBarter.ATTACK)) return rewards;
		for (String part : action.split(SidheBarter.REWARD_SEPARATOR)) {
			ItemStack reward = parseRewardItem(part.trim());
			if (!reward.isEmpty()) rewards.add(reward);
		}
		return rewards;
	}

	/** Picks one of the alternative rewards of an action at random; null if there is none. */
	private String pickRewardAction(String action) {
		if (action == null || action.equals(SidheBarter.ATTACK)) return null;
		List<String> options = new ArrayList<>();
		for (String part : action.split(SidheBarter.REWARD_SEPARATOR)) {
			if (!parseRewardItem(part.trim()).isEmpty()) options.add(part.trim());
		}
		return options.isEmpty() ? null : options.get(this.rand.nextInt(options.size()));
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
			Item item = Item.REGISTRY.getObject(new ResourceLocation(itemParts[0], itemParts[1]));
			if (item != null) {
				int meta = 0;
				if (itemParts.length >= 3) {
					try { meta = Integer.parseInt(itemParts[2]); } catch (Exception e) {}
				}
				ItemStack stack = new ItemStack(item, count, meta);
				if (itemParts.length >= 4) {
					try {
						stack.setTagCompound(JsonToNBT.getTagFromJson(itemParts[3]));
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
				float yaw = (float)(MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
				this.sidhe.rotationYaw = yaw;
				this.sidhe.rotationYawHead = yaw;
				this.sidhe.renderYawOffset = yaw;
			}

			if (this.attackTimer > 0) {
				this.attackTimer--;
				if (this.attackTimer == 6) {
					this.sidhe.motionY = 0.42D;
					if (target != null) {
						Vec3d away = new Vec3d(this.sidhe.posX - target.posX, 0.0D, this.sidhe.posZ - target.posZ).normalize();
						this.sidhe.motionX += away.x * 0.37D;
						this.sidhe.motionZ += away.z * 0.37D;
					}
					this.sidhe.isAirBorne = true;
					this.sidhe.swingArm(EnumHand.MAIN_HAND);
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

	/** While it has a disliked item in its mouth the Sidhe stays put and ignores the player (no following, no watching). */
	static class EntityAISidheHoldMouthful extends EntityAIBase {
		private final EntitySidhe sidhe;

		public EntityAISidheHoldMouthful(EntitySidhe sidhe) {
			this.sidhe = sidhe;
			this.setMutexBits(3); // Movement and looking
		}

		@Override
		public boolean shouldExecute() {
			return this.sidhe.tiltTimer > 0;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.sidhe.tiltTimer > 0;
		}

		@Override
		public void startExecuting() {
			this.sidhe.getNavigator().clearPath();
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
			if (this.sidhe.rewardTimer <= 0) return;
			this.sidhe.rewardTimer--;
			if (this.sidhe.rewardTimer > 0) return;

			EntitySidhe sidhe = this.sidhe;
			switch (sidhe.rewardStage) {
				case 0: // offering accepted (it stays in its mouth): take the reward out of its pocket
					sidhe.pendingRewardChosen = sidhe.pickRewardAction(sidhe.pendingRewardAction);
					ItemStack reward = parseRewardItem(sidhe.pendingRewardChosen);
					if (reward.isEmpty()) {
						sidhe.despawnPoof();
						return;
					}
					sidhe.setHandItem(reward);
					sidhe.spawnHandParticles();
					sidhe.world.playSound(null, sidhe.posX, sidhe.posY, sidhe.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.6F, 0.8F);
					sidhe.rewardStage = 1;
					sidhe.rewardTimer = POCKET_TICKS;
					break;
				case 1: // drop it on the ground next to it
					sidhe.dropHandItem();
					sidhe.swingArm(EnumHand.MAIN_HAND);
					if (sidhe.rewardingPlayer != null && sidhe.pendingRewardKey != null) {
						SidheDiscoveries.discover(sidhe.rewardingPlayer, sidhe.pendingRewardKey, sidhe.pendingRewardAction, sidhe.pendingRewardChosen);
					}
					sidhe.rewardStage = 2;
					sidhe.rewardTimer = AFTER_DROP_TICKS;
					break;
				default: // done
					sidhe.despawnPoof();
					break;
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
