package com.windanesz.tracesofthefallen.entity.shaman;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.*;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;

import java.util.*;

/**
 * Registry and repository for all {@link ShamanSpell} instances castable by {@link EntityGoblinShaman}.
 */
public class ShamanSpells {
	private static final Map<Integer, ShamanSpell> SPELLS_BY_ID = new HashMap<>();
	private static final List<ShamanSpell> ALL_SPELLS = new ArrayList<>();

	public static boolean isAlly(EntityGoblinShaman shaman, net.minecraft.entity.Entity entity) {
		if (entity == null || entity == shaman) return true;
		if (shaman.isOwner(entity)) return true;
		if (entity instanceof EntityGoblin) return true;
		if (entity instanceof EntityGoblinSkeleton) return true;
		if (entity instanceof com.windanesz.tracesofthefallen.entity.EntityTinybones) return true;
		if (entity instanceof com.windanesz.tracesofthefallen.entity.EntityMinecrawler) return true;
		if (entity instanceof com.windanesz.tracesofthefallen.entity.EntitySpecter || entity instanceof com.windanesz.tracesofthefallen.entity.EntityFamiliarSpecter) return true;
		if (entity instanceof EntityBloodTotem) return true;
		if (entity instanceof net.minecraft.entity.IEntityOwnable && shaman.getOwner() != null && shaman.getOwner().equals(((net.minecraft.entity.IEntityOwnable) entity).getOwner())) return true;
		return false;
	}

