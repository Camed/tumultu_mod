package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhysicalDamageReductionEffectTest {

    @Test
    void typeIdIsPhysicalDamageReduction() {
        assertEquals("physical_damage_reduction", new PhysicalDamageReductionEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+8% additional Physical Damage Reduction", new PhysicalDamageReductionEffect(true).describe(0.08).getString());
    }
}
