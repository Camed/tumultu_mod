package com.tumultu.combat;

import java.util.UUID;

// burn data blob used to calculate damage from burning (tumultu, not vanilla one) dot damage
public record BurnData(int ticksRemaining, int ticksUntilNextTick, float dotMultiplier, UUID sourceId) {
    private static final int DAMAGE_INTERVAL_TICKS = 20;

    public static final BurnData INACTIVE = new BurnData(0, 0, 0f, new UUID(0, 0));

    public static BurnData create(int durationTicks, float dotMultiplier, UUID sourceId) {
        return new BurnData(durationTicks, DAMAGE_INTERVAL_TICKS, dotMultiplier, sourceId);
    }

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public boolean shouldDealDamageThisTick() {
        return isActive() && ticksUntilNextTick <= 0;
    }

    // (!!!) call after checking {@link #shouldDealDamageThisTick()} on the current state, not before
    public BurnData tickedDown() {
        int nextRemaining = ticksRemaining - 1;
        int nextUntilTick = shouldDealDamageThisTick() ? DAMAGE_INTERVAL_TICKS : ticksUntilNextTick - 1;
        return new BurnData(nextRemaining, nextUntilTick, dotMultiplier, sourceId);
    }
}
