package com.windanesz.tracesofthefallen.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
    public float damage = 3.0F;
    public boolean visualOnly = false;
    public boolean isVisible = true;
    public int age = 0;
    public int maxAge = 55;
    private java.util.Map<Entity, Integer> hitCooldowns = new java.util.HashMap<>();
    public boolean singleDamageInstance = false;
    public boolean customMaxAge = false;

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
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!this.customMaxAge) {
            if (this.visualOnly) {
                this.maxAge = 10;
            } else {
                this.maxAge = 55;
            }
        }

        this.age++;
        if (this.age >= this.maxAge) {
            this.setDead();
            return;
        }

        if (this.age > 40 && this.age < 50 && !this.visualOnly) {
            this.isVisible = false;
            return;
        }

        this.isVisible = true;

        if (this.age % 3 == 0) {
            this.boltVertex = this.rand.nextLong();
        }

        if (this.age == 1 && !this.visualOnly) {
            this.world.playSound(null, this.startX, this.startY, this.startZ, com.windanesz.tracesofthefallen.init.ModSounds.LIGHTNING_ZAP, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
        }
        
        if (this.age == 50 && !this.visualOnly) {
            this.world.playSound(null, this.startX, this.startY, this.startZ, com.windanesz.tracesofthefallen.init.ModSounds.LIGHTNING_ZAP, SoundCategory.WEATHER, 2.0F, 0.8F + this.rand.nextFloat() * 0.2F);
        }

        if (!this.world.isRemote && !this.visualOnly) {
            if (this.singleDamageInstance) {
                if (this.age == 1) {
                    this.dealDamage(this.damage, true);
                }
            } else {
                if (this.age <= 40) {
                    this.hitCooldowns.replaceAll((e, cd) -> cd - 1);
                    this.hitCooldowns.values().removeIf(cd -> cd <= 0);
                    this.dealDamage(this.damage, false);
                } else if (this.age == 50) {
                    this.dealDamage(this.damage * 0.2F, true);
                }
            }
        }
    }

    private void dealDamage(float dmg, boolean ignoreCooldowns) {
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
                if (!ignoreCooldowns && this.hitCooldowns.containsKey(e)) continue;
                
                Vec3d ePos = new Vec3d(e.posX, e.posY + e.height / 2.0, e.posZ);
                Vec3d toEntity = ePos.subtract(startVec);
                double dot = toEntity.dotProduct(dir);
                if (dot > 0 && dot < endVec.subtract(startVec).length()) {
                    Vec3d proj = startVec.add(dir.scale(dot));
                    if (proj.distanceTo(ePos) <= 0.5 + e.width / 2.0) {
                        if (e instanceof com.windanesz.tracesofthefallen.entity.EntityLamphead) {
                            com.windanesz.tracesofthefallen.entity.EntityLamphead lamphead = (com.windanesz.tracesofthefallen.entity.EntityLamphead) e;
                            if (!lamphead.isTamed()) {
                                net.minecraft.entity.player.EntityPlayer tamer = null;
                                if (this.caster instanceof net.minecraft.entity.player.EntityPlayer) {
                                    tamer = (net.minecraft.entity.player.EntityPlayer) this.caster;
                                } else {
                                    tamer = this.world.getClosestPlayerToEntity(lamphead, 12.0D);
                                }
                                
                                if (tamer != null) {
                                    lamphead.setTamedBy(tamer);
                                    lamphead.setHealth(lamphead.getMaxHealth());
                                    lamphead.setAttackTarget(null);
                                    lamphead.setRevengeTarget(null);
                                    lamphead.world.setEntityState(lamphead, (byte) 7);
                                }
                                if (!ignoreCooldowns) this.hitCooldowns.put(e, 20);
                                continue;
                            }
                        }
                        
                        boolean hurt = e.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.caster != null ? this.caster : this), dmg);
                        if (hurt && !ignoreCooldowns) {
                            this.hitCooldowns.put(e, 20);
                        }
                        if (e instanceof net.minecraft.entity.monster.EntityCreeper) {
                            e.onStruckByLightning(new net.minecraft.entity.effect.EntityLightningBolt(this.world, e.posX, e.posY, e.posZ, true));
                            e.extinguish();
                        }
                    }
                }
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
        if (compound.hasKey("damage")) {
            this.damage = compound.getFloat("damage");
        }
        this.visualOnly = compound.getBoolean("visualOnly");
        this.singleDamageInstance = compound.getBoolean("singleDamageInstance");
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setVisualOnly(boolean visualOnly) {
        this.visualOnly = visualOnly;
    }

    public void setSingleDamageInstance(boolean singleDamageInstance) {
        this.singleDamageInstance = singleDamageInstance;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        this.customMaxAge = true;
    }

    public void setCaster(EntityLivingBase caster) {
        this.caster = caster;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setDouble("startX", this.startX);
        compound.setDouble("startY", this.startY);
        compound.setDouble("startZ", this.startZ);
        compound.setDouble("endX", this.endX);
        compound.setDouble("endY", this.endY);
        compound.setDouble("endZ", this.endZ);
        compound.setFloat("damage", this.damage);
        compound.setBoolean("visualOnly", this.visualOnly);
        compound.setBoolean("singleDamageInstance", this.singleDamageInstance);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeDouble(this.startX);
        buffer.writeDouble(this.startY);
        buffer.writeDouble(this.startZ);
        buffer.writeDouble(this.endX);
        buffer.writeDouble(this.endY);
        buffer.writeDouble(this.endZ);
        buffer.writeBoolean(this.visualOnly);
        buffer.writeBoolean(this.singleDamageInstance);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.startX = additionalData.readDouble();
        this.startY = additionalData.readDouble();
        this.startZ = additionalData.readDouble();
        this.endX = additionalData.readDouble();
        this.endY = additionalData.readDouble();
        this.endZ = additionalData.readDouble();
        this.visualOnly = additionalData.readBoolean();
        this.singleDamageInstance = additionalData.readBoolean();
    }
}
