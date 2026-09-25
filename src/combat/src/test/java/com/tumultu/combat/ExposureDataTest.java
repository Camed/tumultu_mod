package com.tumultu.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExposureDataTest {

    @Test
    void inactiveIsNotActive() {
        assertFalse(ExposureData.INACTIVE.isActive());
    }

    @Test
    void gainIncreasesValue() {
        ExposureData data = ExposureData.INACTIVE.withGain(20f);

        assertTrue(data.isActive());
        assertEquals(20f, data.value());
    }

    @Test
    void gainAccumulatesAcrossHits() {
        ExposureData data = ExposureData.INACTIVE.withGain(20f).withGain(30f);

        assertEquals(50f, data.value());
    }

    @Test
    void gainIsCappedAtMax() {
        ExposureData data = ExposureData.INACTIVE.withGain(150f);

        assertEquals(ExposureData.MAX, data.value());
    }

    @Test
    void decayReducesValueButFloorsAtZero() {
        ExposureData data = new ExposureData(10f).decayedBy(4);

        assertEquals(6f, data.value(), 0.0001f);

        ExposureData decayedPastZero = data.decayedBy(100);
        assertEquals(0f, decayedPastZero.value());
        assertFalse(decayedPastZero.isActive());
    }
}
