package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoisonDamageMultiplierEffectTest {

    @Test
    void typeIdIsPoisonDamageMultiplier() {
        assertEquals("poison_damage_multiplier", new PoisonDamageMultiplierEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+25% Poison Damage", new PoisonDamageMultiplierEffect(true).describe(0.25).getString());
    }
}
