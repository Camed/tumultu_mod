package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CritChanceEffectTest {

    @Test
    void typeIdIsCritChance() {
        assertEquals("crit_chance", new CritChanceEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+25% Critical Strike Chance", new CritChanceEffect(true).describe(0.25).getString());
    }
}
