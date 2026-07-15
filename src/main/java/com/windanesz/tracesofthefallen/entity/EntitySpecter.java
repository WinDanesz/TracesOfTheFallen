package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class EntitySpecter extends EntityMob implements IEntityOwnable {

    protected static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(EntitySpecter.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<String> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntitySpecter.class, DataSerializers.STRING);
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/specter");
    private EntityLivingBase cachedOwner;
    private int lifespan = 3600;

    public EntitySpecter(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.8F);
        this.isImmuneToFire = true;
        this.moveHelper = new SpecterMoveHelper(this);
        this.setNoGravity(true);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(4, new AIAttack(this));
        this.tasks.addTask(5, new AISpecterFollowOwner(this, 1.0D, 5.0F, 3.0F));
        this.tasks.addTask(6, new AIRandomFly(this));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.targetTasks.addTask(1, new AISpecterOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new AISpecterOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(4, new AIFindPlayerToAttack(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.specterMaxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.specterAttackDamage);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, false);
        this.dataManager.register(OWNER_UNIQUE_ID, "");
    }

    @Override
    @Nullable
    public UUID getOwnerId() {
        try {
            String s = this.dataManager.get(OWNER_UNIQUE_ID);
            return s.isEmpty() ? null : UUID.fromString(s);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public void setOwnerId(@Nullable UUID ownerId) {
        this.dataManager.set(OWNER_UNIQUE_ID, ownerId == null ? "" : ownerId.toString());
    }

    @Override
    @Nullable
    public EntityLivingBase getOwner() {
        try {
            UUID uuid = this.getOwnerId();
            if (uuid == null) return null;
            if (this.cachedOwner != null && !this.cachedOwner.isDead && uuid.equals(this.cachedOwner.getUniqueID())) {
                return this.cachedOwner;
            }
            EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
            if (player != null) {
                this.cachedOwner = player;
                return player;
            }
            for (Entity entity : this.world.loadedEntityList) {
                if (entity instanceof EntityLivingBase && uuid.equals(entity.getUniqueID())) {
                    this.cachedOwner = (EntityLivingBase) entity;
                    return this.cachedOwner;
                }
            }
            return null;
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public void setOwner(@Nullable EntityLivingBase owner) {
        this.cachedOwner = owner;
        this.setOwnerId(owner == null ? null : owner.getUniqueID());
    }

    public boolean isOwner(Entity entityIn) {
        return entityIn != null && entityIn == this.getOwner();
    }

    public void setAttacking(boolean attacking) {
        this.dataManager.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (this.isOwner(entitylivingbaseIn)) {
            super.setAttackTarget(null);
            return;
        }
        if (this.getOwner() instanceof com.windanesz.tracesofthefallen.entity.EntityGoblinShaman && com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells.isAlly((com.windanesz.tracesofthefallen.entity.EntityGoblinShaman) this.getOwner(), entitylivingbaseIn)) {
            super.setAttackTarget(null);
            return;
        }
        super.setAttackTarget(entitylivingbaseIn);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (source.getTrueSource() != null) {
            if (this.isOwner(source.getTrueSource())) {
                return false;
            }
            if (this.getOwner() instanceof com.windanesz.tracesofthefallen.entity.EntityGoblinShaman && com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells.isAlly((com.windanesz.tracesofthefallen.entity.EntityGoblinShaman) this.getOwner(), source.getTrueSource())) {
                return false;
            }
        }
        if (source.getTrueSource() instanceof EntityPlayer) {
            return super.attackEntityFrom(source, amount);
        }
        if (source.isProjectile() || source.isMagicDamage()) {
            return super.attackEntityFrom(source, amount);
        }
        if (source == DamageSource.IN_WALL) {
            return false;
        }
        return false;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        EntityLivingBase owner = this.getOwner();
        return entityIn.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, owner != null ? owner : this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.noClip = true;
        if (!this.world.isRemote && this.getOwnerId() != null) {
            if (--this.lifespan <= 0 || (this.getOwner() == null && this.ticksExisted > 600)) {
                if (this.world instanceof WorldServer) {
                    ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 1.0D, this.posZ, 20, 0.3D, 0.5D, 0.3D, 0.05D);
                }
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_VEX_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
                this.setDead();
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Lifespan", this.lifespan);
        if (this.getOwnerId() == null) {
            compound.setString("OwnerUUID", "");
        } else {
            compound.setString("OwnerUUID", this.getOwnerId().toString());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Lifespan")) {
            this.lifespan = compound.getInteger("Lifespan");
        }
        if (compound.hasKey("OwnerUUID", 8)) {
            String s = compound.getString("OwnerUUID");
            if (!s.isEmpty()) {
                this.setOwnerId(UUID.fromString(s));
            }
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT_TABLE;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox())
                && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()
                && !this.world.containsAnyLiquid(this.getEntityBoundingBox());
    }

    @Override
    public int getTalkInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.SPECTER_HURT;
    }

    @Override
    public boolean isEntityUndead() {
        return true;
    }

    @Override
    public float getEyeHeight() {
        return 1.6F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState landedState, BlockPos pos) {
    }

// ... (keep existing imports)

// ... (inside EntitySpecter class)
        static class AIAttack extends EntityAIBase {
            private final EntitySpecter parentEntity;
            private int attackCooldown;
            private int dashCooldown;

            public AIAttack(EntitySpecter specter) {
                this.parentEntity = specter;
                this.setMutexBits(1);
            }

            @Override
            public boolean shouldExecute() {
                return this.parentEntity.getAttackTarget() != null;
            }

            @Override
            public boolean shouldContinueExecuting() {
                return this.parentEntity.getAttackTarget() != null && this.parentEntity.getAttackTarget().isEntityAlive();
            }

            @Override
            public void startExecuting() {
                this.parentEntity.setAttacking(true);
                this.attackCooldown = 0;
                this.dashCooldown = 0;
            }

            @Override
            public void resetTask() {
                this.parentEntity.setAttacking(false);
                if (this.parentEntity.getMoveHelper() instanceof SpecterMoveHelper) {
                    ((SpecterMoveHelper) this.parentEntity.getMoveHelper()).startCooldown(0);
                }
            }

            @Override
            public void updateTask() {
                EntityLivingBase target = this.parentEntity.getAttackTarget();
                if (target == null) return;

                if (this.dashCooldown > 0) {
                    --this.dashCooldown;
                } else {
                    this.parentEntity.getLookHelper().setLookPositionWithEntity(target, 10.0F, 10.0F);
                    this.parentEntity.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1.0D);

                    if (this.attackCooldown > 0) {
                        --this.attackCooldown;
                    }

                    double distanceSq = this.parentEntity.getDistanceSq(target);
                    if (distanceSq < 4.0D && this.attackCooldown <= 0) {
                        this.parentEntity.attackEntityAsMob(target);
                        this.attackCooldown = 20;
                        this.dashCooldown = 20;

                        if (target instanceof EntityPlayer) {
                            Random random = this.parentEntity.getRNG();
                            EntityPlayer player = (EntityPlayer) target;
                            Vec3d playerLook = player.getLookVec();

                            Vec3d forward = new Vec3d(playerLook.x, 0, playerLook.z).normalize();
                            Vec3d right = new Vec3d(-playerLook.z, 0, playerLook.x).normalize();

                            Vec3d[] directions = {
                                forward,
                                forward.scale(-1),
                                right,
                                right.scale(-1)
                            };

                            Vec3d chosenDir = directions[random.nextInt(directions.length)];

                            double distance = 2.0; //+ random.nextDouble() * 1.0; // 2-3 blocks from player

                            double destX = player.posX + chosenDir.x * distance;
                            double destY = player.posY + 1.0;
                            double destZ = player.posZ + chosenDir.z * distance;

                            double moveVecX = destX - this.parentEntity.posX;
                            double moveVecY = destY - this.parentEntity.posY;
                            double moveVecZ = destZ - this.parentEntity.posZ;
                            double moveLen = MathHelper.sqrt(moveVecX*moveVecX + moveVecY*moveVecY + moveVecZ*moveVecZ);

                            if (moveLen > 0) {
                                double dashSpeed = 1.8;
                                this.parentEntity.motionX = (moveVecX / moveLen) * dashSpeed;
                                this.parentEntity.motionY = (moveVecY / moveLen) * dashSpeed * 0.1;
                                this.parentEntity.motionZ = (moveVecZ / moveLen) * dashSpeed;

                                if (this.parentEntity.getMoveHelper() instanceof SpecterMoveHelper) {
                                    ((SpecterMoveHelper) this.parentEntity.getMoveHelper()).startCooldown(10);
                                }
                            }
                        }
                    }
                }
            }
        }

    static class AIFindPlayerToAttack extends EntityAINearestAttackableTarget<EntityPlayer> {
        public AIFindPlayerToAttack(EntityCreature mob) {
            super(mob, EntityPlayer.class, true);
        }

        @Override
        public boolean shouldExecute() {
            if (((EntitySpecter)this.taskOwner).getOwnerId() != null) {
                return false;
            }
            return super.shouldExecute() && this.target != null;
        }
    }

    static class AIRandomFly extends EntityAIBase {
        private final EntitySpecter parentEntity;

        public AIRandomFly(EntitySpecter specter) {
            this.parentEntity = specter;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.parentEntity.getOwnerId() != null && this.parentEntity.getRNG().nextInt(30) != 0) {
                return false;
            }
            if (this.parentEntity.getOwnerId() != null && this.parentEntity.getAttackTarget() == null && this.parentEntity.getOwner() != null && this.parentEntity.getDistanceSq(this.parentEntity.getOwner()) > 64.0D) {
                return false;
            }
            EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();
            if (!entitymovehelper.isUpdating()) {
                return true;
            } else {
                double d0 = entitymovehelper.getX() - this.parentEntity.posX;
                double d1 = entitymovehelper.getY() - this.parentEntity.posY;
                double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            Random random = this.parentEntity.getRNG();
            double d0 = this.parentEntity.posX + (double) ((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
            double d1 = this.parentEntity.posY + (double) ((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
            double d2 = this.parentEntity.posZ + (double) ((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
            this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
        }
    }

    static class AISpecterFollowOwner extends EntityAIBase {
        private final EntitySpecter specter;
        private EntityLivingBase owner;
        private final World world;
        private final double followSpeed;
        private final float minDist;
        private final float maxDist;
        private int timeToRecalcPath;

        public AISpecterFollowOwner(EntitySpecter specter, double speed, float min, float max) {
            this.specter = specter;
            this.world = specter.world;
            this.followSpeed = speed;
            this.minDist = min;
            this.maxDist = max;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase owner = this.specter.getOwner();
            if (owner == null) {
                return false;
            } else if (this.specter.getDistanceSq(owner) < (double) (this.minDist * this.minDist)) {
                return false;
            } else {
                this.owner = owner;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.specter.getDistanceSq(this.owner) > (double) (this.maxDist * this.maxDist);
        }

        @Override
        public void startExecuting() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void resetTask() {
            this.owner = null;
            this.specter.getMoveHelper().action = EntityMoveHelper.Action.WAIT;
        }

        @Override
        public void updateTask() {
            this.specter.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.specter.getVerticalFaceSpeed());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                this.specter.getMoveHelper().setMoveTo(this.owner.posX, this.owner.posY + 1.5D, this.owner.posZ, this.followSpeed);
            }
        }
    }

    static class AISpecterOwnerHurtByTarget extends EntityAITarget {
        EntitySpecter specter;
        EntityLivingBase attacker;
        private int timestamp;

        public AISpecterOwnerHurtByTarget(EntitySpecter specter) {
            super(specter, false);
            this.specter = specter;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase owner = this.specter.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.attacker = owner.getRevengeTarget();
                int i = owner.getRevengeTimer();
                return i != this.timestamp && this.isSuitableTarget(this.attacker, false);
            }
        }

        @Override
        public void startExecuting() {
            this.taskOwner.setAttackTarget(this.attacker);
            EntityLivingBase owner = this.specter.getOwner();
            if (owner != null) {
                this.timestamp = owner.getRevengeTimer();
            }
            super.startExecuting();
        }
    }

    static class AISpecterOwnerHurtTarget extends EntityAITarget {
        EntitySpecter specter;
        EntityLivingBase target;
        private int timestamp;

        public AISpecterOwnerHurtTarget(EntitySpecter specter) {
            super(specter, false);
            this.specter = specter;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase owner = this.specter.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.target = owner.getLastAttackedEntity();
                int i = owner.getLastAttackedEntityTime();
                return i != this.timestamp && this.isSuitableTarget(this.target, false);
            }
        }

        @Override
        public void startExecuting() {
            this.taskOwner.setAttackTarget(this.target);
            EntityLivingBase owner = this.specter.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastAttackedEntityTime();
            }
            super.startExecuting();
        }
    }

    static class SpecterMoveHelper extends EntityMoveHelper {
        private final EntitySpecter parentEntity;
        private int cooldown;

        public SpecterMoveHelper(EntitySpecter specter) {
            super(specter);
            this.parentEntity = specter;
        }

        public void startCooldown(int ticks) {
            this.cooldown = ticks;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.cooldown > 0) {
                --this.cooldown;
                return;
            }

            if (this.action == EntityMoveHelper.Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 < 0.1D) {
                    this.action = Action.WAIT;
                    this.parentEntity.motionX = 0.0D;
                    this.parentEntity.motionY = 0.0D;
                    this.parentEntity.motionZ = 0.0D;
                    return;
                }

                d3 = (double) MathHelper.sqrt(d3);

                d0 /= d3;
                d1 /= d3;
                d2 /= d3;

                float speed = (float) (this.speed * this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());

                this.parentEntity.motionX = d0 * speed;
                this.parentEntity.motionY = d1 * speed;
                this.parentEntity.motionZ = d2 * speed;

                float f = (float) (MathHelper.atan2(this.parentEntity.motionZ, this.parentEntity.motionX) * (180D / Math.PI)) - 90.0F;
                this.parentEntity.rotationYaw = this.limitAngle(this.parentEntity.rotationYaw, f, 90.0F);
                this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;

            } else {
                this.parentEntity.motionX *= 0.5D;
                this.parentEntity.motionY *= 0.5D;
                this.parentEntity.motionZ *= 0.5D;
            }
        }
    }
}

