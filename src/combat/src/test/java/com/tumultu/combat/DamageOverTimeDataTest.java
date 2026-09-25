package com.tumultu.combat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageOverTimeDataTest {

    private static final UUID SOURCE = UUID.randomUUID();

    private static int countFires(DamageOverTimeData data, int maxTicks) {
        int fires = 0;
        for (int i = 0; i < maxTicks && data.isActive(); i++) {
            if (data.shouldDealDamageThisTick()) fires++;
            data = data.tickedDown();
        }
        return fires;
    }

    @Test
    void inactiveIsNotActive() {
        assertFalse(DamageOverTimeData.INACTIVE.isActive());
    }

    @Test
    void freshDataIsActiveButDoesNotFireImmediately() {
        DamageOverTimeData data = DamageOverTimeData.create(60, 1.5f, SOURCE);

        assertTrue(data.isActive());
        assertFalse(data.shouldDealDamageThisTick());
    }

    @ParameterizedTest
    @CsvSource({
            "20, 0",   // shorter than one full 20-tick window: never fires
            "40, 1",
            "60, 2",
            "80, 3",
    })
    void firesOnceEveryTwentyTicksForCleanDurations(int durationTicks, int expectedFires) {
        DamageOverTimeData data = DamageOverTimeData.create(durationTicks, 1.5f, SOURCE);

        assertEquals(expectedFires, countFires(data, durationTicks));
    }

    @ParameterizedTest
    @CsvSource({
            "66, 3",  // a dot duration affix can produce a non-multiple-of-20 total
            "79, 3",  // the countdown must stay independent of ticksRemaining so it still fires
            "99, 4",
    })
    void stillFiresCorrectlyWhenDurationIsNotAMultipleOfTwenty(int durationTicks, int expectedFires) {
        DamageOverTimeData data = DamageOverTimeData.create(durationTicks, 1.5f, SOURCE);

        assertEquals(expectedFires, countFires(data, durationTicks));
    }

    @Test
    void becomesInactiveOnceTicksRunOut() {
        DamageOverTimeData data = DamageOverTimeData.create(1, 1.5f, SOURCE);

        data = data.tickedDown();

        assertFalse(data.isActive());
        assertFalse(data.shouldDealDamageThisTick());
    }

    @Test
    void damagePerSecondAndSourceSurviveTicking() {
        DamageOverTimeData data = DamageOverTimeData.create(40, 2.5f, SOURCE);

        data = data.tickedDown();

        assertEquals(2.5f, data.damagePerSecond());
        assertEquals(SOURCE, data.sourceId());
    }
}
