package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityGnossic extends EntityMob {

    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/gnossic");

    private static final DataParameter<Integer> ATTACK_STATE = EntityDataManager.createKey(EntityGnossic.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DRAIN_TARGET_ID = EntityDataManager.createKey(EntityGnossic.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> IS_SPRINTING = EntityDataManager.createKey(EntityGnossic.class, DataSerializers.BOOLEAN);

    public boolean isAggro = false;
    public boolean hasReachedPlayer = false;
    public float draperyYaw;
    public float prevDraperyYaw;
    public float flightPitch;
    public float prevFlightPitch;
    public float rippleIntensity;
    public float prevRippleIntensity;

    public EntityGnossic(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
        this.isImmuneToFire = true;
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_STATE, 0); // 0: Idle, 1: Charging, 2: Draining
        this.dataManager.register(DRAIN_TARGET_ID, -1);
        this.dataManager.register(IS_SPRINTING, false);
    }

    public int getAttackState() {
        return this.dataManager.get(ATTACK_STATE);
    }

    public void setAttackState(int state) {
        this.dataManager.set(ATTACK_STATE, state);
    }

    public int getDrainTargetId() {
        return this.dataManager.get(DRAIN_TARGET_ID);
    }

    public void setDrainTargetId(int id) {
        this.dataManager.set(DRAIN_TARGET_ID, id);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D); // Cannot be knocked back
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D); // Deals no physical damage
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AIGnossicDrain(this));
        this.tasks.addTask(2, new AIGnossicApproach(this));
        this.tasks.addTask(3, new AIGnossicStalk(this));
        this.tasks.addTask(4, new AIGnossicWatchClosest(this, 40.0F));
        this.tasks.addTask(5, new AIGnossicLookIdle(this));
        this.tasks.addTask(6, new EntityAIWander(this, 0.35D)); // Very slow wander

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void onDeathUpdate() {
        if (this.deathTime == 0) {
            this.setAttackState(0);
            this.setDrainTargetId(-1);
            if (!this.world.isRemote) {
                this.dataManager.set(IS_SPRINTING, false);
            }
        }
        
        ++this.deathTime;
        
        if (this.deathTime == 1 && this.world.isRemote) {
            // Dense smoke cloud when the body disappears
            for (int i = 0; i < 60; ++i) {
                double mx = this.rand.nextGaussian() * 0.02D;
                double my = this.rand.nextGaussian() * 0.02D;
                double mz = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 
                    this.posY + (double)(this.rand.nextFloat() * this.height), 
                    this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 
                    mx, my, mz);
            }
        }
        
        if (this.deathTime == 80) {
            if (!this.world.isRemote && (this.recentlyHit > 0 && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot"))) {
                int i = this.getExperiencePoints(this.attackingPlayer);
                while (i > 0) {
                    int j = EntityXPOrb.getXPSplit(i);
                    i -= j;
                    this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
                }
            }
            this.setDead();
            
            if (this.world.isRemote) {
                // Small puff of smoke when the mask finally vanishes from the floor
                for (int k = 0; k < 20; ++k) {
                    double mx = this.rand.nextGaussian() * 0.02D;
                    double my = this.rand.nextGaussian() * 0.02D;
                    double mz = this.rand.nextGaussian() * 0.02D;
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        this.posX + (double)(this.rand.nextFloat() * this.width), 
                        this.posY + 0.1D, 
                        this.posZ + (double)(this.rand.nextFloat() * this.width), 
                        mx, my, mz);
                }
            }
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isDaytime() && !this.world.isRemote && Settings.mobSettings.gnossicBurnsInSun) {
            float f = this.getBrightness();
            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ))) {
                boolean flag = true;
                ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                if (!itemstack.isEmpty()) {
                    if (itemstack.isItemStackDamageable()) {
                        itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));
                        if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
                            this.renderBrokenItemStack(itemstack);
                            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                    flag = false;
                }
                if (flag) {
                    this.setFire(8);
                }
            }
        }
        
        super.onLivingUpdate();

        this.prevDraperyYaw = this.draperyYaw;
        float yawDiff = MathHelper.wrapDegrees(this.renderYawOffset - this.draperyYaw);
        this.draperyYaw += yawDiff * 0.15F; // Interpolate toward body yaw by 15% each tick
        
        this.prevFlightPitch = this.flightPitch;
        boolean isSprinting = this.world.isRemote ? this.dataManager.get(IS_SPRINTING) : (this.isAggro && !this.hasReachedPlayer);
        float targetPitch = isSprinting ? 1.3F : 0.0F; // Less horizontal, more like 75 degrees
        this.flightPitch += (targetPitch - this.flightPitch) * 0.8F; // 80% interpolation to snap into horizontal flight instantly

        this.prevRippleIntensity = this.rippleIntensity;
        double speedSq = (this.posX - this.prevPosX) * (this.posX - this.prevPosX) + (this.posZ - this.prevPosZ) * (this.posZ - this.prevPosZ);
        float targetRipple = speedSq > 0.0001D ? 1.0F : 0.0F;
        this.rippleIntensity += (targetRipple - this.rippleIntensity) * 0.2F;

        // Walk on water and lava logic
        BlockPos posDown = new BlockPos(this.posX, this.getEntityBoundingBox().minY - 0.05D, this.posZ);
        IBlockState stateDown = this.world.getBlockState(posDown);
        if (stateDown.getMaterial().isLiquid()) {
            if (this.motionY < 0.0D) {
                this.motionY = 0.0D;
                this.onGround = true;
                this.fallDistance = 0.0F;
            }
            // Keep them on top
            if (this.world.getBlockState(new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ)).getMaterial().isLiquid()) {
                this.motionY = 0.1D;
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            int state = this.getAttackState();
            int targetId = this.getDrainTargetId();
            if (state > 0 && targetId >= 0) {
                Entity target = this.world.getEntityByID(targetId);
                if (target != null) {
                    if (state == 1 && this.ticksExisted % 2 == 0) { // Charging: slightly more smoke
                        double px = this.posX + (this.rand.nextDouble() - 0.5D) * 0.5D;
                        double py = this.posY + this.getEyeHeight() + 0.5D; // Shifted up by 0.5
                        double pz = this.posZ + (this.rand.nextDouble() - 0.5D) * 0.5D;
                        double dx = (target.posX - px) * 0.1D;
                        double dy = (target.posY + target.height / 2.0F - py) * 0.1D;
                        double dz = (target.posZ - pz) * 0.1D;
                        this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, dx, dy, dz);
                    } else if (state == 2) { // Draining: lots of red particles every tick
                        for (int i = 0; i < 4; i++) {
                            double px = target.posX + (this.rand.nextDouble() - 0.5D) * (target.width * 0.8D);
                            double py = target.posY + target.height * 0.55D + (this.rand.nextDouble() - 0.5D) * (target.height * 0.4D);
                            double pz = target.posZ + (this.rand.nextDouble() - 0.5D) * (target.width * 0.8D);
                            TracesOfTheFallen.proxy.spawnLifestealParticle(this.world, px, py, pz, this.getEntityId());
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        // No physical push or damage
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL) {
            return false; // Immune to suffocation while noclipping
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false; // To help with the dragging sound later
    }

    // AIGnossicStalk
    public class AIGnossicStalk extends EntityAIBase {
        private final EntityGnossic gnossic;
        private int stalkTimer;
        private boolean hasBeenSeen;

        public AIGnossicStalk(EntityGnossic gnossic) {
            this.gnossic = gnossic;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (this.gnossic.isAggro) return false;
            
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null || !target.isEntityAlive()) return false;
            
            // If target is within 8 blocks, skip the stalk and flight sprint completely
            if (this.gnossic.getDistanceSq(target) <= 64.0D) {
                this.gnossic.isAggro = true;
                this.gnossic.hasReachedPlayer = true;
                return false;
            }
            
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.gnossic.isAggro && this.gnossic.getAttackTarget() != null;
        }

        @Override
        public void startExecuting() {
            this.gnossic.getNavigator().clearPath();
            this.stalkTimer = 300 + this.gnossic.getRNG().nextInt(301); // 15 to 30 seconds baseline
            this.hasBeenSeen = false;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null) return;

            // Face the player slowly (slow rotating head)
            this.gnossic.getLookHelper().setLookPositionWithEntity(target, 2.0F, 2.0F);

            // Automatically aggro and skip sprint if target walks within 8 blocks
            if (this.gnossic.getDistanceSq(target) <= 64.0D) {
                this.gnossic.isAggro = true;
                this.gnossic.hasReachedPlayer = true;
                return;
            }

            // Check if player is looking at the gnossic
            Vec3d lookVec = target.getLook(1.0F).normalize();
            Vec3d toGnossic = new Vec3d(
                    this.gnossic.posX - target.posX,
                    this.gnossic.posY + (double) this.gnossic.getEyeHeight() - (target.posY + (double) target.getEyeHeight()),
                    this.gnossic.posZ - target.posZ).normalize();

            double dot = lookVec.dotProduct(toGnossic);

            if (!this.hasBeenSeen && dot > 0.5D && target.canEntityBeSeen(this.gnossic)) {
                this.hasBeenSeen = true;
                int lookTimer = 100 + this.gnossic.getRNG().nextInt(101); // 5 to 10 seconds of stalking once seen
                if (this.stalkTimer > lookTimer) {
                    this.stalkTimer = lookTimer; // Reduce patience significantly if observed
                }
            }

            if (this.stalkTimer > 0) {
                this.stalkTimer--;
                if (this.stalkTimer <= 0) {
                    this.gnossic.isAggro = true; // Timer finished, rush them
                }
            }
        }
    }

    // AIGnossicApproach
    public class AIGnossicApproach extends EntityAIBase {
        private final EntityGnossic gnossic;
        private int erraticPauseTimer = 0;
        private int erraticPauseCooldown = 100; // 5 second initial cooldown so they don't immediately freeze

        public AIGnossicApproach(EntityGnossic gnossic) {
            this.gnossic = gnossic;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (!this.gnossic.isAggro) return false;
            if (this.gnossic.getAttackState() > 0) return false; // Don't move if charging/draining
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null || !target.isEntityAlive()) return false;
            return this.gnossic.getDistanceSq(target) > 6.25D; // Only approach if outside drain range
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute();
        }

        @Override
        public void startExecuting() {
            this.erraticPauseTimer = 0;
            this.erraticPauseCooldown = 100;
            if (!this.gnossic.hasReachedPlayer) {
                this.gnossic.dataManager.set(IS_SPRINTING, true);
            }
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target != null && this.gnossic.hasReachedPlayer) {
                this.gnossic.getNavigator().tryMoveToEntityLiving(target, 1.15D);
            }
        }

        @Override
        public void resetTask() {
            this.gnossic.dataManager.set(IS_SPRINTING, false);
            this.gnossic.noClip = false;
            this.gnossic.setNoGravity(false);
            this.gnossic.stepHeight = 0.6F;
            if (!this.gnossic.hasReachedPlayer) {
                // Kill momentum so they don't slide past the player
                this.gnossic.motionX = 0;
                this.gnossic.motionY = 0;
                this.gnossic.motionZ = 0;
            }
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null) return;
            
            this.gnossic.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            // Force the body to face the player instead of the movement direction
            this.gnossic.rotationYaw = this.gnossic.rotationYawHead;
            this.gnossic.renderYawOffset = this.gnossic.rotationYawHead;

            if (this.erraticPauseTimer > 0) {
                this.erraticPauseTimer--;
                this.gnossic.getNavigator().clearPath();
                if (this.erraticPauseTimer == 0) {
                    this.erraticPauseCooldown = 100 + this.gnossic.getRNG().nextInt(40); // 5 to 7 seconds cooldown
                }
            } else {
                if (this.erraticPauseCooldown > 0) {
                    this.erraticPauseCooldown--;
                }
                
                if (!this.gnossic.hasReachedPlayer) {
                    this.gnossic.noClip = false; // Use standard collisions
                    this.gnossic.setNoGravity(true); // But allow them to fly/glide
                    this.gnossic.stepHeight = 2.0F; // Instantly step over 1-2 block tall terrain instead of jumping
                    
                    double dx = target.posX - this.gnossic.posX;
                    double dy = (target.posY + target.getEyeHeight() * 0.5D) - (this.gnossic.posY + this.gnossic.getEyeHeight() * 0.5D);
                    double dz = target.posZ - this.gnossic.posZ;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > 0) {
                        double maxSpeed = 1.0D;
                        if (dist < maxSpeed) maxSpeed = dist; // Don't overshoot
                        this.gnossic.motionX = (dx / dist) * maxSpeed;
                        this.gnossic.motionY = (dy / dist) * maxSpeed;
                        this.gnossic.motionZ = (dz / dist) * maxSpeed;
                    }
                } else {
                    this.gnossic.noClip = false;
                    this.gnossic.setNoGravity(false);
                    this.gnossic.stepHeight = 0.6F;
                    this.gnossic.getNavigator().tryMoveToEntityLiving(target, 1.15D);
                    
                    if (this.erraticPauseCooldown == 0 && this.gnossic.getRNG().nextFloat() < 0.05F) { // 5% chance per tick to pause when cooldown is up
                        this.erraticPauseTimer = 20; // Freeze for exactly 1 second (20 ticks)
                    }
                }
            }
        }
    }

    // AIGnossicDrain
    public static class AIGnossicDrain extends EntityAIBase {
        private final EntityGnossic gnossic;
        private int attackTimer = 0;

        public AIGnossicDrain(EntityGnossic gnossic) {
            this.gnossic = gnossic;
            this.setMutexBits(3); // Stop moving while draining
        }

        @Override
        public boolean shouldExecute() {
            if (!this.gnossic.isAggro) return false;
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null || !target.isEntityAlive()) return false;
            return this.gnossic.getDistanceSq(target) <= 4.0D; // 2.0 block radius to initiate attack (forces them to chase closer)
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null || !target.isEntityAlive()) return false;
            return this.gnossic.getDistanceSq(target) <= 25.0D && this.gnossic.getAttackState() > 0; // 5.0 block radius to break the tether
        }

        @Override
        public void startExecuting() {
            this.gnossic.hasReachedPlayer = true;
            this.gnossic.setAttackState(1); // Charging
            this.attackTimer = 40; // 2 seconds charge (reduced from 3)
            
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target != null) {
                this.gnossic.setDrainTargetId(target.getEntityId());
            }
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = this.gnossic.getAttackTarget();
            if (target == null) {
                this.gnossic.setAttackState(0);
                return;
            }

            this.gnossic.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

            if (this.attackTimer > 0) {
                this.attackTimer--;
                if (this.attackTimer == 0) {
                    this.gnossic.setAttackState(2); // Draining
                }
            } else {
                // Draining phase
                if (this.gnossic.ticksExisted % 10 == 0) { // Deal damage every half second
                    DamageSource source = DamageSource.causeIndirectMagicDamage(this.gnossic, this.gnossic);
                    float damage = 2.0F; // 1 heart
                    
                    int prevResist = target.hurtResistantTime;
                    target.hurtResistantTime = Math.min(target.hurtResistantTime, 10);
                    
                    double oldMX = target.motionX;
                    double oldMY = target.motionY;
                    double oldMZ = target.motionZ;
                    
                    if (target.attackEntityFrom(source, damage)) {
                        this.gnossic.heal(1.0F); // Heal half a heart per half second (1 heart per second)
                        // Prevent knockback
                        target.motionX = oldMX;
                        target.motionY = oldMY;
                        target.motionZ = oldMZ;
                        target.velocityChanged = false;
                    } else {
                        target.hurtResistantTime = prevResist;
                    }
                }
            }
            
            // Move slowly towards target while draining/charging, but maintain personal space
            if (this.gnossic.getDistanceSq(target) > 3.24D) { // 1.8 blocks
                this.gnossic.getNavigator().tryMoveToEntityLiving(target, 0.6D);
            } else {
                this.gnossic.getNavigator().clearPath();
            }
        }

        @Override
        public void resetTask() {
            this.gnossic.setAttackState(0);
            this.gnossic.setDrainTargetId(-1);
            this.attackTimer = 0;
        }
    }

    // AIGnossicLookIdle
    public static class AIGnossicLookIdle extends EntityAIBase {
        private final EntityGnossic gnossic;
        private double lookX;
        private double lookZ;
        private int idleTime;

        public AIGnossicLookIdle(EntityGnossic gnossic) {
            this.gnossic = gnossic;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return this.gnossic.getRNG().nextFloat() < 0.02F;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.idleTime >= 0;
        }

        @Override
        public void startExecuting() {
            double d0 = (Math.PI * 2D) * this.gnossic.getRNG().nextDouble();
            this.lookX = Math.cos(d0);
            this.lookZ = Math.sin(d0);
            this.idleTime = 20 + this.gnossic.getRNG().nextInt(20);
        }

        @Override
        public void updateTask() {
            this.idleTime--;
            this.gnossic.getLookHelper().setLookPosition(this.gnossic.posX + this.lookX, this.gnossic.posY + this.gnossic.getEyeHeight(), this.gnossic.posZ + this.lookZ, 2.0F, 2.0F);
        }
    }

    // AIGnossicWatchClosest
    public static class AIGnossicWatchClosest extends EntityAIBase {
        private final EntityGnossic gnossic;
        private Entity closestEntity;
        private final float maxDistance;
        private int lookTime;

        public AIGnossicWatchClosest(EntityGnossic gnossic, float maxDistance) {
            this.gnossic = gnossic;
            this.maxDistance = maxDistance;
            this.setMutexBits(2);
        }

        @Override
        public boolean shouldExecute() {
            if (this.gnossic.getRNG().nextFloat() >= 0.02F) return false;
            
            if (this.gnossic.getAttackTarget() != null) return false;

            this.closestEntity = this.gnossic.world.findNearestEntityWithinAABB(EntityPlayer.class, this.gnossic.getEntityBoundingBox().grow(this.maxDistance, 3.0D, this.maxDistance), this.gnossic);
            return this.closestEntity != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (!this.closestEntity.isEntityAlive()) return false;
            if (this.gnossic.getDistanceSq(this.closestEntity) > this.maxDistance * this.maxDistance) return false;
            return this.lookTime > 0;
        }

        @Override
        public void startExecuting() {
            this.lookTime = 40 + this.gnossic.getRNG().nextInt(40);
        }

        @Override
        public void resetTask() {
            this.closestEntity = null;
        }

        @Override
        public void updateTask() {
            this.gnossic.getLookHelper().setLookPosition(this.closestEntity.posX, this.closestEntity.posY + this.closestEntity.getEyeHeight(), this.closestEntity.posZ, 2.0F, 2.0F);
            this.lookTime--;
        }
    }
}
