package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChanceToWitherEffectTest {

    @Test
    void typeIdIsChanceToWither() {
        assertEquals("chance_to_wither", new ChanceToWitherEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentageChance() {
        assertEquals("15% Chance to Wither", new ChanceToWitherEffect(true).describe(0.15).getString());
    }
}
