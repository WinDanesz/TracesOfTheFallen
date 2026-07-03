package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityMinecrawler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class AIMinecrawlerRiderTarget extends EntityAIBase {
    private final EntityMinecrawler minecrawler;

    public AIMinecrawlerRiderTarget(EntityMinecrawler minecrawler) {
        this.minecrawler = minecrawler;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return !this.minecrawler.getPassengers().isEmpty() && this.minecrawler.getPassengers().get(0) instanceof EntityGoblin;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.minecrawler.getPassengers().isEmpty() && this.minecrawler.getPassengers().get(0) instanceof EntityGoblin;
    }

    @Override
    public void updateTask() {
        EntityGoblin rider = (EntityGoblin) this.minecrawler.getPassengers().get(0);
        EntityLivingBase riderTarget = rider.getAttackTarget();
        if (riderTarget != null && riderTarget.isEntityAlive()) {
            if (this.minecrawler.getAttackTarget() != riderTarget) {
                this.minecrawler.setAttackTarget(riderTarget);
            }
        } else if (this.minecrawler.getAttackTarget() != null) {
            if (rider.isOwner(this.minecrawler.getAttackTarget()) || (rider.hasOwner() && this.minecrawler.getAttackTarget() instanceof EntityPlayer)) {
                this.minecrawler.setAttackTarget(null);
            }
        }
    }
}
