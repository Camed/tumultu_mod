package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CannotBePoisonedEffectTest {

    @Test
    void typeIdIsCannotBePoisoned() {
        assertEquals("cannot_be_poisoned", new CannotBePoisonedEffect(false).typeId());
    }

    @Test
    void describeIgnoresRolledValueAndReturnsFixedText() {
        assertEquals("Cannot be Poisoned", new CannotBePoisonedEffect(false).describe(1.0).getString());
        assertEquals("Cannot be Poisoned", new CannotBePoisonedEffect(false).describe(0.0).getString());
    }
}
