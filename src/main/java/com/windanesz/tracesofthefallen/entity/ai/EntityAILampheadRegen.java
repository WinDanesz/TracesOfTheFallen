package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;

public class EntityAILampheadRegen extends EntityAIBase {
    private final EntityLamphead lamphead;
    private int healTimer;

    public EntityAILampheadRegen(EntityLamphead lamphead) {
        this.lamphead = lamphead;
        this.setMutexBits(5); // Movement (1) + Look (4) - no moving or looking while sitting to heal
    }

    @Override
    public boolean shouldExecute() {
        if (!this.lamphead.isTamed()) return false;
        if (this.lamphead.getMode() != 0) return false;
        if (this.lamphead.getHealth() >= this.lamphead.getMaxHealth()) return false;
        if (this.lamphead.getAttackTarget() != null) return false;
        if (!this.lamphead.getNavigator().noPath()) return false;
        if (!this.lamphead.onGround) return false;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.healTimer = 0;
        this.lamphead.setSitting(true);
        this.lamphead.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        this.healTimer++;
        if (this.healTimer >= 40) { // Heal 1.0 (half a heart) every 2 seconds
            this.lamphead.heal(1.0F);
            this.healTimer = 0;
            
            if (!this.lamphead.world.isRemote && this.lamphead.world instanceof WorldServer) {
                ((WorldServer) this.lamphead.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, 
                    this.lamphead.posX, this.lamphead.posY + this.lamphead.height, this.lamphead.posZ, 
                    3, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        }
    }

    @Override
    public void resetTask() {
        this.lamphead.setSitting(false);
    }
}
