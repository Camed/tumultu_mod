package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageOverTimeDurationEffectTest {

    @Test
    void typeIdIsDamageOverTimeDuration() {
        assertEquals("damage_over_time_duration", new DamageOverTimeDurationEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+20% Damage over Time Duration", new DamageOverTimeDurationEffect(true).describe(0.20).getString());
    }
}
