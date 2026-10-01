package com.tumultu.mobs.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

// a goal for a boss - meant to add some ranged pressure.
public class BlightlordRangedAttackGoal extends Goal {
    private static final double MAX_RANGE_SQR = 20.0 * 20.0;

    private final BlightlordEntity blightlord;

    public BlightlordRangedAttackGoal(BlightlordEntity blightlord) {
        this.blightlord = blightlord;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.blightlord.getTarget();
        return target != null
                && target.isAlive()
                && this.blightlord.isRangedAttackReady()
                && this.blightlord.distanceToSqr(target) <= MAX_RANGE_SQR
                && this.blightlord.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = this.blightlord.getTarget();
        if (target != null) {
            this.blightlord.performRangedAttack(target);
        }
    }
}
