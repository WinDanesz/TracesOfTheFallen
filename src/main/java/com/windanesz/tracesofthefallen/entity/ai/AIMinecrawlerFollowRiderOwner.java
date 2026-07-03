package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityMinecrawler;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AIMinecrawlerFollowRiderOwner extends EntityAIBase {
    private final EntityMinecrawler minecrawler;
    private final double speed;
    private final float minDist;
    private final float maxDist;
    private int timeToRecalcPath;

    public AIMinecrawlerFollowRiderOwner(EntityMinecrawler minecrawler, double speed, float minDist, float maxDist) {
        this.minecrawler = minecrawler;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.minecrawler.getPassengers().isEmpty() || !(this.minecrawler.getPassengers().get(0) instanceof EntityGoblin)) {
            return false;
        }
        EntityGoblin rider = (EntityGoblin) this.minecrawler.getPassengers().get(0);
        if (!rider.hasOwner() || rider.getOwner() == null) {
            return false;
        }
        EntityLivingBase owner = rider.getOwner();
        if (owner == null || !owner.isEntityAlive()) {
            return false;
        }
        if (this.minecrawler.getAttackTarget() != null && this.minecrawler.getAttackTarget().isEntityAlive()) {
            return false;
        }
        return this.minecrawler.getDistanceSq(owner) > (double) (this.minDist * this.minDist);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.minecrawler.getPassengers().isEmpty() || !(this.minecrawler.getPassengers().get(0) instanceof EntityGoblin)) {
            return false;
        }
        EntityGoblin rider = (EntityGoblin) this.minecrawler.getPassengers().get(0);
        if (!rider.hasOwner() || rider.getOwner() == null) {
            return false;
        }
        EntityLivingBase owner = rider.getOwner();
        if (owner == null || !owner.isEntityAlive()) {
            return false;
        }
        if (this.minecrawler.getAttackTarget() != null && this.minecrawler.getAttackTarget().isEntityAlive()) {
            return false;
        }
        return !this.minecrawler.getNavigator().noPath() && this.minecrawler.getDistanceSq(owner) > (double) (this.maxDist * this.maxDist);
    }

    @Override
    public void startExecuting() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void resetTask() {
        this.minecrawler.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        EntityGoblin rider = (EntityGoblin) this.minecrawler.getPassengers().get(0);
        EntityLivingBase owner = rider.getOwner();
        if (owner != null) {
            this.minecrawler.getLookHelper().setLookPositionWithEntity(owner, 10.0F, (float) this.minecrawler.getVerticalFaceSpeed());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (!this.minecrawler.getNavigator().tryMoveToEntityLiving(owner, this.speed)) {
                    if (!this.minecrawler.getLeashed()) {
                        if (this.minecrawler.getDistanceSq(owner) >= 144.0D) {
                            int i = MathHelper.floor(owner.posX) - 2;
                            int j = MathHelper.floor(owner.posZ) - 2;
                            int k = MathHelper.floor(owner.getEntityBoundingBox().minY);

                            for (int l = 0; l <= 4; ++l) {
                                for (int i1 = 0; i1 <= 4; ++i1) {
                                    if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(i, j, k, l, i1)) {
                                        this.minecrawler.setLocationAndAngles((double) ((float) (i + l) + 0.5F), (double) k, (double) ((float) (j + i1) + 0.5F), this.minecrawler.rotationYaw, this.minecrawler.rotationPitch);
                                        this.minecrawler.getNavigator().clearPath();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset) {
        BlockPos blockpos = new BlockPos(x + xOffset, y - 1, z + zOffset);
        IBlockState iblockstate = this.minecrawler.world.getBlockState(blockpos);
        return iblockstate.getBlockFaceShape(this.minecrawler.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(this.minecrawler) && this.minecrawler.world.isAirBlock(blockpos.up()) && this.minecrawler.world.isAirBlock(blockpos.up(2));
    }
}
