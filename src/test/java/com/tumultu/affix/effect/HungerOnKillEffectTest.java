package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HungerOnKillEffectTest {

    @Test
    void typeIdIsHungerOnKill() {
        assertEquals("hunger_on_kill", new HungerOnKillEffect(false).typeId());
    }

    @Test
    void describeFormatsAsFlatHunger() {
        assertEquals("+1.5 Hunger per Kill", new HungerOnKillEffect(false).describe(1.5).getString());
    }
}
