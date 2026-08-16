package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.ai.EntityAILampheadInteract;
import com.windanesz.tracesofthefallen.entity.ai.EntityAILampheadMeleeAttack;
import com.windanesz.tracesofthefallen.entity.ai.EntityAILampheadRegen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityLamphead extends EntityTameable {
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/lamphead");
    private static final DataParameter<Boolean> ATTACK_STANCE = EntityDataManager.createKey(EntityLamphead.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(EntityLamphead.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HEAD_SLAMMING = EntityDataManager.createKey(EntityLamphead.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> FABRICATING = EntityDataManager.createKey(EntityLamphead.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> LAMPHEAD_MODE = EntityDataManager.createKey(EntityLamphead.class, DataSerializers.VARINT);

    public int headSlamCooldown = 0;
    public float headSlamProgress = 0.0F;
    public float prevHeadSlamProgress = 0.0F;
    public int attackTimer;
    public float attackProgress;
    public float prevAttackProgress;
    public float fabricateProgress = 0.0F;
    public float prevFabricateProgress = 0.0F;
    public float squeezeProgress = 0.0F;
    public float prevSqueezeProgress = 0.0F;

    public EntityLamphead(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 1.7F);
        this.experienceValue = 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_STANCE, false);
        this.dataManager.register(ANGRY, false);
        this.dataManager.register(HEAD_SLAMMING, false);
        this.dataManager.register(FABRICATING, false);
        this.dataManager.register(LAMPHEAD_MODE, 0); // 0 = Wandering, 1 = Versatile, 2 = Static
    }

    public int getMode() {
        return this.dataManager.get(LAMPHEAD_MODE);
    }

    public void setMode(int mode) {
        this.dataManager.set(LAMPHEAD_MODE, mode);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.prevSqueezeProgress = this.squeezeProgress;
        boolean squeezing = !this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(0.0D, 0.4D, 0.0D)).isEmpty();
        if (squeezing) {
            this.squeezeProgress += 0.1F;
            if (this.squeezeProgress > 1.0F) this.squeezeProgress = 1.0F;
        } else {
            this.squeezeProgress -= 0.1F;
            if (this.squeezeProgress < 0.0F) this.squeezeProgress = 0.0F;
        }

        this.prevHeadSlamProgress = this.headSlamProgress;
        if (this.isHeadSlamming()) {
            this.headSlamProgress += 0.2F;
            if (this.headSlamProgress > 1.0F) this.headSlamProgress = 1.0F;
        } else {
            this.headSlamProgress -= 0.2F;
            if (this.headSlamProgress < 0.0F) this.headSlamProgress = 0.0F;
        }

        this.prevFabricateProgress = this.fabricateProgress;
        if (this.isFabricating()) {
            this.fabricateProgress += 0.05F; // 20 ticks to fully bend
            if (this.fabricateProgress > 1.0F) this.fabricateProgress = 1.0F;
        } else {
            this.fabricateProgress -= 0.05F;
            if (this.fabricateProgress < 0.0F) this.fabricateProgress = 0.0F;
        }

        this.prevAttackProgress = this.attackProgress;
        if (this.attackTimer > 0) {
            this.attackTimer--;
            this.attackProgress += 1.0F / 12.0F;
            if (this.attackProgress > 1.0F) this.attackProgress = 1.0F;
        } else {
            this.attackProgress = 0.0F;
        }

        if (!this.world.isRemote) {
            if (this.headSlamCooldown > 0) {
                this.headSlamCooldown--;
            }

            boolean attackStance = false;
            boolean angry = this.getAttackTarget() != null || this.getRevengeTarget() != null;

            if (this.getAttackTarget() != null && this.getDistanceSq(this.getAttackTarget()) <= 9.0D) {
                attackStance = true;
            }
            if (this.dataManager.get(ATTACK_STANCE) != attackStance) {
                this.dataManager.set(ATTACK_STANCE, attackStance);
            }
            if (this.dataManager.get(ANGRY) != angry) {
                this.dataManager.set(ANGRY, angry);
            }
        }
    }

    public boolean isInAttackStance() {
        return this.dataManager.get(ATTACK_STANCE);
    }

    public boolean isAngry() {
        return this.dataManager.get(ANGRY);
    }

    public boolean isHeadSlamming() {
        return this.dataManager.get(HEAD_SLAMMING);
    }

    public boolean isFabricating() {
        return this.dataManager.get(FABRICATING);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAILampheadRegen(this));
        this.tasks.addTask(2, new EntityAILampheadInteract(this, 0.8D));
        this.tasks.addTask(3, new EntityAILampheadHeadSlam(this));
        this.tasks.addTask(4, new EntityAILampheadMeleeAttack(this, 1.2D, true));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F) {
            @Override
            public boolean shouldExecute() {
                return EntityLamphead.this.getMode() == 0 && super.shouldExecute();
            }
        });
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D) {
            @Override
            public boolean shouldExecute() {
                return EntityLamphead.this.getMode() == 0 && super.shouldExecute();
            }
        });
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this) {
            @Override
            public boolean shouldExecute() {
                return EntityLamphead.this.getMode() != 2 && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this) {
            @Override
            public boolean shouldExecute() {
                return EntityLamphead.this.getMode() != 2 && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true) {
            @Override
            public boolean shouldExecute() {
                return !EntityLamphead.this.isTamed() && super.shouldExecute();
            }
        });
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (this.isTamed()) {
            if (hand == EnumHand.MAIN_HAND && this.isOwner(player) && !this.world.isRemote && !this.isBreedingItem(itemstack)) {
                if (player.isSneaking()) {
                    int nextMode = (this.getMode() + 1) % 3;
                    this.setMode(nextMode);
                    
                    this.isJumping = false;
                    this.navigator.clearPath();
                    this.setAttackTarget(null);
                    
                    if (nextMode == 0) {
                        player.sendMessage(new TextComponentString("Lamphead is now Wandering: It will follow you and fight by your side."));
                        this.setSitting(false);
                    } else if (nextMode == 1) {
                        player.sendMessage(new TextComponentString("Lamphead is now Versatile: It will search for work and defend the area."));
                        this.setSitting(false);
                    } else {
                        player.sendMessage(new TextComponentString("Lamphead is now Static: It will hold its ground and work only on nearby stations."));
                        this.setSitting(false);
                    }
                }
                return true;
            }
        } else if (itemstack.getItem() == Items.NETHER_STAR) {
            if (hand == EnumHand.MAIN_HAND) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }

                if (!this.world.isRemote) {
                    this.setTamedBy(player);
                    this.navigator.clearPath();
                    this.setAttackTarget(null);
                    this.setMode(0); // Default to Wandering
                    this.setSitting(false);
                    this.setHealth(this.getMaxHealth());
                    this.playTameEffect(true);
                    this.world.setEntityState(this, (byte)7);
                }
                return true;
            }
        }

        return super.processInteract(player, hand);
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed() && this.ticksExisted > 2400;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.mobSettings.lampheadMaxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.225D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.mobSettings.lampheadAttackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.4D); // Help them against zombies
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_IRONGOLEM_STEP; // Placeholder
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_IRONGOLEM_HURT; // Placeholder
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH; // Placeholder
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.attackTimer = 12;
        this.attackProgress = 0.0F;
        this.prevAttackProgress = 0.0F;
        this.world.setEntityState(this, (byte)4);
        float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        
        if (flag && entityIn instanceof EntityLivingBase) {
            float knockback = (float) Settings.mobSettings.lampheadKnockbackMultiplier;
            ((EntityLivingBase)entityIn).knockBack(this, knockback, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
            
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)entityIn, this);
        }
        return flag;
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 4) {
            this.attackTimer = 12;
            this.attackProgress = 0.0F;
            this.prevAttackProgress = 0.0F;
        }
        super.handleStatusUpdate(id);
    }

    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        if (potioneffectIn.getPotion() == MobEffects.POISON || potioneffectIn.getPotion() == MobEffects.HUNGER || potioneffectIn.getPotion() == MobEffects.REGENERATION) {
            return false;
        }
        return super.isPotionApplicable(potioneffectIn);
    }

    public class EntityAILampheadHeadSlam extends EntityAIBase {
        private final EntityLamphead lamphead;
        private int attackTick;

        public EntityAILampheadHeadSlam(EntityLamphead lampheadIn) {
            this.lamphead = lampheadIn;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = this.lamphead.getAttackTarget();
            if (target == null || !target.isEntityAlive()) return false;
            if (this.lamphead.headSlamCooldown > 0) return false;
            if (this.lamphead.squeezeProgress > 0.0F) return false;
            return this.lamphead.getDistanceSq(target) <= 4.0D;
        }

        @Override
        public void startExecuting() {
            this.attackTick = 0;
            this.lamphead.dataManager.set(HEAD_SLAMMING, true);
            this.lamphead.getNavigator().clearPath();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.attackTick < 10;
        }

        @Override
        public void updateTask() {
            this.attackTick++;
            EntityLivingBase target = this.lamphead.getAttackTarget();
            if (target != null) {
                this.lamphead.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            }
            if (this.attackTick == 5) {
                double damage = Settings.mobSettings.lampheadAttackDamage * 1.5D;
                for (EntityLivingBase entity : this.lamphead.world.getEntitiesWithinAABB(EntityLivingBase.class, this.lamphead.getEntityBoundingBox().grow(2.0D))) {
                    if (entity != this.lamphead && !this.lamphead.isOnSameTeam(entity) && entity != this.lamphead.getOwner()) {
                        
                        // Untamed Lampheads should not hurt other untamed Lampheads
                        if (entity instanceof EntityLamphead) {
                            EntityLamphead otherLamphead = (EntityLamphead) entity;
                            if (!this.lamphead.isTamed() && !otherLamphead.isTamed()) {
                                continue;
                            }
                        }

                        entity.attackEntityFrom(DamageSource.causeMobDamage(this.lamphead), (float)damage);
                        float knockback = (float) Settings.mobSettings.lampheadKnockbackMultiplier * 1.5F;
                        entity.knockBack(this.lamphead, knockback, (double) MathHelper.sin(this.lamphead.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.lamphead.rotationYaw * 0.017453292F)));
                    }
                }
                this.lamphead.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
            }
        }

        @Override
        public void resetTask() {
            this.lamphead.dataManager.set(HEAD_SLAMMING, false);
            this.lamphead.headSlamCooldown = 200;
        }
    }
}
