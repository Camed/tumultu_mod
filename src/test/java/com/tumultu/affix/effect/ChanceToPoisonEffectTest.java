package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChanceToPoisonEffectTest {

    @Test
    void typeIdIsChanceToPoison() {
        assertEquals("chance_to_poison", new ChanceToPoisonEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentageChance() {
        assertEquals("10% Chance to Poison", new ChanceToPoisonEffect(true).describe(0.10).getString());
    }
}
