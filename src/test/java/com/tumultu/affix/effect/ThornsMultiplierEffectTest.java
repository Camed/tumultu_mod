package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThornsMultiplierEffectTest {

    @Test
    void typeIdIsThornsMultiplier() {
        assertEquals("thorns_multiplier", new ThornsMultiplierEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+30% increased Thorns Damage", new ThornsMultiplierEffect(true).describe(0.30).getString());
    }
}
