package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementalResistanceEffectTest {

    @Test
    void typeIdIsElementalResistance() {
        assertEquals("elemental_resistance", new ElementalResistanceEffect(ElementKind.FIRE, true).typeId());
    }

    @Test
    void describeIncludesTheElementName() {
        assertEquals("+15% Fire Resistance", new ElementalResistanceEffect(ElementKind.FIRE, true).describe(0.15).getString());
        assertEquals("+15% Cold Resistance", new ElementalResistanceEffect(ElementKind.COLD, true).describe(0.15).getString());
        assertEquals("+15% Lightning Resistance", new ElementalResistanceEffect(ElementKind.LIGHTNING, true).describe(0.15).getString());
    }
}
