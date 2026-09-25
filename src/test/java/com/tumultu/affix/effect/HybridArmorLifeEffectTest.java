package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HybridArmorLifeEffectTest {

    @Test
    void typeIdIsHybridArmorLife() {
        assertEquals("hybrid_armor_life", new HybridArmorLifeEffect(true).typeId());
    }

    @Test
    void lifeForConvertsArmorPercentAtFixedRatio() {
        assertEquals(2.0, new HybridArmorLifeEffect(true).lifeFor(0.10), 0.0001);
    }

    @Test
    void describeFormatsBothArmorAndDerivedLife() {
        assertEquals("+10% Increased Armour and +2.0 Life", new HybridArmorLifeEffect(true).describe(0.10).getString());
    }
}
