package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LifeRegenerationEffectTest {

    @Test
    void typeIdIsLifeRegeneration() {
        assertEquals("life_regeneration", new LifeRegenerationEffect(false).typeId());
    }

    @Test
    void describeFormatsAsFlatHealthPerSecond() {
        assertEquals("+0.6 Life Regenerated per Second", new LifeRegenerationEffect(false).describe(0.6).getString());
    }
}
