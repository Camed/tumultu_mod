package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChanceToFireAdditionalArrowEffectTest {

    @Test
    void typeIdIsChanceExtraArrow() {
        assertEquals("chance_extra_arrow", new ChanceToFireAdditionalArrowEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentageChance() {
        assertEquals("20% Chance to Fire an Additional Arrow", new ChanceToFireAdditionalArrowEffect(true).describe(0.20).getString());
    }
}
