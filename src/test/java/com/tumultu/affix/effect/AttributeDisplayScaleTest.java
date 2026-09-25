package com.tumultu.affix.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeDisplayScaleTest {

    @Test
    void addValueIsNotScaled() {
        assertEquals(3.0, AttributeDisplayScale.displayAmount(3.0, Operation.ADD_VALUE), 0.0001);
    }

    @Test
    void addMultipliedBaseIsScaledByOneHundred() {
        assertEquals(10.0, AttributeDisplayScale.displayAmount(0.1, Operation.ADD_MULTIPLIED_BASE), 0.0001);
    }

    @Test
    void addMultipliedTotalIsScaledByOneHundred() {
        assertEquals(9.0, AttributeDisplayScale.displayAmount(0.09, Operation.ADD_MULTIPLIED_TOTAL), 0.0001);
    }

    @Test
    void negativeRolledValuesAreScaledOnTheirAbsoluteValue() {
        assertEquals(15.0, AttributeDisplayScale.displayAmount(-0.15, Operation.ADD_MULTIPLIED_BASE), 0.0001);
        assertEquals(2.0, AttributeDisplayScale.displayAmount(-2.0, Operation.ADD_VALUE), 0.0001);
    }
}
