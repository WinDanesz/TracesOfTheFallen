package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class EntityGoblinNest extends Entity {

    private static final DataParameter<Integer> FACING = EntityDataManager.createKey(EntityGoblinNest.class, DataSerializers.VARINT);

    private float nestHealth = Settings.miscSettings.nestHealth;
    private int broodsSpawned = 0;
    private int spawnTimer = 0;
    private int shootTimer = 0;
    // Set for nests that belong to a place goblins are meant to live in permanently (e.g. the goblin cavern).
    private boolean persistentBroods = false;

    public EntityGoblinNest(World worldIn) {
        super(worldIn);
        this.setSize(2.0F, 1.6875F);
    }

    public EntityGoblinNest(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(FACING, EnumFacing.NORTH.getHorizontalIndex());
    }

    public EnumFacing getFacing() {
        return EnumFacing.byHorizontalIndex(this.dataManager.get(FACING));
    }

    /** Goblins hatched by (or released from) this nest never despawn naturally. */
    public void setPersistentBroods(boolean persistentBroods) {
        this.persistentBroods = persistentBroods;
    }

    public void setFacing(EnumFacing facing) {
        this.dataManager.set(FACING, facing.getHorizontalIndex());
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.getEntityBoundingBox();
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (!this.world.isRemote) {
            this.nestHealth -= amount;
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 0.8F, 1.2F);
            if (this.nestHealth <= 0) {
                this.destroyNest();
            } else if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + 0.5D, this.posZ, 10, 0.4D, 0.4D, 0.4D, 0.05D, Block.getStateId(Blocks.COBBLESTONE.getDefaultState()));
            }
        }
        return true;
    }

    private void destroyNest() {
        if (!this.world.isRemote) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + 0.5D, this.posZ, 30, 0.6D, 0.6D, 0.6D, 0.05D, Block.getStateId(Blocks.COBBLESTONE.getDefaultState()));

                int numGoblins = 1 + this.rand.nextInt(2);
                for (int i = 0; i < numGoblins; i++) {
                    EntityGoblin goblin = new EntityGoblin(this.world);
                    if (this.persistentBroods) {
                        goblin.enablePersistence();
                    }
                    goblin.setPosition(this.posX + (this.rand.nextDouble() - 0.5D) * 1.5D, this.posY + 0.2D, this.posZ + (this.rand.nextDouble() - 0.5D) * 1.5D);
                    this.world.spawnEntity(goblin);
                }
            }
            this.setDead();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity()) {
            this.motionY -= 0.04D;
        }
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.8D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.8D;
        if (this.onGround) {
            this.motionX = 0;
            this.motionZ = 0;
        }

        if (!this.world.isRemote) {
            if (this.shootTimer > 0) {
                this.shootTimer--;
            }
            if (this.spawnTimer > 0) {
                this.spawnTimer--;
            } else if (this.ticksExisted % 20 == 0) {
                this.trySpawnBrood();
            }
            if (this.shootTimer <= 0 && this.ticksExisted % 10 == 0) {
                this.tryShootRock();
            }
        }
    }

    private void tryShootRock() {
        double detectionRange = Settings.miscSettings.nestRockAttackRange;
        EntityPlayer target = null;
        double closestDist = Double.MAX_VALUE;
        EnumFacing facing = this.getFacing();

        for (EntityPlayer player : this.world.playerEntities) {
            if (player.isEntityAlive() && !player.isCreative() && !player.isSpectator()) {
                double dist = this.getDistanceSq(player);
                if (dist <= detectionRange * detectionRange && dist < closestDist) {
                    double dx = player.posX - this.posX;
                    double dz = player.posZ - this.posZ;
                    double dot = dx * facing.getXOffset() + dz * facing.getZOffset();

                    if (dot > 0 && player.canEntityBeSeen(this)) {
                        closestDist = dist;
                        target = player;
                    }
                }
            }
        }

        if (target != null) {
            double spawnX = this.posX + facing.getXOffset() * 1.5D;
            double spawnY = this.posY + 0.6D;
            double spawnZ = this.posZ + facing.getZOffset() * 1.5D;

            EntityNestRock rock = new EntityNestRock(this.world, spawnX, spawnY, spawnZ);

            double dx = target.posX - spawnX;
            double dy = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - spawnY;
            double dz = target.posZ - spawnZ;

            rock.shoot(dx, dy, dz, 1.2F, 4.0F);
            this.world.spawnEntity(rock);

            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SNOWMAN_SHOOT, SoundCategory.HOSTILE, 1.0F, 0.4F / (this.rand.nextFloat() * 0.4F + 0.8F));

            this.shootTimer = 80;
        }
    }

    private void trySpawnBrood() {
        double detectionRange = this.broodsSpawned == 0 ? Settings.miscSettings.nestGoblinDetectionRange : 20.0D;
        boolean validPlayerNearby = false;
        for (EntityPlayer player : this.world.playerEntities) {
            if (player.isEntityAlive() && !player.isCreative() && !player.isSpectator() && this.getDistanceSq(player) <= detectionRange * detectionRange) {
                validPlayerNearby = true;
                break;
            }
        }
        if (!validPlayerNearby) {
            return;
        }

        int broodCount = 0;
        List<EntityGoblin> nearbyGoblins = this.world.getEntitiesWithinAABB(EntityGoblin.class, this.getEntityBoundingBox().grow(20.0D, 10.0D, 20.0D));
        for (EntityGoblin goblin : nearbyGoblins) {
            if (goblin.isEntityAlive() && goblin.getClass() == EntityGoblin.class) {
                broodCount++;
            }
        }

        if (broodCount > 7) {
            return;
        }

        EntityGoblin brood = new EntityGoblin(this.world);
        if (this.persistentBroods) {
            brood.enablePersistence();
        }
        brood.setPosition(this.posX, this.posY + 0.2D, this.posZ);
        this.world.spawnEntity(brood);
        this.broodsSpawned++;

        if (this.broodsSpawned < 3) {
            this.spawnTimer = 20; // 1 second delay between first 3
        } else {
            this.spawnTimer = 600; // 30 second delay for subsequent spawns
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setFacing(EnumFacing.byHorizontalIndex(compound.getInteger("Facing")));
        if (compound.hasKey("NestHealth")) {
            this.nestHealth = compound.getFloat("NestHealth");
        }
        if (compound.hasKey("BroodsSpawned")) {
            this.broodsSpawned = compound.getInteger("BroodsSpawned");
        }
        if (compound.hasKey("SpawnTimer")) {
            this.spawnTimer = compound.getInteger("SpawnTimer");
        }
        if (compound.hasKey("ShootTimer")) {
            this.shootTimer = compound.getInteger("ShootTimer");
        }
        this.persistentBroods = compound.getBoolean("PersistentBroods");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("Facing", this.getFacing().getHorizontalIndex());
        compound.setFloat("NestHealth", this.nestHealth);
        compound.setInteger("BroodsSpawned", this.broodsSpawned);
        compound.setInteger("SpawnTimer", this.spawnTimer);
        compound.setInteger("ShootTimer", this.shootTimer);
        compound.setBoolean("PersistentBroods", this.persistentBroods);
    }
}
