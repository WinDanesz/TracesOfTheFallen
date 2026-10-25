package com.windanesz.tracesofthefallen.entity.living;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.ai.EntityAISubterfugeGodslap;
import com.windanesz.tracesofthefallen.tile.TileEntityBalance;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.packet.PacketSpawnGodSlapParticle;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntitySubterfuge extends EntityCreature {
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/subterfuge");

    public static final DataParameter<Boolean> AGGRESSION_STATE = EntityDataManager.createKey(EntitySubterfuge.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> EXPRESSION_STATE = EntityDataManager.createKey(EntitySubterfuge.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> HIDDEN_STATE = EntityDataManager.createKey(EntitySubterfuge.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> ATTACKING_STATE = EntityDataManager.createKey(EntitySubterfuge.class, DataSerializers.BOOLEAN);

    public int aggressionHits = 0;
    public int aggressionTimer = 0;
    public int speechCooldown = 0;
    public int expressionTimer = 0;

    // Only set on a shopkeeper: the Subterfuge who minds a generated shop. Others stay plain, unprotected NPCs.
    @Nullable
    private StructureBoundingBox shopBounds;
    private long shopSeed;
    private int[] shopBalances = new int[0];
    // A hostile mob she has noticed in her shop and is about to obliterate.
    @Nullable
    private EntityLivingBase smiteTarget;
    private int smiteTimer;
    private int intruderSpeechCooldown;
    // How many mobs she has wiped out lately. Three and four earn the player a warning, five a slap of their own.
    private int intruderStrikes;
    private int strikeDecayTimer;
    private boolean wrathful;

    private static final String[] HIT_DIALOGUE_LINES = {
            "dialogue.totf.subterfuge.hit1",
            "dialogue.totf.subterfuge.hit2",
            "dialogue.totf.subterfuge.hit3",
            "dialogue.totf.subterfuge.hit4"
    };

    public EntitySubterfuge(World worldIn) {
        super(worldIn);
        this.setSize(0.8F, 2.0F);
        this.experienceValue = 0; 
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(AGGRESSION_STATE, false);
        this.dataManager.register(EXPRESSION_STATE, 0); // 0=None, 1=Welcome, 2=Approval, 3=Disapproval, 4=Angry
        this.dataManager.register(HIDDEN_STATE, false);
        this.dataManager.register(ATTACKING_STATE, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.mobSettings.subterfugeMaxHealth); 
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.mobSettings.subterfugeAttackDamage); 
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return 3; // Much slower rotation (default is typically 10+)
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 3; 
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAISubterfugeGodslap(this));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(3, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false) {
            @Override
            public boolean shouldExecute() {
                return EntitySubterfuge.this.isAggressive() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, false) {
            @Override
            public boolean shouldExecute() {
                return EntitySubterfuge.this.isAggressive() && super.shouldExecute();
            }
        });
    }

    // ---------------------------------------------------------------------------------------------------
    // Shop stuff
    // ---------------------------------------------------------------------------------------------------

    // Makes this Subterfuge the keeper of a generated shop covering bounds, selling from balances
    public void setShop(StructureBoundingBox bounds, List<BlockPos> balances, long seed) {
        this.shopBounds = bounds;
        this.shopSeed = seed;
        this.shopBalances = new int[balances.size() * 3];
        for (int index = 0; index < balances.size(); index++) {
            BlockPos pos = balances.get(index);
            this.shopBalances[index * 3] = pos.getX();
            this.shopBalances[index * 3 + 1] = pos.getY();
            this.shopBalances[index * 3 + 2] = pos.getZ();
        }
        this.enablePersistence();
    }

    public boolean isShopkeeper() {
        return this.shopBounds != null;
    }

    public boolean isInShop(BlockPos pos) {
        return this.shopBounds != null && this.shopBounds.isVecInside(pos);
    }

    // She is in her shop right now: it trades, and it is protected
    public boolean isShopOpen() {
        return this.shopBounds != null && !this.isHiddenState() && this.isEntityAlive();
    }

    /**
     * Whether a shop with this seed has its keeper in at the given time. It is decided once per presence
     * period from the seed alone, so it needs no state and gives the same answer after a reload.
     */
    public static boolean isPresentDuring(long shopSeed, long worldTime) {
        double chance = Settings.worldgenSettings.subterfugeShopPresenceChance;
        if (chance >= 1.0D) {
            return true;
        }
        if (chance <= 0.0D) {
            return false;
        }
        long period = Math.max(1, Settings.worldgenSettings.subterfugeShopPresenceDays) * 24000L;
        Random random = new Random(shopSeed + Math.floorDiv(worldTime, period) * 341873128712L);
        random.nextInt();
        return random.nextDouble() < chance;
    }

    private static final int SMITE_WINDUP_TICKS = 15;
    private static final int STRIKE_DECAY_TICKS = 18000; // 15mins
    private static final int FIRST_WARNING_STRIKES = 3;
    private static final int SECOND_WARNING_STRIKES = 4;
    private static final int WRATH_STRIKES = 5;
    private static final String[] INTRUDER_LINES = {
            "dialogue.totf.subterfuge.intruder1",
            "dialogue.totf.subterfuge.intruder2",
            "dialogue.totf.subterfuge.intruder3",
            "dialogue.totf.subterfuge.intruder4",
            "dialogue.totf.subterfuge.intruder5",
            "dialogue.totf.subterfuge.intruder6",
            "dialogue.totf.subterfuge.intruder7"
    };

    /** Watches for hostile mobs that have come down into the shop, and wipes them out after a short warning. */
    private void tickIntruders() {
        if (this.intruderSpeechCooldown > 0) {
            this.intruderSpeechCooldown--;
        }

        // Her patience comes back one strike every 30 seconds.
        if (this.intruderStrikes > 0 && --this.strikeDecayTimer <= 0) {
            this.intruderStrikes--;
            this.strikeDecayTimer = STRIKE_DECAY_TICKS;
        }
        if (this.wrathful) {
            EntityLivingBase victim = this.getAttackTarget();
            if (!this.isAggressive() || this.intruderStrikes < WRATH_STRIKES || victim == null || !this.isInShopRoom(victim)) {
                this.calmDown();
            }
        }

        if (this.smiteTimer > 0) {
            this.smiteTimer--;
            EntityLivingBase target = this.smiteTarget;
            if (this.smiteTimer > 0) {
                if (target != null) {
                    this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                }
                return;
            }
            this.dataManager.set(ATTACKING_STATE, false);
            this.smiteTarget = null;
            if (target != null && !target.isDead) {
                this.smite(target);
            }
            return;
        }

        if (this.shopBounds == null || this.ticksExisted % 10 != 3) {
            return;
        }
        AxisAlignedBB area = this.shopArea();
        for (EntityLivingBase mob : this.world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (mob instanceof IMob && !(mob instanceof EntitySubterfuge) && mob.isEntityAlive() && this.isInShopRoom(mob)) {
                this.noticeIntruder(mob, area);
                return;
            }
        }
    }

    private AxisAlignedBB shopArea() {
        StructureBoundingBox bounds = this.shopBounds;
        if (bounds == null) {
            return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        }
        return new AxisAlignedBB(
                bounds.minX, bounds.minY, bounds.minZ,
                bounds.maxX + 1, bounds.maxY + 1, bounds.maxZ + 1);
    }

    // Down in the room itself: the open stairwell and the ground above it are not her shop.
    private boolean isInShopRoom(Entity entity) {
        StructureBoundingBox bounds = this.shopBounds;
        return bounds != null && bounds.isVecInside(new BlockPos(entity))
                && entity.posY <= this.posY + 3.5D && !this.world.canSeeSky(new BlockPos(entity));
    }

    private List<EntityPlayer> playersInShopRoom() {
        List<EntityPlayer> inside = new ArrayList<>();
        for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.shopArea())) {
            if (this.isInShopRoom(player)) {
                inside.add(player);
            }
        }
        return inside;
    }

    private void registerStrike() {
        if (this.intruderStrikes == 0) {
            this.strikeDecayTimer = STRIKE_DECAY_TICKS;
        }
        this.intruderStrikes++;

        List<EntityPlayer> inside = this.playersInShopRoom();
        if (inside.isEmpty()) {
            return;
        }
        if (this.intruderStrikes == FIRST_WARNING_STRIKES) {
            for (EntityPlayer player : inside) {
                this.say(player, "dialogue.totf.subterfuge.warning1");
            }
        } else if (this.intruderStrikes == SECOND_WARNING_STRIKES) {
            for (EntityPlayer player : inside) {
                this.say(player, "dialogue.totf.subterfuge.warning2");
            }
        } else if (this.intruderStrikes >= WRATH_STRIKES) {
            EntityPlayer victim = null;
            for (EntityPlayer player : inside) {
                this.say(player, "dialogue.totf.subterfuge.warning3");
                if (victim == null && !player.capabilities.isCreativeMode && !player.isSpectator()) {
                    victim = player;
                }
            }
            if (victim != null) {
                this.wrathful = true;
                this.dataManager.set(AGGRESSION_STATE, true);
                this.setExpressionState(4, 100);
                this.setAttackTarget(victim);
            }
        }
    }

    /**
     * Sends a player to a random safe spot on the surface far from here, rather than killing them. Returns
     * false if nowhere safe was found, or this is not a world with a surface to land on.
     */
    public boolean teleportAway(EntityPlayer player) {
        if (this.world.isRemote || !this.world.provider.isSurfaceWorld()) {
            return false;
        }
        int min = Settings.mobSettings.subterfugeHardcoreTeleportMin;
        int max = Math.max(min, Settings.mobSettings.subterfugeHardcoreTeleportMax);
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = this.rand.nextDouble() * Math.PI * 2.0D;
            double distance = min + this.rand.nextDouble() * (max - min);
            BlockPos spot = this.findSafeSurfaceSpot(
                    (int) Math.floor(player.posX + Math.cos(angle) * distance),
                    (int) Math.floor(player.posZ + Math.sin(angle) * distance));
            if (spot == null) {
                continue;
            }
            this.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.dismountRidingEntity();
            player.setPositionAndUpdate(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D);
            player.fallDistance = 0.0F;
            this.world.playSound(null, spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            this.say(player, "dialogue.totf.subterfuge.rtp");
            return true;
        }
        return false;
    }

    /** Feet position on solid, dry, safe ground at a column; generates the chunk if it has to. Null if unsuitable. */
    @Nullable
    private BlockPos findSafeSurfaceSpot(int x, int z) {
        if (!this.world.getWorldBorder().contains(new BlockPos(x, 64, z))) {
            return null;
        }
        this.world.getChunk(x >> 4, z >> 4);
        BlockPos feet = this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        if (feet.getY() < 2) {
            return null;
        }
        IBlockState ground = this.world.getBlockState(feet.down());
        Material material = ground.getMaterial();
        if (material.isLiquid() || !material.blocksMovement() || material == Material.CACTUS || material == Material.FIRE) {
            return null;
        }
        for (BlockPos body : new BlockPos[] {feet, feet.up()}) {
            IBlockState state = this.world.getBlockState(body);
            if (state.getMaterial().blocksMovement() || state.getMaterial().isLiquid()) {
                return null;
            }
        }
        return feet;
    }

    private void calmDown() {
        this.wrathful = false;
        this.dataManager.set(AGGRESSION_STATE, false);
        this.aggressionHits = 0;
        this.setAttackTarget(null);
        this.setExpressionState(0, 0);
    }

    private void noticeIntruder(EntityLivingBase mob, AxisAlignedBB shopArea) {
        this.smiteTarget = mob;
        this.smiteTimer = SMITE_WINDUP_TICKS;
        this.dataManager.set(ATTACKING_STATE, true);
        this.setExpressionState(4, 40);
        this.getNavigator().clearPath();

        if (this.intruderSpeechCooldown <= 0) {
            this.intruderSpeechCooldown = 40;
            String line = INTRUDER_LINES[this.rand.nextInt(INTRUDER_LINES.length)];
            for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, shopArea.grow(16.0D))) {
                this.say(player, line);
            }
        }
    }

    private void smite(EntityLivingBase target) {
        // The slap comes out of her hand, the same spot as her usual attack; she has been looking at the mob.
        Vec3d lookDir = this.getLookVec();
        double handX = this.posX + lookDir.x;
        double handY = this.posY + 1.8D;
        double handZ = this.posZ + lookDir.z;
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(this.dimension, handX, handY, handZ, 64.0D);
        PacketHandler.net.sendToAllAround(new PacketSpawnGodSlapParticle(handX, handY, handZ), point);
        this.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.HOSTILE, 1.0F, 0.8F);
        // Nothing left: no corpse, no drops.
        target.setDead();
        this.registerStrike();
    }

    /** Every balance of her shop has been bought out. */
    public boolean isSoldOut() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int index = 0; index + 2 < this.shopBalances.length; index += 3) {
            pos.setPos(this.shopBalances[index], this.shopBalances[index + 1], this.shopBalances[index + 2]);
            if (!this.world.isBlockLoaded(pos)) {
                continue;
            }
            TileEntity tileEntity = this.world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityBalance && !((TileEntityBalance) tileEntity).getLeftStack().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** The shop is gone once more than half of its balances have been broken. */
    private boolean isShopIntact() {
        int total = 0;
        int standing = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int index = 0; index + 2 < this.shopBalances.length; index += 3) {
            pos.setPos(this.shopBalances[index], this.shopBalances[index + 1], this.shopBalances[index + 2]);
            if (!this.world.isBlockLoaded(pos)) {
                continue;
            }
            total++;
            if (this.world.getBlockState(pos).getBlock() == ModBlocks.balance) {
                standing++;
            }
        }
        return standing * 2 >= total;
    }

    private void updateShop() {
        if (!this.isShopIntact()) {
            // Torn down while she was away: she is not coming back.
            this.setDead();
            return;
        }

        boolean present = isPresentDuring(this.shopSeed, this.world.getTotalWorldTime());
        if (present && this.isHiddenState()) {
            this.setHiddenState(false);
            this.setExpressionState(1, 60);
        } else if (!present && !this.isHiddenState() && this.world.getClosestPlayerToEntity(this, 16.0D) == null) {
            // Never vanishes in front of anyone.
            this.setHiddenState(true);
            this.dataManager.set(AGGRESSION_STATE, false);
            this.aggressionHits = 0;
            this.setAttackTarget(null);
        }
    }

    /** A line of her dialogue, in the same voice as her idle chatter. */
    public void say(EntityPlayer player, String translationKey) {
        TextComponentTranslation line = new TextComponentTranslation(translationKey);
        line.getStyle().setColor(TextFormatting.WHITE);
        TextComponentString prefix = new TextComponentString("[Subterfuge] ");
        prefix.getStyle().setColor(TextFormatting.DARK_PURPLE);
        player.sendMessage(prefix.appendSibling(line));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.shopBounds != null) {
            compound.setIntArray("ShopBounds", new int[] {
                    this.shopBounds.minX, this.shopBounds.minY, this.shopBounds.minZ,
                    this.shopBounds.maxX, this.shopBounds.maxY, this.shopBounds.maxZ});
            compound.setLong("ShopSeed", this.shopSeed);
            compound.setIntArray("ShopBalances", this.shopBalances);
            compound.setBoolean("ShopHidden", this.isHiddenState());
            compound.setInteger("IntruderStrikes", this.intruderStrikes);
            compound.setInteger("StrikeDecay", this.strikeDecayTimer);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        int[] bounds = compound.getIntArray("ShopBounds");
        if (bounds.length == 6) {
            this.shopBounds = new StructureBoundingBox(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
            this.shopSeed = compound.getLong("ShopSeed");
            this.shopBalances = compound.getIntArray("ShopBalances");
            this.setHiddenState(compound.getBoolean("ShopHidden"));
            this.intruderStrikes = compound.getInteger("IntruderStrikes");
            this.strikeDecayTimer = compound.getInteger("StrikeDecay");
        }
    }

    public boolean isAggressive() {
        return this.dataManager.get(AGGRESSION_STATE);
    }

    public boolean isHiddenState() {
        return this.dataManager.get(HIDDEN_STATE);
    }

    public void setHiddenState(boolean hidden) {
        this.dataManager.set(HIDDEN_STATE, hidden);
    }

    public int getExpressionState() {
        return this.dataManager.get(EXPRESSION_STATE);
    }

    public void setExpressionState(int state, int ticks) {
        this.dataManager.set(EXPRESSION_STATE, state);
        this.expressionTimer = ticks;
    }
    
    public boolean isAttackingState() {
        return this.dataManager.get(ATTACKING_STATE);
    }

    public void incrementAggression(EntityLivingBase attacker) {
        if (!this.world.isRemote && !this.isAggressive()) {
            this.aggressionHits++;
            this.aggressionTimer = 200; // 10 seconds timeout

            // Comment on being hit
            String key = HIT_DIALOGUE_LINES[this.rand.nextInt(HIT_DIALOGUE_LINES.length)];
            TextComponentTranslation line = new TextComponentTranslation(key);
            line.getStyle().setColor(TextFormatting.WHITE);
            TextComponentString prefix = new TextComponentString("[Subterfuge] ");
            prefix.getStyle().setColor(TextFormatting.DARK_PURPLE);
            if (attacker instanceof EntityPlayer) {
                attacker.sendMessage(prefix.appendSibling(line));
            }

            if (this.aggressionHits >= 3) {
                this.dataManager.set(AGGRESSION_STATE, true);
                this.setExpressionState(4, 100); // Angry
                this.setAttackTarget(attacker); // Immediately target the attacker
            } else {
                this.setExpressionState(3, 40); // Disapproval
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isHiddenState()) return false;
        if (source == DamageSource.DROWN) return false;
        
        if (!this.world.isRemote) {
            if (source.getTrueSource() instanceof EntityLivingBase) {
                this.incrementAggression((EntityLivingBase) source.getTrueSource());
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (this.isHiddenState()) return false;

        if (!this.world.isRemote && !this.isAggressive() && hand == EnumHand.MAIN_HAND) {
            if (this.speechCooldown <= 0) {
                this.speak(player);
                this.speechCooldown = 200; 
                this.setExpressionState(1 + this.rand.nextInt(2), 40); // Welcome or approval
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    private void speak(EntityPlayer player) {
        TextComponentTranslation line = new TextComponentTranslation("dialogue.totf.subterfuge.line" + (this.rand.nextInt(6) + 1));
        line.getStyle().setColor(TextFormatting.WHITE);
        TextComponentString prefix = new TextComponentString("[Subterfuge] ");
        prefix.getStyle().setColor(TextFormatting.DARK_PURPLE);
        player.sendMessage(prefix.appendSibling(line));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote && this.shopBounds != null && this.ticksExisted % 40 == 5) {
            this.updateShop();
            if (this.isDead) {
                return;
            }
        }
        if (!this.world.isRemote && this.shopBounds != null && !this.isHiddenState()) {
            this.tickIntruders();
        }

        if (this.isHiddenState()) {
            this.setAttackTarget(null);
            return;
        }

        if (!this.world.isRemote) {
            // Regen 10 HP per second (every 20 ticks)
            if (this.ticksExisted % 20 == 0) {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(10.0F);
                }
            }

            if (!this.isAggressive() && this.aggressionHits > 0) {
                if (this.aggressionTimer > 0) {
                    this.aggressionTimer--;
                } else {
                    this.aggressionHits = 0; 
                }
            }

            if (this.isAggressive()) {
                EntityLivingBase target = this.getAttackTarget();
                if (target == null || target.isDead || this.getDistance(target) > 40.0D) {
                    this.dataManager.set(AGGRESSION_STATE, false);
                    this.aggressionHits = 0;
                    this.setAttackTarget(null);
                    this.setExpressionState(0, 0); 
                }
            }

            if (this.speechCooldown > 0) {
                this.speechCooldown--;
            }



            if (this.expressionTimer > 0) {
                this.expressionTimer--;
                if (this.expressionTimer <= 0) {
                    this.dataManager.set(EXPRESSION_STATE, 0);
                }
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isHiddenState();
    }

    @Override
    public boolean canBePushed() {
        return false;
    }
    
    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!this.isHiddenState()) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }
}
