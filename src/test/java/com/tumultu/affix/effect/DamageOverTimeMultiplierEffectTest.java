package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageOverTimeMultiplierEffectTest {

    @Test
    void typeIdIsDamageOverTimeMultiplier() {
        assertEquals("damage_over_time_multiplier", new DamageOverTimeMultiplierEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+40% Damage over Time", new DamageOverTimeMultiplierEffect(true).describe(0.40).getString());
    }
}
