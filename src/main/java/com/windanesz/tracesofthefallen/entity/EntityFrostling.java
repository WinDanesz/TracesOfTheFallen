package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityFrostling extends EntityTameable {
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/frostling");
    private static final DataParameter<Boolean> ROLLING = EntityDataManager.createKey(EntityFrostling.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> NO_MASK = EntityDataManager.createKey(EntityFrostling.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> SHOOTING = EntityDataManager.createKey(EntityFrostling.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> AGGRO = EntityDataManager.createKey(EntityFrostling.class, DataSerializers.BOOLEAN);
    
    public int biteCooldown = 0;
    public int attackTimer;
    public float attackProgress;
    public float prevAttackProgress;
    
    public int suicideTimer = -1;
    public int shootTimer = -1;
    public int preShootTimer = -1;
    
    public float shootProgress;
    public float prevShootProgress;
    
    public float aggroProgress;
    public float prevAggroProgress;
    
    public int slashTimer = 0;
    public float slashProgress;
    public float prevSlashProgress;
    
    private int rollTimeout = 0;
    
    public EntityFrostling(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.8F);
        this.experienceValue = 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ROLLING, false);
        this.dataManager.register(NO_MASK, false);
        this.dataManager.register(SHOOTING, false);
        this.dataManager.register(AGGRO, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.2D, false) {
            @Override
            protected double getAttackReachSqr(net.minecraft.entity.EntityLivingBase attackTarget) {
                if (((EntityFrostling)this.attacker).biteCooldown <= 0) {
                    return 3.4D + attackTarget.width; // 4.0 squared (2.0 blocks distance) against standard 0.6 width players
                }
                return 1.09D + attackTarget.width; // 1.69 squared (1.3 blocks distance) against standard 0.6 width players
            }
        });
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true) {
            @Override
            public boolean shouldExecute() {
                return !EntityFrostling.this.isTamed() && super.shouldExecute();
            }
        });
    }

    public float getBiteProgress(float partialTick) {
        return this.prevAttackProgress + (this.attackProgress - this.prevAttackProgress) * partialTick;
    }

    public float getShootProgress(float partialTick) {
        return this.prevShootProgress + (this.shootProgress - this.prevShootProgress) * partialTick;
    }

    public float getAggroProgress(float partialTick) {
        return this.prevAggroProgress + (this.aggroProgress - this.prevAggroProgress) * partialTick;
    }

    public float getSlashProgress(float partialTick) {
        float f = this.slashProgress - this.prevSlashProgress;
        if (f < 0.0F) {
            f++;
        }
        return this.prevSlashProgress + f * partialTick;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        this.prevShootProgress = this.shootProgress;
        if (this.isShooting()) {
            this.shootProgress += 0.05F; // 1 second transition
            if (this.shootProgress > 1.0F) this.shootProgress = 1.0F;
        } else if (this.isNoMask()) {
            this.shootProgress = 1.0F;
        } else {
            this.shootProgress = 0.0F;
        }
        
        this.prevAggroProgress = this.aggroProgress;
        if (this.dataManager.get(AGGRO)) {
            this.aggroProgress += 0.15F;
            if (this.aggroProgress > 1.0F) this.aggroProgress = 1.0F;
        } else {
            this.aggroProgress -= 0.15F;
            if (this.aggroProgress < 0.0F) this.aggroProgress = 0.0F;
        }
        
        this.prevSlashProgress = this.slashProgress;
        if (this.slashTimer > 0) {
            this.slashTimer--;
            this.slashProgress = 1.0F - ((float)this.slashTimer / 12.0F);
        } else {
            this.slashProgress = 0.0F;
            this.prevSlashProgress = 0.0F;
        }
        
        this.prevAttackProgress = this.attackProgress;
        if (this.attackTimer > 0) {
            this.attackTimer--;
            if (this.attackTimer > 16) {
                this.attackProgress += 0.35F; // 3 ticks to snap up
            } else if (this.attackTimer > 10) {
                this.attackProgress = 1.0F; // Hold the bite for 6 ticks
            } else {
                this.attackProgress -= 0.1F; // 10 ticks to gradually release
            }
            if (this.attackProgress > 1.0F) this.attackProgress = 1.0F;
            if (this.attackProgress < 0.0F) this.attackProgress = 0.0F;
        } else {
            this.attackProgress = 0.0F;
        }

        if (this.world.isRemote) {
            if (this.isShooting() || this.isNoMask()) {
                this.motionX = 0;
                this.motionZ = 0;
            }
        }

        if (!this.world.isRemote) {
            if (this.getAttackTarget() != null) {
                if (!this.dataManager.get(AGGRO)) this.dataManager.set(AGGRO, true);
            } else {
                if (this.dataManager.get(AGGRO)) this.dataManager.set(AGGRO, false);
            }

            if (this.isTamed() && this.ticksExisted % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(0.5F);
            }

            boolean isSuiciding = this.preShootTimer > 0 || this.shootTimer > 0 || this.suicideTimer > 0;

            if (this.suicideTimer > 0) {
                this.suicideTimer--;
                if (this.suicideTimer <= 0) {
                    this.attackEntityFrom(DamageSource.GENERIC, 1000.0F);
                    return;
                }
            } else if (this.shootTimer > 0) {
                this.shootTimer--;
                if (this.shootTimer <= 0) {
                    this.dataManager.set(SHOOTING, false);
                    this.dataManager.set(NO_MASK, true);
                    
                    EntityFrostlingMask maskProj = new EntityFrostlingMask(this.world, this);
                    maskProj.setPosition(this.posX, this.posY + this.getEyeHeight(), this.posZ);
                    maskProj.shoot(this, this.rotationPitch, this.rotationYawHead, 0.0F, 1.5F, 1.0F);
                    this.world.spawnEntity(maskProj);
                    
                    this.suicideTimer = 60;
                }
            } else if (this.preShootTimer > 0) {
                this.preShootTimer--;
                if (this.preShootTimer <= 0) {
                    this.dataManager.set(SHOOTING, true);
                    this.shootTimer = 40;
                }
            } else if (this.getHealth() < this.getMaxHealth() * 0.20F && !this.isNoMask() && !this.isShooting() && this.suicideTimer <= 0) {
                this.preShootTimer = 20;
                isSuiciding = true;
            }

            if (isSuiciding) {
                this.getNavigator().clearPath();
                this.motionX = 0;
                this.motionZ = 0;
            }

            if (this.biteCooldown > 0) {
                this.biteCooldown--;
            }

            if (!this.isTamed() && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                this.setDead();
                return;
            }

            boolean isCurrentlyRolling = this.isRolling();
            boolean wantsToRoll = isCurrentlyRolling;
            
            if (isSuiciding) {
                wantsToRoll = false;
            } else if (!this.getNavigator().noPath()) {
                Path path = this.getNavigator().getPath();
                if (path != null) {
                    double targetX = path.getFinalPathPoint().x;
                    double targetY = path.getFinalPathPoint().y;
                    double targetZ = path.getFinalPathPoint().z;
                    double distSq = this.getDistanceSq(targetX, targetY, targetZ);
                    
                    if (isCurrentlyRolling) {
                        if (distSq < 4.0D) { // Stop if within 2 blocks
                            wantsToRoll = false;
                        }
                    } else {
                        if (distSq > 16.0D) { // Start if further than 4 blocks
                            wantsToRoll = true;
                        }
                    }
                }
            } else {
                wantsToRoll = false;
            }

            if (wantsToRoll) {
                this.rollTimeout = 10;
            } else if (this.rollTimeout > 0) {
                this.rollTimeout--;
                wantsToRoll = true;
            }

            if (this.dataManager.get(ROLLING) != wantsToRoll) {
                this.dataManager.set(ROLLING, wantsToRoll);
            }
            
            if (wantsToRoll) {
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.275D);
            } else {
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
            }
        }
    }

    public boolean isRolling() {
        return this.dataManager.get(ROLLING);
    }

    public boolean isNoMask() {
        return this.dataManager.get(NO_MASK);
    }

    public boolean isShooting() {
        return this.dataManager.get(SHOOTING);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (this.isTamed()) {
            if (this.isOwner(player) && !this.world.isRemote && !this.isBreedingItem(itemstack)) {
                this.aiSit.setSitting(!this.isSitting());
                this.isJumping = false;
                this.navigator.clearPath();
                this.setAttackTarget(null);
            }
        } else if (itemstack.getItem() == Items.NETHER_STAR) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }

            if (!this.world.isRemote) {
                this.setTamedBy(player);
                this.navigator.clearPath();
                this.setAttackTarget(null);
                this.aiSit.setSitting(true);
                this.setHealth(this.getMaxHealth());
                this.playTameEffect(true);
                this.world.setEntityState(this, (byte)7);
            }

            return true;
        }

        return super.processInteract(player, hand);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed() && this.ticksExisted > 2400;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.biteCooldown <= 0) {
            this.biteCooldown = 200; // 10 second cooldown
            this.attackTimer = 20; // 20 tick animation
            this.world.setEntityState(this, (byte) 4);
            float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage * 1.5F); // Bites hurt a bit more
            if (flag && entityIn instanceof net.minecraft.entity.EntityLivingBase) {
                ((net.minecraft.entity.EntityLivingBase) entityIn).addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.SLOWNESS, 40, 0));
            }
            return flag;
        } else {
            // Standard attack while biting is on cooldown
            this.world.setEntityState(this, (byte) 5);
            this.slashTimer = 12;
            this.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 4) {
            this.attackTimer = 20; // 20 tick animation
            this.attackProgress = 0.0F;
            this.prevAttackProgress = 0.0F;
        } else if (id == 5) {
            this.slashTimer = 12;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        
        if (pos.getY() >= 60 || this.world.canBlockSeeSky(pos)) {
            return false;
        }
        
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return false;
        }
        
        if (this.world.getLightFor(EnumSkyBlock.BLOCK, pos) > 7) {
            return false;
        }
        
        if (!this.world.getBlockState(pos.down()).isSideSolid(this.world, pos.down(), net.minecraft.util.EnumFacing.UP)) {
            return false;
        }
        
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox()) && 
               this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && 
               !this.world.containsAnyLiquid(this.getEntityBoundingBox());
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }
}
