package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityMinecrawler;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;

import java.util.List;

public class GoblinAIMountMinecrawler extends EntityAIBase {
    private final EntityGoblin goblin;
    private final PathNavigate navigator;
    private final double speed;
    private EntityMinecrawler targetMinecrawler;
    private int searchCooldown;
    private static final double DETECTION_RANGE = 12.0D;
    private static final int SEARCH_INTERVAL = 20;

    public GoblinAIMountMinecrawler(EntityGoblin goblin, double speed) {
        this.goblin = goblin;
        this.speed = speed;
        this.navigator = goblin.getNavigator();
        this.setMutexBits(3);
        this.searchCooldown = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (this.goblin.isRiding() || this.goblin.isCarryingBomb() || this.goblin.isHoldingIdol()) {
            return false;
        }
        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }

        this.searchCooldown = SEARCH_INTERVAL;
        this.targetMinecrawler = this.findNearestMinecrawler();
        return this.targetMinecrawler != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.goblin.isRiding() || this.goblin.isCarryingBomb() || this.goblin.isHoldingIdol()) {
            return false;
        }
        if (this.targetMinecrawler == null || this.targetMinecrawler.isDead || !this.targetMinecrawler.getPassengers().isEmpty() || this.targetMinecrawler.isRiding()) {
            return false;
        }
        if (this.goblin.getDistanceSq(this.targetMinecrawler) > DETECTION_RANGE * DETECTION_RANGE * 2.0D) {
            return false;
        }
        return !this.navigator.noPath();
    }

    @Override
    public void startExecuting() {
        if (this.targetMinecrawler != null) {
            this.navigator.tryMoveToEntityLiving(this.targetMinecrawler, this.speed);
        }
    }

    @Override
    public void resetTask() {
        this.targetMinecrawler = null;
        this.navigator.clearPath();
    }

    @Override
    public void updateTask() {
        if (this.targetMinecrawler != null && !this.targetMinecrawler.isDead) {
            this.goblin.getLookHelper().setLookPositionWithEntity(this.targetMinecrawler, 10.0F, (float) this.goblin.getVerticalFaceSpeed());

            if (this.goblin.getDistanceSq(this.targetMinecrawler) > 4.0D) {
                if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
                    this.navigator.tryMoveToEntityLiving(this.targetMinecrawler, this.speed);
                }
            } else {
                if (!this.goblin.world.isRemote && this.targetMinecrawler.getPassengers().isEmpty()) {
                    this.goblin.startRiding(this.targetMinecrawler);
                }
            }
        }
    }

    private EntityMinecrawler findNearestMinecrawler() {
        List<EntityMinecrawler> nearby = this.goblin.world.getEntitiesWithinAABB(
                EntityMinecrawler.class,
                this.goblin.getEntityBoundingBox().grow(DETECTION_RANGE, 4.0D, DETECTION_RANGE)
        );

        EntityMinecrawler closest = null;
        double closestDist = Double.MAX_VALUE;

        for (EntityMinecrawler crawler : nearby) {
            if (!crawler.isDead && crawler.getPassengers().isEmpty() && !crawler.isRiding()) {
                double dist = this.goblin.getDistanceSq(crawler);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = crawler;
                }
            }
        }
        return closest;
    }
}
