package com.tumultu.combat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResistanceScalingTest {

    @Test
    void zeroResistanceLeavesDurationUnchanged() {
        assertEquals(100, ResistanceScaling.scaledDuration(100, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "100, 0.75, 25",   // "75% resistance -> 75% shorter"
            "100, 0.5, 50",
            "100, 0.25, 75",
            "200, 0.75, 50",
    })
    void durationShrinksProportionallyWithResistance(int base, double resistance, int expected) {
        assertEquals(expected, ResistanceScaling.scaledDuration(base, resistance));
    }

    @Test
    void zeroResistanceLeavesDecayRateUnchanged() {
        assertEquals(1.0, ResistanceScaling.scaledDecayRate(1.0, 0), 0.0001);
    }

    @Test
    void seventyFivePercentResistanceQuadruplesDecayRate() {
        // A meter that decays 4x faster empties in 1/4 the time - the same "75% shorter" outcome
        // as scaledDuration, expressed as a rate instead of a fixed duration.
        assertEquals(4.0, ResistanceScaling.scaledDecayRate(1.0, 0.75), 0.0001);
    }

    @Test
    void resistanceAboveSafeCeilingDoesNotDivideByZero() {
        double rate = ResistanceScaling.scaledDecayRate(1.0, 1.0);

        assertEquals(100.0, rate, 0.001);
    }
}
