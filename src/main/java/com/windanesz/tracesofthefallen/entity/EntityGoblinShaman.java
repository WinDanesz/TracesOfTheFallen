package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIShamanDance;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIShamanStandStill;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class EntityGoblinShaman extends EntityGoblin {
	private static final DataParameter<Integer> DANCE_TYPE = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SPELL_CASTING_TIMER = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SPELL_CAST_TYPE = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);

	public EntityGoblinShaman(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DANCE_TYPE, 0);
		this.dataManager.register(SPELL_CASTING_TIMER, 0);
		this.dataManager.register(SPELL_CAST_TYPE, 0);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new GoblinAIShamanStandStill(this));
		this.tasks.addTask(4, new GoblinAIShamanDance(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinShamanMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinShamanAttackDamage);
	}

	public int getDanceType() {
		return this.dataManager.get(DANCE_TYPE);
	}

	public void setDanceType(int type) {
		this.dataManager.set(DANCE_TYPE, type);
	}

	public int getSpellCastingTimer() {
		return this.dataManager.get(SPELL_CASTING_TIMER);
	}

	public void setSpellCastingTimer(int timer) {
		this.dataManager.set(SPELL_CASTING_TIMER, timer);
	}

	public int getSpellCastType() {
		return this.dataManager.get(SPELL_CAST_TYPE);
	}

	public void setSpellCastType(int type) {
		this.dataManager.set(SPELL_CAST_TYPE, type);
	}

	public int danceCooldown = 0;
	private int totemCooldown = 0;
	private int fireOrbCooldown = 0;
	private int chillPulseCooldown = 0;
	private int boneRattleCooldown = 0;
	private int willOWispCooldown = 0;
	private int seekingOrbCooldown = 0;

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.danceCooldown > 0) {
			this.danceCooldown--;
		}
		int castTimer = this.getSpellCastingTimer();
		int castType = this.getSpellCastType();
		if (castTimer > 0) {
			if (!this.world.isRemote) {
				this.setSpellCastingTimer(castTimer - 1);
				if (castTimer - 1 == 0) {
					this.setSpellCastType(0);
				}
			}
			if (this.getAttackTarget() != null) {
				this.getLookHelper().setLookPositionWithEntity(this.getAttackTarget(), 30.0F, 30.0F);
			}
			this.getNavigator().clearPath();

			double particleX = this.posX + (this.rand.nextDouble() - 0.5D) * 0.4D;
			double particleY = this.posY + (double)this.height + 0.3D + (this.rand.nextDouble() - 0.5D) * 0.3D;
			double particleZ = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.4D;

			// fireball
			if (castType == 1) {
				if (castTimer > 10) {
					double forwardX = this.getLookVec().x * 0.45D;
					double forwardZ = this.getLookVec().z * 0.45D;
					double fireX = this.posX + forwardX + (this.rand.nextDouble() - 0.5D) * 0.4D;
					double fireY = this.posY + (double)this.height + 0.65D + (this.rand.nextDouble() - 0.5D) * 0.3D;
					double fireZ = this.posZ + forwardZ + (this.rand.nextDouble() - 0.5D) * 0.4D;

					if (this.world.isRemote) {
						for (int i = 0; i < 2; i++) {
							this.world.spawnParticle(EnumParticleTypes.FLAME, fireX, fireY, fireZ, 0.0D, 0.02D, 0.0D);
						}
						if (this.rand.nextBoolean()) {
							this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, fireX, fireY, fireZ, 0.0D, 0.01D, 0.0D);
						}
					} else if (this.world instanceof WorldServer && castTimer % 3 == 0) {
						((WorldServer) this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX + forwardX, this.posY + (double)this.height + 0.65D, this.posZ + forwardZ, 3, 0.15D, 0.15D, 0.15D, 0.01D);
					}
				} else if (castTimer == 10 && !this.world.isRemote) {
					if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
						this.shootFireOrbAt(this.getAttackTarget());
					}
				}
				// blood totem
			} else if (castType == 2) {
				if (this.world.isRemote) {
					for (int i = 0; i < 2; i++) {
						this.world.spawnParticle(EnumParticleTypes.REDSTONE, particleX, particleY, particleZ, 0.0D, 0.0D, 0.0D);
					}
					if (this.rand.nextBoolean()) {
						TracesOfTheFallen.proxy.spawnBloodDropParticle(this.world, particleX, particleY, particleZ, (this.rand.nextDouble() - 0.5D) * 0.02D, -0.03D, (this.rand.nextDouble() - 0.5D) * 0.02D);
					}
				} else if (this.world instanceof WorldServer && castTimer % 3 == 0) {
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + (double)this.height + 0.3D, this.posZ, 4, 0.2D, 0.2D, 0.2D, 0.0D);
				}
				if (castTimer == 1 && !this.world.isRemote) {
					this.summonBloodTotem();
				}
				// chill pulse
			} else if (castType == 3) {
				if (this.world.isRemote) {
					for (int i = 0; i < 3; i++) {
						double radius = 1.5D + this.rand.nextDouble() * 1.5D;
						double angle = this.rand.nextDouble() * Math.PI * 2.0D;
						double px = this.posX + Math.cos(angle) * radius;
						double py = this.posY + this.rand.nextDouble() * 2.0D;
						double pz = this.posZ + Math.sin(angle) * radius;
						double mx = (this.posX - px) * 0.05D;
						double my = 0.03D + this.rand.nextDouble() * 0.02D;
						double mz = (this.posZ - pz) * 0.05D;
						TracesOfTheFallen.proxy.spawnChillParticle(this.world, px, py, pz, mx, my, mz);
					}
					if (castTimer == 1) {
						for (int i = 0; i < 120; i++) {
							double angle = this.rand.nextDouble() * Math.PI * 2.0D;
							double speed = 0.15D + this.rand.nextDouble() * 0.45D;
							double mx = Math.cos(angle) * speed;
							double mz = Math.sin(angle) * speed;
							double my = (this.rand.nextDouble() - 0.5D) * 0.2D;
							TracesOfTheFallen.proxy.spawnChillParticle(this.world, this.posX, this.posY + 1.0D, this.posZ, mx, my, mz);
						}
					}
				}
				if (castTimer == 1 && !this.world.isRemote) {
					this.releaseChillPulse();
				}
			} else if (castType == 4) {
				if (this.world.isRemote && this.rand.nextBoolean()) {
					this.world.spawnParticle(EnumParticleTypes.SPELL_INSTANT, this.posX + (this.rand.nextDouble() - 0.5D) * 1.0D, this.posY + 1.0D + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - 0.5D) * 1.0D, 0.0D, 0.02D, 0.0D);
				}
				if (!this.world.isRemote && castTimer % 5 == 0) {
					this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 0.8F, 1.5F + this.rand.nextFloat() * 0.3F);
				}
				if (castTimer == 1 && !this.world.isRemote) {
					this.releaseBoneRattle();
				}
			} else if (castType == 5) {
				if (this.world.isRemote && this.rand.nextBoolean()) {
					TracesOfTheFallen.proxy.spawnCyanCinderParticle(this.world, this.posX + (this.rand.nextDouble() - 0.5D) * 1.0D, this.posY + 1.0D + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - 0.5D) * 1.0D, 0.0D, 0.02D, 0.0D);
				}
				if (castTimer == 1 && !this.world.isRemote) {
					this.releaseWillOWisp();
				}
			} else if (castType == 6) {
				if (this.world.isRemote && this.rand.nextBoolean()) {
					this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - 0.5D) * 1.0D, this.posY + 1.0D + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - 0.5D) * 1.0D, 0.0D, 0.05D, 0.0D);
				}
				if (castTimer == 1 && !this.world.isRemote) {
					this.releaseSeekingOrb();
				}
			}
		}
		if (!this.world.isRemote) {
			if (this.totemCooldown > 0) {
				this.totemCooldown--;
			}
			if (this.fireOrbCooldown > 0) {
				this.fireOrbCooldown--;
			}
			if (this.chillPulseCooldown > 0) {
				this.chillPulseCooldown--;
			}
			if (this.boneRattleCooldown > 0) {
				this.boneRattleCooldown--;
			}
			if (this.willOWispCooldown > 0) {
				this.willOWispCooldown--;
			}
			if (this.seekingOrbCooldown > 0) {
				this.seekingOrbCooldown--;
			}
			if (castTimer == 0 && !this.getHeldItemMainhand().isEmpty() && (this.getHeldItemMainhand().getItem() == Items.BONE || this.getHeldItemMainhand().getItem() == ModItems.bone_rattle)) {
				this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
			}
			if (this.getAttackTarget() != null && !this.isDancing()) {
				EntityLivingBase target = this.getAttackTarget();
				if (target.isEntityAlive() && !this.isOwner(target)) {
					if (this.boneRattleCooldown == 0 && castTimer == 0) {
						List<EntityGoblin> nearbyGoblins = this.world.getEntitiesWithinAABB(EntityGoblin.class, this.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D));
						boolean needsSpeed = false;
						for (EntityGoblin gob : nearbyGoblins) {
							if (gob.isEntityAlive() && !gob.isPotionActive(MobEffects.SPEED)) {
								needsSpeed = true;
								break;
							}
						}
						if (needsSpeed && this.rand.nextInt(15) == 0) {
							this.setSpellCastType(4);
							this.setSpellCastingTimer(70);
							this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.bone_rattle));
							this.boneRattleCooldown = 300 + this.rand.nextInt(150);
							this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 1.0F, 1.5F);
						}
					}
					if (this.chillPulseCooldown == 0 && castTimer == 0) {
						List<EntityLivingBase> nearbyEnemies = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
						boolean enemyNearby = false;
						for (EntityLivingBase entity : nearbyEnemies) {
							if (entity.isEntityAlive() && entity != this && this.getDistanceSq(entity) <= 64.0D) {
								if (!this.isOwner(entity) && !(entity instanceof EntityGoblin)) {
									if (this.getEntitySenses().canSee(entity)) {
										enemyNearby = true;
										break;
									}
								}
							}
						}
						if (enemyNearby && this.rand.nextInt(10) == 0) {
							this.setSpellCastType(3);
							this.setSpellCastingTimer(40);
							this.chillPulseCooldown = 200 + this.rand.nextInt(100);
							this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SNOWMAN_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.5F);
						}
					}
					if (this.willOWispCooldown == 0 && castTimer == 0) {
						double distSq = this.getDistanceSq(target);
						if (distSq > 4.0D && distSq < 256.0D && this.getEntitySenses().canSee(target)) {
							if (this.rand.nextInt(3) == 0) {
								this.setSpellCastType(5);
								this.setSpellCastingTimer(35);
								this.willOWispCooldown = 120 + this.rand.nextInt(80);
								this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 1.0F, 1.5F);
							}
						}
					}
					if (this.seekingOrbCooldown == 0 && castTimer == 0) {
						double distSq = this.getDistanceSq(target);
						if (distSq > 4.0D && distSq < 256.0D && this.getEntitySenses().canSee(target)) {
							if (this.rand.nextInt(4) == 0) {
								this.setSpellCastType(6);
								this.setSpellCastingTimer(40);
								this.seekingOrbCooldown = 180 + this.rand.nextInt(120);
								this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.2F);
							}
						}
					}
					if (this.fireOrbCooldown == 0 && castTimer == 0) {
						double distSq = this.getDistanceSq(target);
						if (distSq > 1.0D && distSq < 256.0D && this.getEntitySenses().canSee(target)) {
							this.setSpellCastType(1);
							this.setSpellCastingTimer(30);
							this.fireOrbCooldown = 50 + this.rand.nextInt(40);
							this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.HOSTILE, 1.0F, 1.0F + this.rand.nextFloat() * 0.5F);
						}
					}
				}
				if (this.totemCooldown == 0 && castTimer == 0) {
					double distSq = this.getDistanceSq(this.getAttackTarget());
					if (distSq < 256.0D && this.onGround && this.rand.nextInt(30) == 0) {
						List<EntityBloodTotem> totems = this.world.getEntitiesWithinAABB(EntityBloodTotem.class, this.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D));
						boolean hasTotemNearby = false;
						for (EntityBloodTotem t : totems) {
							if (t.isEntityAlive() && this.getDistanceSq(t) <= 256.0D) {
								hasTotemNearby = true;
								break;
							}
						}
						if (!hasTotemNearby) {
							this.setSpellCastType(2);
							this.setSpellCastingTimer(70);
							this.totemCooldown = 300 + this.rand.nextInt(200);
							this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.2F);
						} else {
							this.totemCooldown = 60;
						}
					}
				}
			}
		}
	}

	private void releaseChillPulse() {
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.0F, 0.6F + this.rand.nextFloat() * 0.2F);
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.HOSTILE, 1.2F, 0.5F);
		if (this.world instanceof WorldServer) {
			((WorldServer) this.world).spawnParticle(EnumParticleTypes.SNOW_SHOVEL, this.posX, this.posY + 1.0D, this.posZ, 60, 4.5D, 0.6D, 4.5D, 0.05D);
		}
		List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityLivingBase target : targets) {
			if (target.isEntityAlive() && target != this && this.getDistanceSq(target) <= 64.0D) {
				if (!this.isOwner(target) && !(target instanceof EntityGoblin)) {
					target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 420, 1));
				}
			}
		}
	}

	private void releaseBoneRattle() {
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 1.2F, 0.8F + this.rand.nextFloat() * 0.2F);
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 1.2F);
		if (this.world instanceof WorldServer) {
			((WorldServer) this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + 1.0D, this.posZ, 20, 1.5D, 0.5D, 1.5D, 0.02D);
		}
		List<EntityGoblin> targets = this.world.getEntitiesWithinAABB(EntityGoblin.class, this.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D));
		for (EntityGoblin target : targets) {
			if (target.isEntityAlive() && this.getDistanceSq(target) <= 144.0D) {
				target.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 0));
			}
		}
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
	}

	private void summonBloodTotem() {
		double radYaw = Math.toRadians(this.rotationYawHead);
		double forwardX = -Math.sin(radYaw) * 1.5D;
		double forwardZ = Math.cos(radYaw) * 1.5D;
		double totemX = this.posX + forwardX;
		double totemZ = this.posZ + forwardZ;
		EntityBloodTotem totem = new EntityBloodTotem(this.world, totemX, this.posY, totemZ);
		totem.rotationYaw = this.rotationYaw;
		totem.prevRotationYaw = this.rotationYaw;
		this.world.spawnEntity(totem);
		this.world.playSound(null, totemX, this.posY, totemZ, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 1.0F, 1.0F);
		if (this.world instanceof WorldServer) {
			((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, totemX, this.posY + 0.5D, totemZ, 15, 0.3D, 0.5D, 0.3D, 0.0D);
		}
	}

	private void shootFireOrbAt(EntityLivingBase target) {
		EntityFireOrb fireOrb = new EntityFireOrb(this.world, this);
		double d0 = target.posX - this.posX;
		double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - fireOrb.posY;
		double d2 = target.posZ - this.posZ;
		double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
		fireOrb.shoot(d0, d1 + d3 * 0.1D, d2, 1.2F, 2.0F);
		this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		if (this.world instanceof WorldServer) {
			((WorldServer) this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + this.getEyeHeight(), this.posZ, 10, 0.2D, 0.2D, 0.2D, 0.05D);
		}
		this.world.spawnEntity(fireOrb);
	}

	private void releaseWillOWisp() {
		EntityLivingBase target = this.getAttackTarget();
		if (target != null) {
			EntityWillOWisp wisp = new EntityWillOWisp(this.world, this, target);
			double d0 = target.posX - this.posX;
			double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
			double d2 = target.posZ - this.posZ;
			double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
			wisp.shoot(d0, d1 + d3 * 0.1D, d2, 0.35F, 3.0F);
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITCH_THROW, SoundCategory.HOSTILE, 1.0F, 1.3F + this.rand.nextFloat() * 0.3F);
			if (this.world instanceof WorldServer) {
				for (int i = 0; i < 15; i++) {
					double px = this.posX + (this.rand.nextDouble() - 0.5D) * 0.5D;
					double py = this.posY + this.getEyeHeight() + (this.rand.nextDouble() - 0.5D) * 0.5D;
					double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.5D;
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, 0.05D, 0.9D, 1.0D, 1.0D);
				}
			}
			this.world.spawnEntity(wisp);
		}
	}

	private void releaseSeekingOrb() {
		EntityLivingBase target = this.getAttackTarget();
		if (target != null) {
			EntitySeekingOrb orb = new EntitySeekingOrb(this.world, this, target);
			double d0 = target.posX - this.posX;
			double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
			double d2 = target.posZ - this.posZ;
			double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
			orb.shoot(d0, d1 + d3 * 0.1D, d2, 0.05F, 2.0F);
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.HOSTILE, 1.0F, 1.0F + this.rand.nextFloat() * 0.3F);
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + this.getEyeHeight(), this.posZ, 20, 0.3D, 0.3D, 0.3D, 0.1D);
			}
			this.world.spawnEntity(orb);
		}
	}

	public boolean isDancing() {
		return this.getDanceType() > 0;
	}

	public void setDancing(boolean dancing) {
		this.setDanceType(dancing ? 1 : 0);
	}

	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		super.dropFewItems(wasRecentlyHit, lootingModifier);
		if (wasRecentlyHit && this.rand.nextInt(Math.max(1, 10 - lootingModifier * 2)) == 0) {
			this.dropItem(ModItems.bone_rattle, 1);
		}
	}
}
