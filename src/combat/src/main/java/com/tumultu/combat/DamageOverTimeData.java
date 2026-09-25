package com.tumultu.combat;

import java.util.UUID;

/**
 * Attachment payload for bleed/poison. Not persisted across save/reload (at least for now).
 *
 * <p>The countdown-to-next-damage is tracked independently of the total remaining duration
 * (rather than firing on {@code ticksRemaining % 20 == 0}) specifically because a
 * damage-over-time-duration affix can scale total duration to something that isn't a clean
 * multiple of 20 - a modulo check against the total would then never align and could fire zero
 * times. This way any duration works: damage always fires every 20 ticks from application,
 * however many total ticks remain, for however many full 20-tick windows fit.
 *
 * @param ticksRemaining     total ticks left before the effect ends
 * @param ticksUntilNextTick counts down independently to the next damage tick, resetting to 20
 *                           each time it fires - decoupled from ticksRemaining's exact value
 * @param damagePerSecond    baked in at application time, including any damage-over-time-multiplier
 *                           affix the attacker had at that moment - never re-read from their gear later
 * @param sourceId           the attacker's UUID, for kill credit on each tick; may be absent if unknown
 */
public record DamageOverTimeData(int ticksRemaining, int ticksUntilNextTick, float damagePerSecond, UUID sourceId) {
    private static final int DAMAGE_INTERVAL_TICKS = 20;

    public static final DamageOverTimeData INACTIVE = new DamageOverTimeData(0, 0, 0f, new UUID(0, 0));

    public static DamageOverTimeData create(int durationTicks, float damagePerSecond, UUID sourceId) {
        return new DamageOverTimeData(durationTicks, DAMAGE_INTERVAL_TICKS, damagePerSecond, sourceId);
    }

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public boolean shouldDealDamageThisTick() {
        return isActive() && ticksUntilNextTick <= 0;
    }

    // call after checking {@link #shouldDealDamageThisTick()} on the current state, not before.
    public DamageOverTimeData tickedDown() {
        int nextRemaining = ticksRemaining - 1;
        int nextUntilTick = shouldDealDamageThisTick() ? DAMAGE_INTERVAL_TICKS : ticksUntilNextTick - 1;
        return new DamageOverTimeData(nextRemaining, nextUntilTick, damagePerSecond, sourceId);
    }
}
