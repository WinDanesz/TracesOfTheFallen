package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityMinecrawler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class AIMinecrawlerAttack extends EntityAIAttackMelee {
    private static final int WINDUP_TICKS = 10;
    private final EntityMinecrawler crawler;
    private int windupTicks;
    private boolean windingUp;

    public AIMinecrawlerAttack(EntityMinecrawler crawler, double speedIn) {
        super(crawler, speedIn, false);
        this.crawler = crawler;
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = this.crawler.getAttackTarget();
        if (target == null) {
            super.updateTask();
            return;
        }

        if (this.crawler.isTaunting()) {
            if (this.attackTick > 0) {
                this.attackTick--;
            }
            this.crawler.getNavigator().clearPath();
            this.crawler.lockFacing(target);
            this.crawler.getMoveHelper().strafe(0.0F, 0.0F);
            return;
        }

        if (this.attackTick > 0) {
            this.attackTick--;
        }

        super.updateTask();
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        if (this.crawler.isTaunting()) {
            return;
        }

        double reach = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= reach) {
            if (!this.windingUp && this.attackTick <= 0) {
                this.windingUp = true;
                this.windupTicks = WINDUP_TICKS;
                this.crawler.startAttackAnimation();
                return;
            }

            if (this.windingUp) {
                if (this.windupTicks > 0) {
                    this.windupTicks--;
                    return;
                }
                this.windingUp = false;
                this.attackTick = 20;
                this.crawler.attackEntityAsMob(enemy);
                return;
            }
        } else {
            this.windingUp = false;
            this.windupTicks = 0;
        }
        super.checkAndPerformAttack(enemy, distToEnemySqr);
    }
}
