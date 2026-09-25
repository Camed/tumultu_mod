package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LifeOnKillEffectTest {

    @Test
    void typeIdIsLifeOnKill() {
        assertEquals("life_on_kill", new LifeOnKillEffect(false).typeId());
    }

    @Test
    void describeFormatsAsFlatLife() {
        assertEquals("+5.0 Life per Kill", new LifeOnKillEffect(false).describe(5.0).getString());
    }
}
