package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatElementalDamageEffectTest {

    @Test
    void typeIdIsFlatElementalDamage() {
        assertEquals("flat_elemental_damage", new FlatElementalDamageEffect(ElementKind.FIRE, false).typeId());
    }

    @Test
    void describeIncludesTheElementName() {
        assertEquals("+5.0 Fire Damage", new FlatElementalDamageEffect(ElementKind.FIRE, false).describe(5.0).getString());
        assertEquals("+5.0 Cold Damage", new FlatElementalDamageEffect(ElementKind.COLD, false).describe(5.0).getString());
        assertEquals("+5.0 Lightning Damage", new FlatElementalDamageEffect(ElementKind.LIGHTNING, false).describe(5.0).getString());
    }

    @ParameterizedTest
    @EnumSource(ElementKind.class)
    void everyElementRoundTripsThroughItsOwnEffectInstance(ElementKind element) {
        FlatElementalDamageEffect effect = new FlatElementalDamageEffect(element, false);
        assertEquals(element, effect.element());
    }
}
