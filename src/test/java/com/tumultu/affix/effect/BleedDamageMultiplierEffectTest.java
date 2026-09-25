package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BleedDamageMultiplierEffectTest {

    @Test
    void typeIdIsBleedDamageMultiplier() {
        assertEquals("bleed_damage_multiplier", new BleedDamageMultiplierEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+25% Bleed Damage", new BleedDamageMultiplierEffect(true).describe(0.25).getString());
    }
}