	public static final ShamanSpell FIRE_ORB = register(new ShamanSpell("Fireball", 1, SpellSchool.PYROMANCY, 2, 30) {
		@Override
		public boolean stopsMovement() {
			return false;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.shouldStartCasting(shaman, target) ? 1.0F : 0.0F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
				return false;
			}
			if (target == null || !target.isEntityAlive() || isAlly(shaman, target)) {
				return false;
			}
			double distSq = shaman.getDistanceSq(target);
			return distSq > 1.0D && distSq < 256.0D && shaman.getEntitySenses().canSee(target);
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.HOSTILE, 1.0F, 1.0F + shaman.getRNG().nextFloat() * 0.5F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (castTimer > 10) {
				double forwardX = shaman.getLookVec().x * 0.45D;
				double forwardZ = shaman.getLookVec().z * 0.45D;
				double fireX = shaman.posX + forwardX + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;
				double fireY = shaman.posY + (double)shaman.height + 0.65D + (shaman.getRNG().nextDouble() - 0.5D) * 0.3D;
				double fireZ = shaman.posZ + forwardZ + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;

				if (shaman.world.isRemote) {
					for (int i = 0; i < 2; i++) {
						shaman.world.spawnParticle(EnumParticleTypes.FLAME, fireX, fireY, fireZ, 0.0D, 0.02D, 0.0D);
					}
					if (shaman.getRNG().nextBoolean()) {
						shaman.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, fireX, fireY, fireZ, 0.0D, 0.01D, 0.0D);
					}
				} else if (shaman.world instanceof WorldServer && castTimer % 3 == 0) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.FLAME, shaman.posX + forwardX, shaman.posY + (double)shaman.height + 0.65D, shaman.posZ + forwardZ, 3, 0.15D, 0.15D, 0.15D, 0.01D);
				}
			} else if (castTimer == 10 && !shaman.world.isRemote) {
				if (target != null && target.isEntityAlive()) {
					EntityFireOrb fireOrb = new EntityFireOrb(shaman.world, shaman);
					double d0 = target.posX - shaman.posX;
					double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - fireOrb.posY;
					double d2 = target.posZ - shaman.posZ;
					double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
					fireOrb.shoot(d0, d1 + d3 * 0.1D, d2, 1.2F, 2.0F);
					shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0F, (shaman.getRNG().nextFloat() - shaman.getRNG().nextFloat()) * 0.2F + 1.0F);
					if (shaman.world instanceof WorldServer) {
						((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.FLAME, shaman.posX, shaman.posY + shaman.getEyeHeight(), shaman.posZ, 10, 0.2D, 0.2D, 0.2D, 0.05D);
					}
					shaman.world.spawnEntity(fireOrb);
				}
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			// Projectile shot at castTimer == 10 during tick loop
		}
	});

	public static final ShamanSpell BLOOD_TOTEM = register(new ShamanSpell("Blood Totem", 2, SpellSchool.SUPPORT, 2, 70) {
		@Override
		public boolean stopsMovement() {
			return true;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.shouldStartCasting(shaman, target)) return 0.0F;
			return shaman.getHealth() < shaman.getMaxHealth() * 0.6F ? 3.0F : 1.5F;
		}

		@Override
		public boolean canStartCastingIgnoringRng(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!super.canStartCastingIgnoringRng(shaman, target)) {
				return false;
			}
			List<EntityBloodTotem> totems = shaman.world.getEntitiesWithinAABB(EntityBloodTotem.class, shaman.getEntityBoundingBox().grow(32.0D, 16.0D, 32.0D));
			for (EntityBloodTotem t : totems) {
				if (t.isEntityAlive() && shaman.getDistanceSq(t) <= 1024.0D) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.canStartCastingIgnoringRng(shaman, target)) {
				if (shaman.getCooldown(this) <= 0) {
					shaman.setCooldown(this, 100);
				}
				return false;
			}
			return shaman.onGround && shaman.getRNG().nextInt(20) == 0;
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 0.8F + shaman.getRNG().nextFloat() * 0.2F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			double particleX = shaman.posX + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;
			double particleY = shaman.posY + (double)shaman.height + 0.3D + (shaman.getRNG().nextDouble() - 0.5D) * 0.3D;
			double particleZ = shaman.posZ + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;

			if (shaman.world.isRemote) {
				for (int i = 0; i < 2; i++) {
					shaman.world.spawnParticle(EnumParticleTypes.REDSTONE, particleX, particleY, particleZ, 0.0D, 0.0D, 0.0D);
				}
				if (shaman.getRNG().nextBoolean()) {
					TracesOfTheFallen.proxy.spawnBloodDropParticle(shaman.world, particleX, particleY, particleZ, (shaman.getRNG().nextDouble() - 0.5D) * 0.02D, -0.03D, (shaman.getRNG().nextDouble() - 0.5D) * 0.02D);
				}
			} else if (shaman.world instanceof WorldServer && castTimer % 3 == 0) {
				((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.REDSTONE, shaman.posX, shaman.posY + (double)shaman.height + 0.3D, shaman.posZ, 4, 0.2D, 0.2D, 0.2D, 0.0D);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				double radYaw = Math.toRadians(shaman.rotationYawHead);
				double forwardX = -Math.sin(radYaw) * 1.5D;
				double forwardZ = Math.cos(radYaw) * 1.5D;
				double totemX = shaman.posX + forwardX;
				double totemZ = shaman.posZ + forwardZ;
				EntityBloodTotem totem = new EntityBloodTotem(shaman.world, totemX, shaman.posY, totemZ);
				totem.rotationYaw = shaman.rotationYaw;
				totem.prevRotationYaw = shaman.rotationYaw;
				shaman.world.spawnEntity(totem);
				shaman.world.playSound(null, totemX, shaman.posY, totemZ, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 1.0F, 1.0F);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.REDSTONE, totemX, shaman.posY + 0.5D, totemZ, 15, 0.3D, 0.5D, 0.3D, 0.0D);
				}
			}
		}
	});

	public static final ShamanSpell CHILL_PULSE = register(new ShamanSpell("Chill Pulse", 3, SpellSchool.SOUL_FLAME, 1, 40) {
		@Override
		public boolean stopsMovement() {
			return true;
		}

		@Override
		public boolean canStartCastingIgnoringRng(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!super.canStartCastingIgnoringRng(shaman, target) || target == null || shaman.getDistanceSq(target) >= 25.0D) {
				return false;
			}
			List<EntityLivingBase> nearbyEnemies = shaman.world.getEntitiesWithinAABB(EntityLivingBase.class, shaman.getEntityBoundingBox().grow(5.0D, 3.0D, 5.0D));
			for (EntityLivingBase entity : nearbyEnemies) {
				if (entity.isEntityAlive() && entity != shaman && shaman.getDistanceSq(entity) < 25.0D && !isAlly(shaman, entity) && shaman.getEntitySenses().canSee(entity)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.shouldStartCasting(shaman, target)) return 0.0F;
			List<EntityLivingBase> nearby = shaman.world.getEntitiesWithinAABB(EntityLivingBase.class, shaman.getEntityBoundingBox().grow(6.0D, 4.0D, 6.0D));
			int enemyCount = 0;
			for (EntityLivingBase entity : nearby) {
				if (entity.isEntityAlive() && entity != shaman && shaman.getDistanceSq(entity) <= 36.0D && !isAlly(shaman, entity)) {
					enemyCount++;
				}
			}
			return enemyCount >= 2 ? 4.0F : 2.0F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.canStartCastingIgnoringRng(shaman, target)) {
				return false;
			}
			return shaman.getRNG().nextInt(8) == 0;
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_SNOWMAN_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.5F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (shaman.world.isRemote) {
				if (castTimer > 1) {
					for (int i = 0; i < 3; i++) {
						double radius = 1.5D + shaman.getRNG().nextDouble() * 1.5D;
						double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
						double px = shaman.posX + Math.cos(angle) * radius;
						double py = shaman.posY + shaman.getRNG().nextDouble() * 2.0D;
						double pz = shaman.posZ + Math.sin(angle) * radius;
						double mx = (shaman.posX - px) * 0.05D;
						double my = 0.03D + shaman.getRNG().nextDouble() * 0.02D;
						double mz = (shaman.posZ - pz) * 0.05D;
						TracesOfTheFallen.proxy.spawnChillParticle(shaman.world, px, py, pz, mx, my, mz);
					}
					for (int i = 0; i < 4; i++) {
						double ringAngle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
						double ringX = shaman.posX + Math.cos(ringAngle) * 6.0D;
						double ringZ = shaman.posZ + Math.sin(ringAngle) * 6.0D;
						double ringY = shaman.posY + 0.1D + shaman.getRNG().nextDouble() * 0.3D;
						TracesOfTheFallen.proxy.spawnChillParticle(shaman.world, ringX, ringY, ringZ, 0.0D, 0.02D, 0.0D);
						if (shaman.getRNG().nextBoolean()) {
							shaman.world.spawnParticle(EnumParticleTypes.SNOWBALL, ringX, ringY, ringZ, 0.0D, 0.01D, 0.0D);
						}
					}
				} else if (castTimer == 1) {
					for (int i = 0; i < 120; i++) {
						double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
						double speed = 0.15D + shaman.getRNG().nextDouble() * 0.45D;
						double mx = Math.cos(angle) * speed;
						double mz = Math.sin(angle) * speed;
						double my = (shaman.getRNG().nextDouble() - 0.5D) * 0.2D;
						TracesOfTheFallen.proxy.spawnChillParticle(shaman.world, shaman.posX, shaman.posY + 1.0D, shaman.posZ, mx, my, mz);
					}
				}
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.0F, 0.6F + shaman.getRNG().nextFloat() * 0.2F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.HOSTILE, 1.2F, 0.5F);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SNOW_SHOVEL, shaman.posX, shaman.posY + 1.0D, shaman.posZ, 60, 3.5D, 0.6D, 3.5D, 0.05D);
				}
				List<EntityLivingBase> targets = shaman.world.getEntitiesWithinAABB(EntityLivingBase.class, shaman.getEntityBoundingBox().grow(6.0D, 4.0D, 6.0D));
				for (EntityLivingBase entity : targets) {
					if (entity.isEntityAlive() && entity != shaman && shaman.getDistanceSq(entity) <= 36.0D) {
						if (!isAlly(shaman, entity)) {
							entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(shaman, shaman), (float) Settings.miscSettings.shamanChillPulseDamage);
							entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 420, 1));
						}
					}
				}
			}
		}
	});

	public static final ShamanSpell BONE_RATTLE = register(new ShamanSpell("Bone Rattle", 4, SpellSchool.SUPPORT, 1, 70) {
		@Override
		public boolean stopsMovement() {
			return false;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.shouldStartCasting(shaman, target) ? 2.5F : 0.0F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
				return false;
			}
			if (target == null || shaman.getRNG().nextInt(15) != 0) {
				return false;
			}
			List<EntityGoblin> nearbyGoblins = shaman.world.getEntitiesWithinAABB(EntityGoblin.class, shaman.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D));
			for (EntityGoblin gob : nearbyGoblins) {
				if (gob.isEntityAlive() && !gob.isPotionActive(MobEffects.SPEED)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.bone_rattle));
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 1.0F, 1.5F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (shaman.world.isRemote && shaman.getRNG().nextBoolean()) {
				shaman.world.spawnParticle(EnumParticleTypes.SPELL_INSTANT, shaman.posX + (shaman.getRNG().nextDouble() - 0.5D) * 1.0D, shaman.posY + 1.0D + shaman.getRNG().nextDouble(), shaman.posZ + (shaman.getRNG().nextDouble() - 0.5D) * 1.0D, 0.0D, 0.02D, 0.0D);
			}
			if (!shaman.world.isRemote && castTimer % 5 == 0) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 0.8F, 1.5F + shaman.getRNG().nextFloat() * 0.3F);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 1.2F, 0.8F + shaman.getRNG().nextFloat() * 0.2F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 1.2F);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, shaman.posX, shaman.posY + 1.0D, shaman.posZ, 20, 1.5D, 0.5D, 1.5D, 0.02D);
				}
				List<EntityGoblin> targets = shaman.world.getEntitiesWithinAABB(EntityGoblin.class, shaman.getEntityBoundingBox().grow(12.0D, 6.0D, 12.0D));
				for (EntityGoblin gob : targets) {
					if (gob.isEntityAlive() && shaman.getDistanceSq(gob) <= 144.0D) {
						gob.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 0));
					}
				}
				shaman.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
			}
		}
	});

	public static final ShamanSpell WILL_O_WISP = register(new ShamanSpell("Lifesteal Will-O'-Wisp", 5, SpellSchool.SOUL_FLAME, 2, 35) {
		@Override
		public boolean stopsMovement() {
			return false;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.shouldStartCasting(shaman, target) ? 1.2F : 0.0F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
				return false;
			}
			if (target == null || !target.isEntityAlive() || isAlly(shaman, target) || shaman.getRNG().nextInt(3) != 0) {
				return false;
			}
			double distSq = shaman.getDistanceSq(target);
			return distSq > 1.0D && distSq < 256.0D && shaman.getEntitySenses().canSee(target);
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 1.0F, 1.5F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (castTimer > 10) {
				double forwardX = shaman.getLookVec().x * 0.45D;
				double forwardZ = shaman.getLookVec().z * 0.45D;
				double wispX = shaman.posX + forwardX + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;
				double wispY = shaman.posY + (double)shaman.height + 0.65D + (shaman.getRNG().nextDouble() - 0.5D) * 0.3D;
				double wispZ = shaman.posZ + forwardZ + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;

				if (shaman.world.isRemote) {
					for (int i = 0; i < 2; i++) {
						TracesOfTheFallen.proxy.spawnCyanCinderParticle(shaman.world, wispX, wispY, wispZ, (shaman.getRNG().nextDouble() - 0.5D) * 0.04D, 0.02D + shaman.getRNG().nextDouble() * 0.02D, (shaman.getRNG().nextDouble() - 0.5D) * 0.04D);
					}
					shaman.world.spawnParticle(EnumParticleTypes.REDSTONE, wispX, wispY, wispZ, 0.05D, 0.9D, 1.0D);
				} else if (shaman.world instanceof WorldServer && castTimer % 2 == 0) {
					for (int i = 0; i < 3; i++) {
						double px = shaman.posX + forwardX + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;
						double py = shaman.posY + (double)shaman.height + 0.65D + (shaman.getRNG().nextDouble() - 0.5D) * 0.3D;
						double pz = shaman.posZ + forwardZ + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;
						((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, 0.05D, 0.9D, 1.0D, 1.0D);
					}
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, shaman.posX + forwardX, shaman.posY + (double)shaman.height + 0.65D, shaman.posZ + forwardZ, 2, 0.15D, 0.15D, 0.15D, 0.0D);
				}
			} else if (castTimer == 10 && !shaman.world.isRemote) {
				if (target != null && target.isEntityAlive()) {
					EntityWillOWisp wisp = new EntityWillOWisp(shaman.world, shaman, target);
					double d0 = target.posX - shaman.posX;
					double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (shaman.posY + (double)(shaman.height / 2.0F));
					double d2 = target.posZ - shaman.posZ;
					double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
					wisp.shoot(d0, d1 + d3 * 0.1D, d2, 0.35F, 3.0F);
					shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_WITCH_THROW, SoundCategory.HOSTILE, 1.0F, 1.3F + shaman.getRNG().nextFloat() * 0.3F);
					if (shaman.world instanceof WorldServer) {
						for (int i = 0; i < 15; i++) {
							double px = shaman.posX + (shaman.getRNG().nextDouble() - 0.5D) * 0.5D;
							double py = shaman.posY + shaman.getEyeHeight() + (shaman.getRNG().nextDouble() - 0.5D) * 0.5D;
							double pz = shaman.posZ + (shaman.getRNG().nextDouble() - 0.5D) * 0.5D;
							((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, 0.05D, 0.9D, 1.0D, 1.0D);
						}
					}
					shaman.world.spawnEntity(wisp);
				}
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			// Projectile shot at castTimer == 10 during tick loop
		}
	});

	public static final ShamanSpell SEEKING_ORB = register(new ShamanSpell("Seeking Orb", 6, SpellSchool.ANIMISM, 3, 40) {
		@Override
		public boolean stopsMovement() {
			return false;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.shouldStartCasting(shaman, target)) return 0.0F;
			double distSq = target != null ? shaman.getDistanceSq(target) : 0.0D;
			return distSq > 64.0D ? 2.0F : 1.3F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
				return false;
			}
			if (target == null || !target.isEntityAlive() || isAlly(shaman, target) || shaman.getRNG().nextInt(4) != 0) {
				return false;
			}
			double distSq = shaman.getDistanceSq(target);
			return distSq > 1.0D && distSq < 256.0D && shaman.getEntitySenses().canSee(target);
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 1.0F, 0.8F + shaman.getRNG().nextFloat() * 0.2F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (shaman.world.isRemote && shaman.getRNG().nextBoolean()) {
				shaman.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, shaman.posX + (shaman.getRNG().nextDouble() - 0.5D) * 1.0D, shaman.posY + 1.0D + shaman.getRNG().nextDouble(), shaman.posZ + (shaman.getRNG().nextDouble() - 0.5D) * 1.0D, 0.0D, 0.05D, 0.0D);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote && target != null) {
				EntitySeekingOrb orb = new EntitySeekingOrb(shaman.world, shaman, target);
				double d0 = target.posX - shaman.posX;
				double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (shaman.posY + (double)(shaman.height / 2.0F));
				double d2 = target.posZ - shaman.posZ;
				double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
				orb.shoot(d0, d1 + d3 * 0.1D, d2, 0.05F, 2.0F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.HOSTILE, 1.0F, 1.0F + shaman.getRNG().nextFloat() * 0.3F);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, shaman.posX, shaman.posY + shaman.getEyeHeight(), shaman.posZ, 20, 0.3D, 0.3D, 0.3D, 0.1D);
				}
				shaman.world.spawnEntity(orb);
			}
		}
	});

	// ====================================================================================================
	// PLACEHOLDER SPELLS FOR 3-TIER SCHOOL PROGRESSION
	// ====================================================================================================

	/**
	 * Pyromancy Tier 1: Heat Wave (Placeholder)
	 * Future implementation: Short-range erupting wave of intense heat and embers that pushes back nearby attackers and ignites them.
	 */
	public static final ShamanSpell HEAT_WAVE = register(new ShamanSpell("Heat Wave", 7, SpellSchool.PYROMANCY, 1, 45) {
		@Override
		public boolean canStartCastingIgnoringRng(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!super.canStartCastingIgnoringRng(shaman, target) || target == null || shaman.getDistanceSq(target) >= 25.0D) {
				return false;
			}
			List<EntityLivingBase> nearbyEnemies = shaman.world.getEntitiesWithinAABB(EntityLivingBase.class, shaman.getEntityBoundingBox().grow(5.0D, 2.5D, 5.0D));
			for (EntityLivingBase entity : nearbyEnemies) {
				if (entity.isEntityAlive() && entity != shaman && shaman.getDistanceSq(entity) < 25.0D && !isAlly(shaman, entity) && shaman.getEntitySenses().canSee(entity)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.shouldStartCasting(shaman, target)) return 0.0F;
			List<EntityLivingBase> nearby = shaman.world.getEntitiesWithinAABB(EntityLivingBase.class, shaman.getEntityBoundingBox().grow(5.0D, 2.5D, 5.0D));
			int enemyCount = 0;
			for (EntityLivingBase entity : nearby) {
				if (entity.isEntityAlive() && entity != shaman && shaman.getDistanceSq(entity) <= 25.0D && !isAlly(shaman, entity)) {
					enemyCount++;
				}
			}
			return enemyCount >= 2 ? 3.8F : 1.8F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!this.canStartCastingIgnoringRng(shaman, target)) {
				return false;
			}
			return shaman.getRNG().nextInt(8) == 0;
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0F, 0.6F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (shaman.world.isRemote) {
				if (castTimer > 15) {
					for (int i = 0; i < 3; i++) {
						double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
						double radius = 0.8D + shaman.getRNG().nextDouble() * 0.7D;
						double px = shaman.posX + Math.cos(angle) * radius;
						double py = shaman.posY + shaman.getRNG().nextDouble() * 1.5D;
						double pz = shaman.posZ + Math.sin(angle) * radius;
						shaman.world.spawnParticle(EnumParticleTypes.FLAME, px, py, pz, 0.0D, 0.05D + shaman.getRNG().nextDouble() * 0.03D, 0.0D);
						if (shaman.getRNG().nextBoolean()) {
							shaman.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, 0.0D, 0.03D, 0.0D);
						}
					}
					for (int i = 0; i < 3; i++) {
						double ringAngle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
						double ringX = shaman.posX + Math.cos(ringAngle) * 5.0D;
						double ringZ = shaman.posZ + Math.sin(ringAngle) * 5.0D;
						double ringY = shaman.posY + 0.1D + shaman.getRNG().nextDouble() * 0.3D;
						shaman.world.spawnParticle(EnumParticleTypes.FLAME, ringX, ringY, ringZ, 0.0D, 0.02D, 0.0D);
						if (shaman.getRNG().nextBoolean()) {
							shaman.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ringX, ringY, ringZ, 0.0D, 0.01D, 0.0D);
						}
					}
				} else if (castTimer == 15) {
					for (int i = 0; i < 120; i++) {
						double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
						double speed = 0.2D + shaman.getRNG().nextDouble() * 0.5D;
						double mx = Math.cos(angle) * speed;
						double mz = Math.sin(angle) * speed;
						double my = (shaman.getRNG().nextDouble() - 0.2D) * 0.2D;
						shaman.world.spawnParticle(EnumParticleTypes.FLAME, shaman.posX, shaman.posY + 1.0D, shaman.posZ, mx, my, mz);
						if (i % 2 == 0) {
							shaman.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, shaman.posX, shaman.posY + 1.0D, shaman.posZ, mx * 0.8D, my, mz * 0.8D);
						}
					}
				}
			} else {
				if (castTimer == 15) {
					this.onCastCompleted(shaman, target);
				} else if (castTimer > 15 && castTimer % 6 == 0) {
					shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.HOSTILE, 0.7F, 1.2F + shaman.getRNG().nextFloat() * 0.3F);
				}
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote && shaman.getSpellCastingTimer() != 1) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.2F, 0.7F + shaman.getRNG().nextFloat() * 0.2F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0F, 0.8F);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.FLAME, shaman.posX, shaman.posY + 1.0D, shaman.posZ, 40, 2.5D, 0.5D, 2.5D, 0.05D);
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, shaman.posX, shaman.posY + 1.0D, shaman.posZ, 25, 2.5D, 0.5D, 2.5D, 0.05D);
				}
				List<EntityLivingBase> targets = shaman.world.getEntitiesWithinAABB(EntityLivingBase.class, shaman.getEntityBoundingBox().grow(5.0D, 2.5D, 5.0D));
				for (EntityLivingBase entity : targets) {
					if (entity.isEntityAlive() && entity != shaman && shaman.getDistanceSq(entity) <= 25.0D) {
						if (!isAlly(shaman, entity)) {
							entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(shaman, shaman).setFireDamage(), (float) Settings.miscSettings.shamanHeatWaveDamage);
							entity.setFire(Settings.miscSettings.shamanHeatWaveIgnitionDuration);
							double dx = entity.posX - shaman.posX;
							double dz = entity.posZ - shaman.posZ;
							double dist = MathHelper.sqrt(dx * dx + dz * dz);
							if (dist > 0.001D) {
								entity.addVelocity((dx / dist) * 0.7D, 0.35D, (dz / dist) * 0.7D);
								entity.velocityChanged = true;
							}
						}
					}
				}
			}
		}
	});

	/**
	 * Pyromancy Tier 3: Magma Blast
	 * Powerful heavy molten burst or erupting lava rupture dealing high fire damage and residual burn to target area.
	 */
	public static final ShamanSpell MAGMA_BLAST = register(new ShamanSpell("Magma Blast", 8, SpellSchool.PYROMANCY, 3, 50) {
		@Override
		public boolean stopsMovement() {
			return true;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.shouldStartCasting(shaman, target) ? 5.0F : 0.0F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
				return false;
			}
			if (target == null || !target.isEntityAlive() || isAlly(shaman, target)) {
				return false;
			}
			double distSq = shaman.getDistanceSq(target);
			return distSq > 4.0D && distSq < 256.0D && shaman.getEntitySenses().canSee(target) && shaman.getRNG().nextInt(4) == 0;
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.HOSTILE, 1.2F, 0.8F + shaman.getRNG().nextFloat() * 0.2F);
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_BLAZE_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.6F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			double forwardX = shaman.getLookVec().x * 0.25D;
			double forwardZ = shaman.getLookVec().z * 0.25D;
			double castX = shaman.posX + forwardX + (shaman.getRNG().nextDouble() - 0.5D) * 0.5D;
			double castY = shaman.posY + (double) shaman.height + 0.85D + (shaman.getRNG().nextDouble() - 0.5D) * 0.4D;
			double castZ = shaman.posZ + forwardZ + (shaman.getRNG().nextDouble() - 0.5D) * 0.5D;

			if (shaman.world.isRemote) {
				for (int i = 0; i < 3; i++) {
					shaman.world.spawnParticle(EnumParticleTypes.FLAME, castX, castY, castZ, 0.0D, 0.02D, 0.0D);
				}
				if (shaman.getRNG().nextBoolean()) {
					shaman.world.spawnParticle(EnumParticleTypes.DRIP_LAVA, castX, castY, castZ, 0.0D, 0.0D, 0.0D);
				}
			} else if (shaman.world instanceof WorldServer && castTimer % 2 == 0) {
				((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.LAVA, shaman.posX + forwardX, shaman.posY + (double) shaman.height + 0.85D, shaman.posZ + forwardZ, 3, 0.3D, 0.3D, 0.3D, 0.01D);
				((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.FLAME, shaman.posX + forwardX, shaman.posY + (double) shaman.height + 0.85D, shaman.posZ + forwardZ, 4, 0.3D, 0.3D, 0.3D, 0.02D);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote && target != null && target.isEntityAlive()) {
				EntityMagmaBlast blast = new EntityMagmaBlast(shaman.world, shaman);
				double forwardX = shaman.getLookVec().x * 0.25D;
				double forwardZ = shaman.getLookVec().z * 0.25D;
				blast.setPosition(shaman.posX + forwardX, shaman.posY + (double) shaman.height + 0.85D, shaman.posZ + forwardZ);
				blast.launchLobTo(target);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.5F, 0.6F + shaman.getRNG().nextFloat() * 0.2F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.2F, 0.8F);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.FLAME, shaman.posX + forwardX, shaman.posY + (double) shaman.height + 0.85D, shaman.posZ + forwardZ, 25, 0.4D, 0.4D, 0.4D, 0.05D);
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.LAVA, shaman.posX + forwardX, shaman.posY + (double) shaman.height + 0.85D, shaman.posZ + forwardZ, 12, 0.4D, 0.4D, 0.4D, 0.0D);
				}
				shaman.world.spawnEntity(blast);
			}
		}
	});

	/**
	 * Soul Flame Tier 3: Void Slash
	 * Launches a dark/void energy wave slicing forward, piercing multiple targets with heavy shadow damage, Wither, and Blindness.
	 */
	public static final ShamanSpell VOID_SLASH = register(new ShamanSpell("Void Slash", 9, SpellSchool.SOUL_FLAME, 3, 35) {
		@Override
		public boolean stopsMovement() {
			return true;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.shouldStartCasting(shaman, target) ? 6.0F : 0.0F;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (shaman.getCooldown(this) > 0 || shaman.getSpellCastingTimer() > 0 || !this.canShamanCast(shaman)) {
				return false;
			}
			if (target == null || !target.isEntityAlive() || isAlly(shaman, target)) {
				return false;
			}
			double distSq = shaman.getDistanceSq(target);
			return distSq > 4.0D && distSq < 256.0D && shaman.getEntitySenses().canSee(target);
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			shaman.setCooldown(this, this.getTierCooldown(shaman));
			shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.6F + shaman.getRNG().nextFloat() * 0.2F);
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (shaman.world.isRemote && shaman.getRNG().nextBoolean()) {
				shaman.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, shaman.posX + (shaman.getRNG().nextDouble() - 0.5D) * 1.2D, shaman.posY + 1.2D + shaman.getRNG().nextDouble(), shaman.posZ + (shaman.getRNG().nextDouble() - 0.5D) * 1.2D, 0.0D, 0.03D, 0.0D);
				if (shaman.getRNG().nextInt(3) == 0) {
					shaman.world.spawnParticle(EnumParticleTypes.PORTAL, shaman.posX + (shaman.getRNG().nextDouble() - 0.5D) * 1.0D, shaman.posY + 1.0D + shaman.getRNG().nextDouble(), shaman.posZ + (shaman.getRNG().nextDouble() - 0.5D) * 1.0D, (shaman.getRNG().nextDouble() - 0.5D) * 0.2D, (shaman.getRNG().nextDouble() - 0.5D) * 0.2D, (shaman.getRNG().nextDouble() - 0.5D) * 0.2D);
				}
			}
			if (!shaman.world.isRemote && castTimer % 10 == 0) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 0.5F, 1.5F);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote && target != null) {
				EntityVoidSlash slash = new EntityVoidSlash(shaman.world, shaman, target);
				slash.setPosition(shaman.posX, shaman.posY + 1.4D, shaman.posZ);
				double d0 = target.posX - shaman.posX;
				double d1 = target.getEntityBoundingBox().minY + (double) (target.height * 0.5F) - slash.posY;
				double d2 = target.posZ - shaman.posZ;
				slash.shoot(d0, d1, d2, 0.65F, 1.0F);
				shaman.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);

				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.0F, 0.8F + shaman.getRNG().nextFloat() * 0.3F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.7F);

				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, shaman.posX, shaman.posY + shaman.getEyeHeight(), shaman.posZ, 25, 0.4D, 0.4D, 0.4D, 0.15D);
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.PORTAL, shaman.posX, shaman.posY + shaman.getEyeHeight(), shaman.posZ, 15, 0.3D, 0.3D, 0.3D, 0.2D);
				}
				shaman.world.spawnEntity(slash);
			}
		}
	});

	/**
	 * Animism Tier 1: Reanimated Bones
	 * Summons 3 small reanimated skeleton construct mobs ("Reanimated Bones") to fight directly for the Shaman.
	 */
	public static final ShamanSpell REANIMATED_BONES = register(new ShamanSpell("Reanimated Bones", 10, SpellSchool.ANIMISM, 1, 40) {
		@Override
		public boolean stopsMovement() {
			return true;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return 3.5F;
		}

		@Override
		public boolean canStartCastingIgnoringRng(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!super.canStartCastingIgnoringRng(shaman, target)) {
				return false;
			}
			int ownedCount = 0;
			List<EntityGoblinSkeleton> skeletons = shaman.world.getEntitiesWithinAABB(EntityGoblinSkeleton.class, shaman.getEntityBoundingBox().grow(32.0D, 16.0D, 32.0D));
			for (EntityGoblinSkeleton skel : skeletons) {
				if (skel.isOwner(shaman)) {
					if (skel instanceof com.windanesz.tracesofthefallen.entity.EntityTinybones) {
						ownedCount += 3;
					} else {
						ownedCount++;
					}
				}
			}
			return ownedCount < 3;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.canStartCastingIgnoringRng(shaman, target);
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 0.6F);
			}
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (castTimer % 5 == 0 && shaman.world instanceof WorldServer) {
				double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
				double radius = 1.2D;
				double px = shaman.posX + Math.cos(angle) * radius;
				double pz = shaman.posZ + Math.sin(angle) * radius;
				((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_MOB, px, shaman.posY + 0.2D, pz, 3, 0.1D, 0.3D, 0.1D, 1.0D);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.HOSTILE, 1.2F, 0.8F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 1.2F);
				shaman.setCooldown(this, this.getTierCooldown(shaman));
				if (shaman.getRNG().nextInt(4) == 0) {
					com.windanesz.tracesofthefallen.entity.EntityTinybones tinybones = new com.windanesz.tracesofthefallen.entity.EntityTinybones(shaman.world);
					double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
					double dist = 1.0D + shaman.getRNG().nextDouble() * 0.8D;
					double sx = shaman.posX + Math.cos(angle) * dist;
					double sz = shaman.posZ + Math.sin(angle) * dist;
					tinybones.setLocationAndAngles(sx, shaman.posY, sz, shaman.rotationYaw, 0.0F);
					tinybones.setOwner(shaman);
					if (target != null && target.isEntityAlive()) {
						tinybones.setAttackTarget(target);
					}
					shaman.world.spawnEntity(tinybones);
					if (shaman.world instanceof WorldServer) {
						((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, sx, shaman.posY + 0.5D, sz, 30, 0.3D, 0.5D, 0.3D, 0.05D, Block.getIdFromBlock(Blocks.BONE_BLOCK));
						((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, shaman.posY + 0.5D, sz, 10, 0.2D, 0.4D, 0.2D, 0.0D);
					}
				} else {
					for (int i = 0; i < 3; i++) {
						EntityGoblinSkeleton skeleton = new EntityGoblinSkeleton(shaman.world);
						double angle = (i * (Math.PI * 2 / 3)) + shaman.getRNG().nextDouble() * 0.5D;
						double dist = 1.0D + shaman.getRNG().nextDouble() * 0.8D;
						double sx = shaman.posX + Math.cos(angle) * dist;
						double sz = shaman.posZ + Math.sin(angle) * dist;
						skeleton.setLocationAndAngles(sx, shaman.posY, sz, shaman.rotationYaw, 0.0F);
						skeleton.setOwner(shaman);
						if (target != null && target.isEntityAlive()) {
							skeleton.setAttackTarget(target);
						}
						shaman.world.spawnEntity(skeleton);
						if (shaman.world instanceof WorldServer) {
							((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, sx, shaman.posY + 0.5D, sz, 30, 0.3D, 0.5D, 0.3D, 0.05D, Block.getIdFromBlock(Blocks.BONE_BLOCK));
							((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, shaman.posY + 0.5D, sz, 10, 0.2D, 0.4D, 0.2D, 0.0D);
						}
					}
				}
			}
		}
	});

	/**
	 * Animism Tier 2: Summon Spectre
	 * Summons a friendly EntitySpecter to fight alongside the Shaman.
	 */
	public static final ShamanSpell SUMMON_SPECTRE = register(new ShamanSpell("Summon Spectre", 11, SpellSchool.ANIMISM, 2, 70) {
		@Override
		public boolean stopsMovement() {
			return true;
		}

		@Override
		public float getUtilityScore(EntityGoblinShaman shaman, EntityLivingBase target) {
			return 4.5F;
		}

		@Override
		public boolean canStartCastingIgnoringRng(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!super.canStartCastingIgnoringRng(shaman, target)) {
				return false;
			}
			int ownedCount = 0;
			List<EntitySpecter> specters = shaman.world.getEntitiesWithinAABB(EntitySpecter.class, shaman.getEntityBoundingBox().grow(32.0D, 16.0D, 32.0D));
			for (EntitySpecter specter : specters) {
				if (specter.isOwner(shaman)) {
					ownedCount++;
				}
			}
			List<EntityFamiliarSpecter> famSpecters = shaman.world.getEntitiesWithinAABB(EntityFamiliarSpecter.class, shaman.getEntityBoundingBox().grow(32.0D, 16.0D, 32.0D));
			for (EntityFamiliarSpecter specter : famSpecters) {
				if (specter.isOwner(shaman)) {
					ownedCount++;
				}
			}
			return ownedCount < 1;
		}

		@Override
		public boolean shouldStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			return this.canStartCastingIgnoringRng(shaman, target);
		}

		@Override
		public void onStartCasting(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.6F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 0.8F);
			}
		}

		@Override
		public void onCastingTick(EntityGoblinShaman shaman, EntityLivingBase target, int castTimer) {
			if (castTimer % 5 == 0 && shaman.world instanceof WorldServer) {
				double angle = shaman.getRNG().nextDouble() * Math.PI * 2.0D;
				double radius = 1.5D;
				double px = shaman.posX + Math.cos(angle) * radius;
				double pz = shaman.posZ + Math.sin(angle) * radius;
				((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, px, shaman.posY + 1.0D, pz, 5, 0.2D, 0.4D, 0.2D, 0.01D);
				((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, shaman.posY + 1.0D, pz, 3, 0.2D, 0.4D, 0.2D, 0.0D);
			}
		}

		@Override
		public void onCastCompleted(EntityGoblinShaman shaman, EntityLivingBase target) {
			if (!shaman.world.isRemote) {
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_VEX_CHARGE, SoundCategory.HOSTILE, 1.2F, 0.8F);
				shaman.world.playSound(null, shaman.posX, shaman.posY, shaman.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 1.0F, 1.0F);
				shaman.setCooldown(this, this.getTierCooldown(shaman));
				EntitySpecter specter = new EntitySpecter(shaman.world);
				double angle = shaman.rotationYaw * (Math.PI / 180.0D);
				double sx = shaman.posX - Math.sin(angle) * 1.5D;
				double sz = shaman.posZ + Math.cos(angle) * 1.5D;
				specter.setLocationAndAngles(sx, shaman.posY + 0.5D, sz, shaman.rotationYaw, 0.0F);
				specter.setOwner(shaman);
				if (target != null && target.isEntityAlive()) {
					specter.setAttackTarget(target);
				}
				shaman.world.spawnEntity(specter);
				if (shaman.world instanceof WorldServer) {
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, sx, shaman.posY + 1.0D, sz, 30, 0.4D, 0.6D, 0.4D, 0.05D);
					((WorldServer) shaman.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, shaman.posY + 1.0D, sz, 20, 0.3D, 0.5D, 0.3D, 0.0D);
				}
			}
		}
	});

	private static ShamanSpell register(ShamanSpell spell) {
		SPELLS_BY_ID.put(spell.getSpellId(), spell);
		ALL_SPELLS.add(spell);
		return spell;
	}

	public static ShamanSpell getSpellById(int spellId) {
		return SPELLS_BY_ID.get(spellId);
	}

	public static List<ShamanSpell> getAllSpells() {
		return Collections.unmodifiableList(ALL_SPELLS);
	}
}
