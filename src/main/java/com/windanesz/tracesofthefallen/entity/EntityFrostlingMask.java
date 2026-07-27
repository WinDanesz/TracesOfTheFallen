package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFrostlingMask extends EntityThrowable {

    public EntityFrostlingMask(World worldIn) {
        super(worldIn);
    }

    public EntityFrostlingMask(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityFrostlingMask(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.01F; // low gravity for projectile
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.entityHit != null) {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 10.0F);
        }
        if (!this.world.isRemote) {
            this.setDead();
        }
    }
}
