package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaMiningEffectTest {

    @Test
    void typeIdIsAreaMining() {
        assertEquals("area_mining", new AreaMiningEffect(false).typeId());
    }

    @Test
    void isAValuelessMarkerRecord() {
        assertEquals(new AreaMiningEffect(false), new AreaMiningEffect(false));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 2",
            "2, 3, 2",
            "3, 3, 3",
            "4, 4, 3",
            "5, 4, 4"
    })
    void sizeForDecodesEachTierToItsFixedDimensions(double rolledValue, int width, int height) {
        assertArrayEquals(new int[]{width, height}, AreaMiningEffect.sizeFor(rolledValue));
    }

    @Test
    void sizeForClampsOutOfRangeValuesToTheNearestValidTier() {
        assertArrayEquals(new int[]{2, 2}, AreaMiningEffect.sizeFor(0.0));
        assertArrayEquals(new int[]{4, 4}, AreaMiningEffect.sizeFor(9.0));
    }
}