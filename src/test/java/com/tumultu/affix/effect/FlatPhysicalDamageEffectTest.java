package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatPhysicalDamageEffectTest {

    @Test
    void typeIdIsFlatPhysicalDamage() {
        assertEquals("flat_physical_damage", new FlatPhysicalDamageEffect(false).typeId());
    }

    @Test
    void describeFormatsAsFlatDamage() {
        assertEquals("+3.0 Physical Damage", new FlatPhysicalDamageEffect(false).describe(3.0).getString());
    }
}
