package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementalPenetrationEffectTest {

    @Test
    void typeIdIsElementalPenetration() {
        assertEquals("elemental_penetration", new ElementalPenetrationEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("30% Elemental Penetration", new ElementalPenetrationEffect(true).describe(0.30).getString());
    }
}
