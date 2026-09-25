package com.tumultu.loot;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BonusDropHandlerTest {

    private static final int TRIALS = 20_000;

    @Test
    void neverGoesNegative() {
        RandomSource random = RandomSource.create(1L);
        for (int i = 0; i < 1000; i++) {
            assertTrue(BonusDropHandler.rollBonusCount(random, 0) >= 0);
        }
    }

    @Test
    void zeroLuckMatchesHalvingGeometricDistribution() {
        RandomSource random = RandomSource.create(42L);
        int atLeastOne = 0;
        int atLeastTwo = 0;
        int atLeastThree = 0;
        for (int i = 0; i < TRIALS; i++) {
            int count = BonusDropHandler.rollBonusCount(random, 0);
            if (count >= 1) atLeastOne++;
            if (count >= 2) atLeastTwo++;
            if (count >= 3) atLeastThree++;
        }

        // P(>=1)=0.5, P(>=2)=0.5*0.25=0.125, P(>=3)=0.5*0.25*0.125=0.015625 - each successive
        // roll's own chance is BASE_CHANCE*DECAY_FACTOR^rollIndex, and reaching count N requires
        // every roll up to N to succeed.
        assertRatioNear(atLeastOne, TRIALS, 0.50, 0.05);
        assertRatioNear(atLeastTwo, TRIALS, 0.125, 0.03);
        assertRatioNear(atLeastThree, TRIALS, 0.015625, 0.02);
    }

    @Test
    void higherLuckIncreasesAverageBonusCount() {
        RandomSource random = RandomSource.create(7L);
        double noLuckAvg = averageBonus(random, 0);
        double someLuckAvg = averageBonus(random, 5);
        double lotsOfLuckAvg = averageBonus(random, 20);

        assertTrue(someLuckAvg > noLuckAvg,
                "5 luck (" + someLuckAvg + ") should beat 0 luck (" + noLuckAvg + ")");
        assertTrue(lotsOfLuckAvg > someLuckAvg,
                "20 luck (" + lotsOfLuckAvg + ") should beat 5 luck (" + someLuckAvg + ")");
    }

    @Test
    void extremeLuckStillStopsEventually() {
        RandomSource random = RandomSource.create(3L);
        for (int i = 0; i < 500; i++) {
            int count = BonusDropHandler.rollBonusCount(random, 1000);
            assertTrue(count <= 20, "must respect the safety cap even at absurd luck values");
        }
    }

    private static double averageBonus(RandomSource random, double luck) {
        long total = 0;
        for (int i = 0; i < TRIALS; i++) {
            total += BonusDropHandler.rollBonusCount(random, luck);
        }
        return (double) total / TRIALS;
    }

    private static void assertRatioNear(int hits, int trials, double expected, double tolerance) {
        double actual = (double) hits / trials;
        assertTrue(Math.abs(actual - expected) < tolerance,
                "expected ~" + expected + " but got " + actual + " (" + hits + "/" + trials + ")");
    }
}
