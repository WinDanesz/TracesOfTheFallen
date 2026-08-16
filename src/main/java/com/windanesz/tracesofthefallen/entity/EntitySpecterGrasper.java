package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.Random;

public class EntitySpecterGrasper extends EntitySpecter {

    public EntitySpecterGrasper(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.8F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.specterMaxHealth * 3.0D); // x3 health
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.specterAttackDamage * 1.6D); // x1.6 damage (reduced by 20% from x2)
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.16D); // Slower than regular specter (was 0.2D)
    }

    @Override
    public int getBrightnessForRender() {
        int i = super.getBrightnessForRender();
        int j = i & 255;
        int k = i >> 16 & 255;

        // Ensure a high minimum block light for a strong glow
        if (j < 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @Override
    public float getBrightness() {
        float f = super.getBrightness();
        return f < 1.0F ? 1.0F : f;
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        // Clear inherited AI tasks as Grasper is a pure hostile mob and not a minion
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();
        
        // Add Grasper specific movement and attack AI
        this.tasks.addTask(4, new AIGrasperAttack(this));
        this.tasks.addTask(5, new AIGrasperWander(this));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        
        // Add robust targeting AI
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    public boolean useLeftHand = false;

    static class AIGrasperAttack extends EntityAIBase {
        private final EntitySpecterGrasper parentEntity;
        private int attackCooldown;
        private int windupTicks;

        public AIGrasperAttack(EntitySpecterGrasper specter) {
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
            this.windupTicks = 0;
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

            double distanceSq = this.parentEntity.getDistanceSq(target);
            this.parentEntity.getLookHelper().setLookPositionWithEntity(target, 10.0F, 10.0F);
            if (distanceSq > 5.0D) {
                this.parentEntity.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1.0D);
            } else {
                // Clear move target so it hovers in front instead of flying into the player's face
                if (this.parentEntity.getMoveHelper() instanceof SpecterMoveHelper) {
                    ((SpecterMoveHelper) this.parentEntity.getMoveHelper()).startCooldown(20);
                }
            }

            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            }

            if (this.windupTicks > 0) {
                --this.windupTicks;
                if (this.windupTicks == 0) {
                    // Deal damage after windup if target is still in range
                    if (this.parentEntity.getDistanceSq(target) < 8.0D) { 
                        this.parentEntity.attackEntityAsMob(target);
                    }
                    this.attackCooldown = 40;
                }
            } else if (distanceSq < 6.5D && this.attackCooldown <= 0) {
                // Alternate hands
                this.parentEntity.useLeftHand = !this.parentEntity.useLeftHand;
                // Start the swing animation 10 ticks before the actual hit lands
                this.parentEntity.swingArm(this.parentEntity.useLeftHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                this.windupTicks = 10;
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        // Slow down the swing animation to take ~15 ticks instead of 6
        if (this.isSwingInProgress && this.ticksExisted % 3 != 0) {
            this.swingProgressInt--;
        }
    }

    static class AIGrasperWander extends EntityAIBase {
        private final EntitySpecterGrasper parentEntity;

        public AIGrasperWander(EntitySpecterGrasper specter) {
            this.parentEntity = specter;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.parentEntity.getRNG().nextInt(60) != 0) {
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
            double d0 = this.parentEntity.posX + (double) ((random.nextFloat() * 2.0F - 1.0F) * 6.0F);
            double d1 = this.parentEntity.posY + (double) ((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
            double d2 = this.parentEntity.posZ + (double) ((random.nextFloat() * 2.0F - 1.0F) * 6.0F);
            this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 0.4D); // Slower, eerie movement
        }
    }

    @Override
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return new net.minecraft.util.ResourceLocation(com.windanesz.tracesofthefallen.TracesOfTheFallen.MODID, "entities/specter_grasper");
    }
}
