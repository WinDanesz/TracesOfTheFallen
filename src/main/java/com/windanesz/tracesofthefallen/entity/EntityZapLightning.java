package com.windanesz.tracesofthefallen.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.List;

public class EntityZapLightning extends Entity implements IEntityAdditionalSpawnData {
    private int lightningState;
    public long boltVertex = 0L;
    private int boltLivingTime;
    
    public double startX, startY, startZ;
    public double endX, endY, endZ;
    public EntityLivingBase caster;

    public EntityZapLightning(World worldIn) {
        super(worldIn);
        this.ignoreFrustumCheck = true;
        this.lightningState = 2;
        this.boltVertex = this.rand.nextLong();
        this.boltLivingTime = this.rand.nextInt(3) + 1;
    }

    public EntityZapLightning(World worldIn, double x1, double y1, double z1, double x2, double y2, double z2, EntityLivingBase casterIn) {
        super(worldIn);
        this.startX = x1;
        this.startY = y1;
        this.startZ = z1;
        this.endX = x2;
        this.endY = y2;
        this.endZ = z2;
        this.caster = casterIn;
        this.setPosition(x1, y1, z1);
        this.lightningState = 2;
        this.boltVertex = this.rand.nextLong();
        this.boltLivingTime = this.rand.nextInt(3) + 1;
        this.ignoreFrustumCheck = true;
        
        worldIn.playSound(null, x1, y1, z1, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 2.0F, 1.2F);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.lightningState == 2) {
            this.world.playSound(null, this.startX, this.startY, this.startZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
            
            if (!this.world.isRemote) {
                double minX = Math.min(this.startX, this.endX) - 0.5;
                double minY = Math.min(this.startY, this.endY) - 0.5;
                double minZ = Math.min(this.startZ, this.endZ) - 0.5;
                double maxX = Math.max(this.startX, this.endX) + 0.5;
                double maxY = Math.max(this.startY, this.endY) + 0.5;
                double maxZ = Math.max(this.startZ, this.endZ) + 0.5;
                
                List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
                Vec3d startVec = new Vec3d(this.startX, this.startY, this.startZ);
                Vec3d endVec = new Vec3d(this.endX, this.endY, this.endZ);
                Vec3d dir = endVec.subtract(startVec).normalize();
                
                for (Entity e : list) {
                    if (e != this.caster && e instanceof EntityLivingBase) {
                        Vec3d ePos = new Vec3d(e.posX, e.posY + e.height / 2.0, e.posZ);
                        Vec3d toEntity = ePos.subtract(startVec);
                        double dot = toEntity.dotProduct(dir);
                        if (dot > 0 && dot < endVec.subtract(startVec).length()) {
                            Vec3d proj = startVec.add(dir.scale(dot));
                            if (proj.distanceTo(ePos) <= 0.5 + e.width / 2.0) {
                                if (e instanceof com.windanesz.tracesofthefallen.entity.EntityLamphead) {
                                    com.windanesz.tracesofthefallen.entity.EntityLamphead lamphead = (com.windanesz.tracesofthefallen.entity.EntityLamphead) e;
                                    if (!lamphead.isTamed()) {
                                        lamphead.setTamedBy(this.caster instanceof net.minecraft.entity.player.EntityPlayer ? (net.minecraft.entity.player.EntityPlayer) this.caster : null);
                                        lamphead.setHealth(lamphead.getMaxHealth());
                                        lamphead.setAttackTarget(null);
                                        lamphead.setRevengeTarget(null);
                                        lamphead.world.setEntityState(lamphead, (byte) 7); // Heart particles
                                        continue; // Skip dealing damage to the newly tamed lamphead
                                    }
                                }
                                
                                e.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.caster != null ? this.caster : this), 8.0F);
                                if (e instanceof net.minecraft.entity.monster.EntityCreeper) {
                                    e.onStruckByLightning(new net.minecraft.entity.effect.EntityLightningBolt(this.world, e.posX, e.posY, e.posZ, true));
                                    e.extinguish();
                                }
                            }
                        }
                    }
                }
            }
        }

        --this.lightningState;

        if (this.lightningState < 0) {
            if (this.boltLivingTime == 0) {
                if (this.lightningState < -5) {
                    this.setDead();
                }
            } else if (this.lightningState < -this.rand.nextInt(10)) {
                --this.boltLivingTime;
                this.lightningState = 1;
                this.boltVertex = this.rand.nextLong();
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.startX = compound.getDouble("startX");
        this.startY = compound.getDouble("startY");
        this.startZ = compound.getDouble("startZ");
        this.endX = compound.getDouble("endX");
        this.endY = compound.getDouble("endY");
        this.endZ = compound.getDouble("endZ");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setDouble("startX", this.startX);
        compound.setDouble("startY", this.startY);
        compound.setDouble("startZ", this.startZ);
        compound.setDouble("endX", this.endX);
        compound.setDouble("endY", this.endY);
        compound.setDouble("endZ", this.endZ);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeDouble(this.startX);
        buffer.writeDouble(this.startY);
        buffer.writeDouble(this.startZ);
        buffer.writeDouble(this.endX);
        buffer.writeDouble(this.endY);
        buffer.writeDouble(this.endZ);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.startX = additionalData.readDouble();
        this.startY = additionalData.readDouble();
        this.startZ = additionalData.readDouble();
        this.endX = additionalData.readDouble();
        this.endY = additionalData.readDouble();
        this.endZ = additionalData.readDouble();
    }
}
