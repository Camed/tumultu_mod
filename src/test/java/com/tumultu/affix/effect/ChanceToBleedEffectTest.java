package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChanceToBleedEffectTest {

    @Test
    void typeIdIsChanceToBleed() {
        assertEquals("chance_to_bleed", new ChanceToBleedEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentageChance() {
        assertEquals("20% Chance to Bleed", new ChanceToBleedEffect(true).describe(0.20).getString());
    }
}
