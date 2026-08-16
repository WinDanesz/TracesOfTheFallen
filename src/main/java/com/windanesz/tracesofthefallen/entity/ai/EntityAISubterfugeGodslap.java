package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntitySubterfuge;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class EntityAISubterfugeGodslap extends EntityAIBase {
    private final EntitySubterfuge subterfuge;
    private int attackTick;
    private int nextAttackTime;

    public EntityAISubterfugeGodslap(EntitySubterfuge subterfuge) {
        this.subterfuge = subterfuge;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.subterfuge.isAggressive() || this.subterfuge.isHiddenState()) return false;
        EntityLivingBase target = this.subterfuge.getAttackTarget();
        if (target == null || !target.isEntityAlive()) return false;
        
        if (this.subterfuge.ticksExisted < this.nextAttackTime) {
            return false;
        }

        double distanceSq = this.subterfuge.getDistanceSq(target);
        return distanceSq <= 256.0D && this.subterfuge.getEntitySenses().canSee(target); // 16 blocks, line of sight
    }

    @Override
    public void startExecuting() {
        this.attackTick = 0;
        this.subterfuge.getDataManager().set(EntitySubterfuge.ATTACKING_STATE, true);
        this.subterfuge.getNavigator().clearPath(); // Stationary attack
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase target = this.subterfuge.getAttackTarget();
        if (target == null || !target.isEntityAlive() || !this.subterfuge.isAggressive()) {
            return false; // Stop immediately and lower arm if the target dies or she becomes peaceful
        }
        return this.attackTick < 40; // 2.0 seconds total: arm raise (10) + particle + damage delay (20) + recovery (10)
    }

    @Override
    public void updateTask() {
        this.attackTick++;
        EntityLivingBase target = this.subterfuge.getAttackTarget();
        
        if (target != null) {
            this.subterfuge.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }

        // Spawn particle at the climax of the arm raise (tick 10)
        if (this.attackTick == 10 && target != null) {
            double distanceSq = this.subterfuge.getDistanceSq(target);
            if (distanceSq <= 256.0D && this.subterfuge.getEntitySenses().canSee(target)) {
                // Spawn god_slap particle from the Subterfuge's hand area
                // Approx 1.0 blocks forward (closer to her) and 1.8 blocks high
                net.minecraft.util.math.Vec3d lookDir = this.subterfuge.getLookVec();
                double px = this.subterfuge.posX + lookDir.x * 1.0D;
                double py = this.subterfuge.posY + 1.8D;
                double pz = this.subterfuge.posZ + lookDir.z * 1.0D;

                net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint point = new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(
                        this.subterfuge.dimension, px, py, pz, 64.0D);
                com.windanesz.tracesofthefallen.network.PacketHandler.net.sendToAllAround(
                        new com.windanesz.tracesofthefallen.packet.PacketSpawnGodSlapParticle(px, py, pz), point);
            }
        }

        // Deal damage 1.0s (20 ticks) after the particle (tick 30)
        if (this.attackTick == 30 && target != null) {
            double distanceSq = this.subterfuge.getDistanceSq(target);
            if (distanceSq <= 256.0D && this.subterfuge.getEntitySenses().canSee(target)) {
                float damage = (float) this.subterfuge.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                target.attackEntityFrom(DamageSource.causeMobDamage(this.subterfuge), damage);

                // Massive knockback
                Vec3d knockbackDir = target.getPositionVector().subtract(this.subterfuge.getPositionVector()).normalize();
                target.knockBack(this.subterfuge, 3.0F, -knockbackDir.x, -knockbackDir.z);
            }
        }
    }

    @Override
    public void resetTask() {
        this.subterfuge.getDataManager().set(EntitySubterfuge.ATTACKING_STATE, false);
        this.nextAttackTime = this.subterfuge.ticksExisted + 60 + this.subterfuge.getRNG().nextInt(41); // 3 to 5 seconds cooldown
    }
}
