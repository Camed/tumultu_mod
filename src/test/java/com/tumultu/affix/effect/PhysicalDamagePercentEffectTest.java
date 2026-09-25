package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhysicalDamagePercentEffectTest {

    @Test
    void typeIdIsPhysicalDamagePercent() {
        assertEquals("physical_damage_percent", new PhysicalDamagePercentEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+15% Physical Damage", new PhysicalDamagePercentEffect(true).describe(0.15).getString());
    }
}
