package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LifeOnHitEffectTest {

    @Test
    void typeIdIsLifeOnHit() {
        assertEquals("life_on_hit", new LifeOnHitEffect(false).typeId());
    }

    @Test
    void describeFormatsAsFlatLife() {
        assertEquals("+2.5 Life per Hit", new LifeOnHitEffect(false).describe(2.5).getString());
    }
}
