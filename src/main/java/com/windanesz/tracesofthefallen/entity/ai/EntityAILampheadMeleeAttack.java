package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class EntityAILampheadMeleeAttack extends EntityAIAttackMelee {
    private final EntityLamphead lamphead;
    private int raiseArmTicks;

    public EntityAILampheadMeleeAttack(EntityLamphead lamphead, double speedIn, boolean useLongMemory) {
        super(lamphead, speedIn, useLongMemory);
        this.lamphead = lamphead;
    }

    @Override
    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F + attackTarget.width) + 1.5D; // Increased reach
    }

    @Override
    public boolean shouldExecute() {
        if (this.lamphead.getMode() == 2) {
            return false; // Static mode won't fight for player
        }
        return super.shouldExecute();
    }
}
