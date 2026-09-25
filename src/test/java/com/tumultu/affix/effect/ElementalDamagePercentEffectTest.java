package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementalDamagePercentEffectTest {

    @Test
    void typeIdIsElementalDamagePercent() {
        assertEquals("elemental_damage_percent", new ElementalDamagePercentEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+30% Elemental Damage", new ElementalDamagePercentEffect(true).describe(0.30).getString());
    }
}
