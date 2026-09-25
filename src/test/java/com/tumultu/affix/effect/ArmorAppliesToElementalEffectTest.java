package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorAppliesToElementalEffectTest {

    @Test
    void typeIdIsArmorAppliesToElemental() {
        assertEquals("armor_applies_to_elemental", new ArmorAppliesToElementalEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("20% of Armour applies to Elemental Damage", new ArmorAppliesToElementalEffect(true).describe(0.20).getString());
    }
}
