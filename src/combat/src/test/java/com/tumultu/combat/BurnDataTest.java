package com.tumultu.combat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurnDataTest {

    private static final UUID SOURCE = UUID.randomUUID();

    private static int countFires(BurnData data, int maxTicks) {
        int fires = 0;
        for (int i = 0; i < maxTicks && data.isActive(); i++) {
            if (data.shouldDealDamageThisTick()) fires++;
            data = data.tickedDown();
        }
        return fires;
    }

    @Test
    void inactiveIsNotActive() {
        assertFalse(BurnData.INACTIVE.isActive());
    }

    @Test
    void freshDataIsActiveButDoesNotFireImmediately() {
        BurnData data = BurnData.create(60, 0.5f, SOURCE);

        assertTrue(data.isActive());
        assertFalse(data.shouldDealDamageThisTick());
    }

    @ParameterizedTest
    @CsvSource({
            "20, 0",
            "40, 1",
            "60, 2",
            "80, 3",
    })
    void firesOnceEveryTwentyTicksForCleanDurations(int durationTicks, int expectedFires) {
        BurnData data = BurnData.create(durationTicks, 0f, SOURCE);

        assertEquals(expectedFires, countFires(data, durationTicks));
    }

    @Test
    void becomesInactiveOnceTicksRunOut() {
        BurnData data = BurnData.create(1, 0f, SOURCE).tickedDown();

        assertFalse(data.isActive());
        assertFalse(data.shouldDealDamageThisTick());
    }

    @Test
    void dotMultiplierAndSourceSurviveTicking() {
        BurnData data = BurnData.create(40, 0.3f, SOURCE).tickedDown();

        assertEquals(0.3f, data.dotMultiplier());
        assertEquals(SOURCE, data.sourceId());
    }
}
