package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CritDamageReductionEffectTest {

    @Test
    void typeIdIsCritDamageReduction() {
        assertEquals("crit_damage_reduction", new CritDamageReductionEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("-50% Critical Strike Damage Taken", new CritDamageReductionEffect(true).describe(0.50).getString());
    }
}
