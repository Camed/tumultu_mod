package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CritDamagePercentEffectTest {

    @Test
    void typeIdIsCritDamagePercent() {
        assertEquals("crit_damage_percent", new CritDamagePercentEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+40% Critical Strike Damage", new CritDamagePercentEffect(true).describe(0.40).getString());
    }
}
