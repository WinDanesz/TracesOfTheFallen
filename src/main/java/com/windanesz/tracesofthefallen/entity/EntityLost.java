package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityLost extends EntityMob {

    protected static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(EntityLost.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Integer> LUNGE_STATE = EntityDataManager.createKey(EntityLost.class, DataSerializers.VARINT);
    protected static final DataParameter<Boolean> EXCITED = EntityDataManager.createKey(EntityLost.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> LAYING_DOWN = EntityDataManager.createKey(EntityLost.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/lost");
    
    public float mouthOpenProgress;
    public float prevMouthOpenProgress;
    
    public int lungeTicks = 0;
    public int lungeCooldown = 60 + (int)(Math.random() * 60); // 3-6 seconds initial cooldown before first lunge
    public boolean hasHitInCurrentLunge = false;
    private int clientPrevLungeState = 0;
    
    public int grabTicks = 0;
    
    // Timer for how long to lay down
    public int layDownTimer = 0;
    
    public float layDownProgress = 0.0F;
    public float prevLayDownProgress = 0.0F;
    
    public int digCooldown = 0;
    public boolean hasRolledForBoneThisDig = false;

    public EntityLost(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.55F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, net.minecraft.entity.EntityLivingBase.class, 10, true, false, new com.google.common.base.Predicate<net.minecraft.entity.EntityLivingBase>() {
            @Override
            public boolean apply(net.minecraft.entity.EntityLivingBase input) {
                return EntityLost.isEdible(input);
            }
        }));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, net.minecraft.entity.player.EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25.0D);
    }

    @Override
    public boolean getCanSpawnHere() {
        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        
        // Only spawn at mining depth (diamond level by default), away from sky access, and never in the Nether/End
        if (pos.getY() < Settings.mobSettings.lostSpawnMinY || pos.getY() > Settings.mobSettings.lostSpawnMaxY
                || !this.world.provider.isSurfaceWorld() || this.world.canBlockSeeSky(pos)) {
            return false;
        }
        
        return super.getCanSpawnHere();
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        return com.windanesz.tracesofthefallen.init.ModSounds.LOST_AMBIENT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource damageSourceIn) {
        return com.windanesz.tracesofthefallen.init.ModSounds.LOST_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return com.windanesz.tracesofthefallen.init.ModSounds.LOST_DEATH;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, false);
        this.dataManager.register(LUNGE_STATE, Integer.valueOf(0));
        this.dataManager.register(EXCITED, false);
        this.dataManager.register(LAYING_DOWN, false);
    }

    public void setAttacking(boolean attacking) {
        this.dataManager.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }
    
    public void setLayingDown(boolean laying) {
        this.dataManager.set(LAYING_DOWN, laying);
    }
    
    public boolean isLayingDown() {
        return this.dataManager.get(LAYING_DOWN);
    }

    public int getLungeState() {
        return this.dataManager.get(LUNGE_STATE).intValue();
    }

    public void setLungeState(int state) {
        this.dataManager.set(LUNGE_STATE, Integer.valueOf(state));
    }

    public boolean isExcited() {
        return this.dataManager.get(EXCITED);
    }

    public static boolean isEdible(Entity entity) {
        if (entity == null || entity instanceof EntityLost) return false;
        ResourceLocation res = EntityList.getKey(entity);
        if (res == null) return false;
        String name = res.toString();
        for (String s : Settings.goblinSettings.lostEdibleEntities) {
            if (s.equals(name)) return true;
        }
        return false;
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (entityIn instanceof EntityLost) {
            return true;
        }
        return super.isOnSameTeam(entityIn);
    }

    @Override
    protected boolean isMovementBlocked() {
        return this.getLungeState() != 0 || !this.getPassengers().isEmpty() || super.isMovementBlocked();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (this.getLungeState() != 0) {
            this.fallDistance = 0.0F;
            return;
        }
        super.fall(distance, damageMultiplier);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getLungeState() != 0) {
            return false;
        }
        
        if (isEdible(entityIn) && this.getPassengers().isEmpty()) {
            if (entityIn.startRiding(this, true)) {
                this.grabTicks = 0;
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, this.getSoundCategory(), 1.0F, 0.8F);
                return true;
            }
        }
        
        return super.attackEntityAsMob(entityIn);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            double forwardOffset;
            double verticalOffset;
            
            if (this.grabTicks <= 30) {
                // First 1.5 seconds: Hold still in front (waiting)
                forwardOffset = 0.6D;
                verticalOffset = 0.0D;
            } else if (this.grabTicks <= 70) {
                // Next 2.0 seconds: pull towards mouth horizontally
                double progress = (this.grabTicks - 30) / 40.0D;
                forwardOffset = 0.6D - (0.25D * progress); // 0.6 -> 0.35
                verticalOffset = 0.0D;
            } else {
                // Last 0.5 seconds: swallow horizontally with a tiny dip
                double swallowProgress = (this.grabTicks - 70) / 10.0D;
                forwardOffset = 0.35D - (0.35D * swallowProgress); // 0.35 -> 0.0
                verticalOffset = 0.0D - (0.15D * swallowProgress); // 0.0 -> -0.15
            }
            
            // Adjust based on Lost's rotation
            double d0 = Math.cos(this.renderYawOffset * 0.017453292D);
            double d1 = Math.sin(this.renderYawOffset * 0.017453292D);
            
            passenger.setPosition(this.posX - d1 * forwardOffset, this.posY + verticalOffset, this.posZ + d0 * forwardOffset);
            
            // Force passenger to stare in horror at the Lost
            float targetYaw = this.renderYawOffset + 180.0F;
            passenger.rotationYaw = targetYaw;
            passenger.prevRotationYaw = targetYaw;
            if (passenger instanceof EntityLivingBase) {
                ((EntityLivingBase) passenger).renderYawOffset = targetYaw;
                ((EntityLivingBase) passenger).prevRenderYawOffset = targetYaw;
                ((EntityLivingBase) passenger).rotationYawHead = targetYaw;
                ((EntityLivingBase) passenger).prevRotationYawHead = targetYaw;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public int hurtFaceTicks;

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 2) {
            this.hurtFaceTicks = 20; // 1 second of idle face
        }
        super.handleStatusUpdate(id);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        
        if (this.digCooldown > 0) {
            this.digCooldown--;
        }
        
        if (this.isLayingDown()) {
            // Freeze all rotation while laying down so they don't spin around like a record
            this.rotationYaw = this.prevRotationYaw;
            this.rotationYawHead = this.prevRotationYawHead;
            this.renderYawOffset = this.prevRenderYawOffset;
            this.rotationPitch = this.prevRotationPitch;
        }
        
        if (this.world.isRemote && this.hurtFaceTicks > 0) {
            this.hurtFaceTicks--;
        }
        
        boolean isDiggingPhase = false;
        
        // Procedural digging particles (mirrors the ModelLost animation)
        if (!this.isLayingDown() && !this.isAttacking() && this.getPassengers().isEmpty()) {
            float investigateDelt = net.minecraft.util.math.MathHelper.sin(this.ticksExisted * 0.015F) * net.minecraft.util.math.MathHelper.sin(this.ticksExisted * 0.04F);
            float digCycle = net.minecraft.util.math.MathHelper.sin(this.ticksExisted * 0.005F);
            
            if (investigateDelt > 0.3F && digCycle > 0.0F) {
                isDiggingPhase = true;
                float scratchAnim = net.minecraft.util.math.MathHelper.sin(this.ticksExisted * 1.5F);
                // When scratchAnim strikes near the lowest point (-1.0), spawn particles
                if (scratchAnim < -0.8F) {
                    if (this.world.isRemote) {
                        if (this.rand.nextInt(2) == 0) {
                            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.getPosition().down());
                            if (state.getMaterial() != net.minecraft.block.material.Material.AIR) {
                                for (int i = 0; i < 4; i++) {
                                    // Calculate hand position roughly (in front of entity)
                                    double px = this.posX + (this.rand.nextDouble() - 0.5D) * 0.6D + this.getLookVec().x * 0.5D;
                                    double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D + this.getLookVec().z * 0.5D;
                                    this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.BLOCK_CRACK, px, this.posY + 0.1D, pz, 
                                        (this.rand.nextDouble() - 0.5D) * 0.15D, 
                                        this.rand.nextDouble() * 0.2D, 
                                        (this.rand.nextDouble() - 0.5D) * 0.15D, 
                                        Block.getStateId(state));
                                }
                            }
                        }
                    } else {
                        if (!this.hasRolledForBoneThisDig) {
                            this.hasRolledForBoneThisDig = true;
                            if (this.digCooldown <= 0 && this.rand.nextFloat() < 0.05F) {
                                net.minecraft.entity.item.EntityItem bone = new net.minecraft.entity.item.EntityItem(this.world, this.posX + this.getLookVec().x * 0.5D, this.posY + 0.1D, this.posZ + this.getLookVec().z * 0.5D, new net.minecraft.item.ItemStack(net.minecraft.init.Items.BONE));
                                this.world.spawnEntity(bone);
                                this.digCooldown = 2400; // 2 minutes cooldown
                            }
                        }
                    }
                }
            }
        }
        
        if (!isDiggingPhase) {
            this.hasRolledForBoneThisDig = false;
        }
        
        this.prevLayDownProgress = this.layDownProgress;
        if (this.isLayingDown()) {
            if (this.layDownProgress < 1.0F) {
                this.layDownProgress += 0.05F; // 1 second to lay down
            }
            if (this.layDownProgress > 1.0F) this.layDownProgress = 1.0F;
        } else {
            if (this.layDownProgress > 0.0F) {
                this.layDownProgress -= 0.1F; // 0.5 seconds to stand up
            }
            if (this.layDownProgress < 0.0F) this.layDownProgress = 0.0F;
        }
        
        if (!this.getPassengers().isEmpty()) {
            this.grabTicks++;
            
            // Spawn blood and flesh particles during the final chewing/swallowing phase
            if (this.world.isRemote && this.grabTicks > 50) {
                Entity passenger = this.getPassengers().get(0);
                for (int i = 0; i < 5; i++) {
                    double px = passenger.posX + (this.rand.nextDouble() - 0.5D) * passenger.width;
                    double py = passenger.posY + (this.rand.nextDouble() * passenger.height);
                    double pz = passenger.posZ + (this.rand.nextDouble() - 0.5D) * passenger.width;
                    
                    // Blood drops
                    this.world.spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, 0, 0);
                    
                    if (this.rand.nextBoolean()) {
                        // Chunks of flesh (Nether Wart Block)
                        this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, px, py, pz,
                            (this.rand.nextDouble() - 0.5D) * 0.2D, 
                            (this.rand.nextDouble() * 0.2D), 
                            (this.rand.nextDouble() - 0.5D) * 0.2D, 
                            Block.getStateId(Blocks.NETHER_WART_BLOCK.getDefaultState()));
                    }
                }
            }
        } else {
            this.grabTicks = 0;
        }
        
        if (!this.world.isRemote) {
            boolean hasTarget = this.getAttackTarget() != null;
            this.setAttacking(hasTarget || !this.getPassengers().isEmpty());
            
            boolean isExcited = hasTarget && isEdible(this.getAttackTarget());
            if (this.dataManager.get(EXCITED) != isExcited) {
                this.dataManager.set(EXCITED, isExcited);
            }
            
            if (this.getPassengers().isEmpty()) {
                this.updateMobCombat();
                
                // Laying down logic
                if (!hasTarget) {
                    if (this.layDownTimer > 0) {
                        this.layDownTimer--;
                        this.setLayingDown(true);
                        // Stop moving
                        this.motionX = 0;
                        this.motionZ = 0;
                        if (this.getNavigator() != null) {
                            this.getNavigator().clearPath();
                        }
                    } else {
                        this.setLayingDown(false);
                        if (this.rand.nextInt(600) == 0) { // roughly every 30 seconds
                            this.layDownTimer = 300 + this.rand.nextInt(600); // Lay down for 15-45 seconds
                        }
                    }
                } else {
                    this.setLayingDown(false);
                    this.layDownTimer = 0;
                }
            } else {
                // We are holding something!
                Entity passenger = this.getPassengers().get(0);
                
                // Stop moving while eating
                this.motionX = 0;
                this.motionZ = 0;
                if (this.getNavigator() != null) {
                    this.getNavigator().clearPath();
                }
                
                // Deal 1 damage every 40 ticks, starting at tick 1
                if (this.grabTicks % 40 == 1) {
                    passenger.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0F);
                }
                
                // Play crunch sound occasionally
                if (this.grabTicks % 10 == 0) {
                    this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EAT, this.getSoundCategory(), 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
                }
                
                // Swallow
                if (this.grabTicks >= 80) {
                    // Fully heal
                    this.setHealth(this.getMaxHealth());
                    
                    // Gain Slowness II for 3 seconds
                    this.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                    
                    // Instantly remove the entity without playing a death animation
                    passenger.setDead();
                    
                    this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_BURP, this.getSoundCategory(), 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
                }
            }
        } else {
            this.updateClientCombat();
        }

        this.prevMouthOpenProgress = this.mouthOpenProgress;
        
        if (this.isAttacking() && this.hurtFaceTicks == 0) {
            this.mouthOpenProgress += (1.0F - this.mouthOpenProgress) * 0.6F;
        } else {
            this.mouthOpenProgress += (0.0F - this.mouthOpenProgress) * 0.6F;
        }
    }

    protected void updateMobCombat() {
        if (this.lungeCooldown > 0) {
            this.lungeCooldown--;
        }
        int state = this.getLungeState();
        if (state != 0) {
            this.fallDistance = 0.0F;
        }
        if (state == 1) {
            this.lungeTicks++;
            if (this.collidedHorizontally) {
                this.motionX = 0.0D;
                this.motionZ = 0.0D;
            }
            if (!this.hasHitInCurrentLunge) {
                List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.2D, 0.3D, 0.2D));
                for (EntityLivingBase target : targets) {
                    if (target != this && !this.isOnSameTeam(target) && target.isEntityAlive()) {
                        if (isEdible(target) && this.getPassengers().isEmpty()) {
                            if (target.startRiding(this, true)) {
                                this.grabTicks = 0;
                                this.setLungeState(0);
                                this.lungeTicks = 0;
                                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, this.getSoundCategory(), 1.0F, 0.8F);
                                break;
                            }
                        }
                        
                        target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.1F);
                        this.hasHitInCurrentLunge = true;
                        this.motionX *= 0.3D;
                        this.motionZ *= 0.3D;
                        break;
                    }
                }
            }
            if (this.onGround && this.lungeTicks > 5 || this.lungeTicks > 22) {
                this.setLungeState(2);
                this.lungeTicks = 0;
                this.motionX = 0.0D;
                this.motionZ = 0.0D;
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SMALL_FALL, this.getSoundCategory(), 1.0F, 0.8F);
            }
        } else if (state == 2) {
            this.lungeTicks++;
            this.motionX = 0.0D;
            this.motionZ = 0.0D;
            if (this.getNavigator() != null) {
                this.getNavigator().clearPath();
            }
            if (this.lungeTicks > 30) {
                this.setLungeState(0);
                this.lungeCooldown = 400 + this.rand.nextInt(200); // 20-30 seconds cooldown for Lost
            }
        } else if (state == 0 && this.lungeCooldown == 0 && this.onGround && this.getPassengers().isEmpty()) {
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive() && this.getEntitySenses().canSee(target)) {
                double distSq = this.getDistanceSq(target);
                if (distSq >= 2.25D && distSq <= 25.0D) {
                    this.setLungeState(1);
                    this.lungeTicks = 0;
                    this.hasHitInCurrentLunge = false;
                    double dx = target.posX - this.posX;
                    double dz = target.posZ - this.posZ;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    this.motionX = (dx / dist) * 1.15D;
                    this.motionZ = (dz / dist) * 1.15D;
                    this.motionY = 0.32D;
                    this.rotationYaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                    this.renderYawOffset = this.rotationYaw;
                    this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ZOMBIE_STEP, this.getSoundCategory(), 1.2F, 1.4F);
                }
            }
        }
    }

    protected void updateClientCombat() {
        int state = this.getLungeState();
        if (state != this.clientPrevLungeState) {
            this.clientPrevLungeState = state;
            this.lungeTicks = 0;
        }
        if (state == 1 || state == 2) {
            this.lungeTicks++;
        }
    }

    @Override
    public boolean isEntityUndead() {
        return false;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }
}
