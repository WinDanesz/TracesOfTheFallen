package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.ai.EntityAISubterfugeGodslap;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

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

    public void incrementAggression(EntityPlayer attacker) {
        if (!this.world.isRemote && !this.isAggressive()) {
            this.aggressionHits++;
            this.aggressionTimer = 200; // 10 seconds timeout

            // Comment on being hit
            String key = HIT_DIALOGUE_LINES[this.rand.nextInt(HIT_DIALOGUE_LINES.length)];
            TextComponentTranslation line = new TextComponentTranslation(key);
            line.getStyle().setColor(net.minecraft.util.text.TextFormatting.WHITE);
            TextComponentString prefix = new TextComponentString("[Subterfuge] ");
            prefix.getStyle().setColor(net.minecraft.util.text.TextFormatting.DARK_PURPLE);
            attacker.sendMessage(prefix.appendSibling(line));

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
        
        if (!this.world.isRemote) {
            if (source.getTrueSource() instanceof EntityPlayer) {
                this.incrementAggression((EntityPlayer) source.getTrueSource());
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
                EntityPlayer target = (EntityPlayer) this.getAttackTarget();
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

            if (!this.isAggressive() && this.speechCooldown <= 0 && this.ticksExisted % 40 == 0) {
                EntityPlayer closest = this.world.getClosestPlayerToEntity(this, 5.0D);
                if (closest != null && !closest.isSneaking()) {
                    this.speak(closest);
                    this.speechCooldown = 300; 
                    this.setExpressionState(1, 40); // Welcome
                }
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
    public void applyEntityCollision(net.minecraft.entity.Entity entityIn) {
        if (!this.isHiddenState()) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    protected ResourceLocation getLootTable() {
        return null;
    }
}
