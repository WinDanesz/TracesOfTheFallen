package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.ai.AIMinecrawlerAttack;
import com.windanesz.tracesofthefallen.entity.ai.AIMinecrawlerFollowRiderOwner;
import com.windanesz.tracesofthefallen.entity.ai.AIMinecrawlerRiderTarget;
import com.windanesz.tracesofthefallen.item.ChitinArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityMinecrawler extends EntityMob {
    private static final int ATTACK_ANIMATION_DURATION = 30;
    private static final int TAUNT_MIN_TICKS = 40;
    private static final int TAUNT_MAX_TICKS = 60;
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/minecrawler");
    private static final DataParameter<Integer> ATTACK_ANIMATION_TICKS = EntityDataManager.createKey(EntityMinecrawler.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TAUNT_TICKS = EntityDataManager.createKey(EntityMinecrawler.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TAUNT_DURATION = EntityDataManager.createKey(EntityMinecrawler.class, DataSerializers.VARINT);
    private boolean hadTargetLastTick;

    public EntityMinecrawler(World worldIn) {
        super(worldIn);
        this.setSize(1.1F, 1.4F);
        this.experienceValue = 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_ANIMATION_TICKS, 0);
        this.dataManager.register(TAUNT_TICKS, 0);
        this.dataManager.register(TAUNT_DURATION, 0);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(2, new AIMinecrawlerAttack(this, 1.2D));
        this.tasks.addTask(3, new AIMinecrawlerFollowRiderOwner(this, 1.3D, 6.0F, 3.0F));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(0, new AIMinecrawlerRiderTarget(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true) {
            @Override
            protected boolean isSuitableTarget(EntityLivingBase target, boolean ignoreDisabledDamage) {
                if (!super.isSuitableTarget(target, ignoreDisabledDamage)) {
                    return false;
                }
                if (!this.taskOwner.getPassengers().isEmpty() && this.taskOwner.getPassengers().get(0) instanceof EntityGoblin) {
                    EntityGoblin rider = (EntityGoblin) this.taskOwner.getPassengers().get(0);
                    if (rider.isOwner(target)) {
                        return false;
                    }
                }
                if (target instanceof EntityPlayer && shouldIgnoreChitinPlayer((EntityPlayer) target,
                        this.taskOwner.getDistanceSq(target))) {
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.26D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(4.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.swingArm(EnumHand.MAIN_HAND);
        boolean attacked = super.attackEntityAsMob(entityIn);
        if (attacked) {
            double dx = entityIn.posX - this.posX;
            double dz = entityIn.posZ - this.posZ;
            double distSq = dx * dx + dz * dz;
            if (distSq > 1.0E-6D) {
                double invDist = 1.0D / Math.sqrt(distSq);
                double lunge = 0.5D;
                this.motionX += dx * invDist * lunge;
                this.motionZ += dz * invDist * lunge;
                this.velocityChanged = true;
            }
        }
        return attacked;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            EntityLivingBase target = this.getAttackTarget();
            boolean hasTarget = target != null && target.isEntityAlive();
            if (hasTarget && !this.hadTargetLastTick && !this.isTaunting()) {
                if (this.getDistanceSq(target) > 16.0D) {
                    this.startTaunt(TAUNT_MIN_TICKS + this.rand.nextInt(TAUNT_MAX_TICKS - TAUNT_MIN_TICKS + 1));
                }
            }
            this.hadTargetLastTick = hasTarget;

            int attackTicks = this.dataManager.get(ATTACK_ANIMATION_TICKS);
            if (attackTicks > 0) {
                this.dataManager.set(ATTACK_ANIMATION_TICKS, attackTicks - 1);
            }

            int tauntTicks = this.dataManager.get(TAUNT_TICKS);
            if (tauntTicks > 0) {
                this.dataManager.set(TAUNT_TICKS, tauntTicks - 1);
                if (tauntTicks == 1) {
                    this.dataManager.set(TAUNT_DURATION, 0);
                }
            }
        }
    }

    public float getAttackAnimationProgress(float partialTicks) {
        int attackTicks = this.dataManager.get(ATTACK_ANIMATION_TICKS);
        if (attackTicks <= 0) {
            return 0.0F;
        }
        float elapsed = ATTACK_ANIMATION_DURATION - (attackTicks - partialTicks);
        return MathHelper.clamp(elapsed / ATTACK_ANIMATION_DURATION, 0.0F, 1.0F);
    }

    public boolean isTaunting() {
        return this.dataManager.get(TAUNT_TICKS) > 0;
    }

    public float getTauntRaiseAmount(float partialTicks) {
        int tauntTicks = this.dataManager.get(TAUNT_TICKS);
        int tauntDuration = this.dataManager.get(TAUNT_DURATION);
        if (tauntTicks <= 0 || tauntDuration <= 0) {
            return 0.0F;
        }

        float elapsed = tauntDuration - (tauntTicks - partialTicks);
        float normalized = MathHelper.clamp(elapsed / tauntDuration, 0.0F, 1.0F);
        float wave = MathHelper.sin(normalized * ((float) Math.PI * 4.0F));
        return Math.max(0.0F, wave);
    }

    public void startAttackAnimation() {
        if (!this.world.isRemote) {
            this.dataManager.set(ATTACK_ANIMATION_TICKS, ATTACK_ANIMATION_DURATION);
        }
    }

    private void startTaunt(int durationTicks) {
        if (!this.world.isRemote) {
            this.dataManager.set(TAUNT_DURATION, durationTicks);
            this.dataManager.set(TAUNT_TICKS, durationTicks);
        }
    }

    public void lockFacing(EntityLivingBase target) {
        double dx = target.posX - this.posX;
        double dz = target.posZ - this.posZ;
        float yawToTarget = (float) (MathHelper.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
        this.rotationYaw = yawToTarget;
        this.rotationYawHead = yawToTarget;
        this.renderYawOffset = yawToTarget;
        this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
    }

    private static boolean shouldIgnoreChitinPlayer(EntityPlayer player, double distanceSq) {
        int stealthRadius = Settings.miscSettings.chitinArmorMinecrawlerStealthRadius;
        if (stealthRadius <= 0) {
            return false;
        }
        return ChitinArmor.isWearingFullSet(player) && distanceSq > (double) (stealthRadius * stealthRadius);
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof EntityGoblin;
    }

    @Override
    public double getMountedYOffset() {
        return (double) this.height * 0.5D;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            float yaw = this.renderYawOffset * (float) (Math.PI / 180.0);
            double backOffset = 0.3375D;
            double xOffset = (double) MathHelper.sin(yaw) * backOffset;
            double zOffset = -(double) MathHelper.cos(yaw) * backOffset;
            passenger.setPosition(this.posX + xOffset, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + zOffset);
            if (passenger instanceof EntityLivingBase) {
                ((EntityLivingBase) passenger).renderYawOffset = this.renderYawOffset;
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!this.world.isRemote) {
            if (!this.getPassengers().isEmpty()) {
                Entity passenger = this.getPassengers().get(0);
                if (passenger instanceof EntityGoblin && player.isSneaking() && ((EntityGoblin) passenger).isOwner(player)) {
                    passenger.dismountRidingEntity();
                    return true;
                }
            } else {
                List<EntityGoblin> nearbyGoblins = this.world.getEntitiesWithinAABB(EntityGoblin.class,
                        this.getEntityBoundingBox().grow(16.0D));
                EntityGoblin closestAllied = null;
                double closestDist = Double.MAX_VALUE;
                for (EntityGoblin goblin : nearbyGoblins) {
                    if (goblin.isOwner(player) && !goblin.isRiding() && !goblin.isCarryingBomb() && !goblin.isHoldingIdol()) {
                        double dist = this.getDistanceSq(goblin);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closestAllied = goblin;
                        }
                    }
                }
                if (closestAllied != null) {
                    closestAllied.startRiding(this);
                    return true;
                }
            }
        }
        return super.processInteract(player, hand);
    }

}
