package com.tumultu.combat;

/**
 * Attachment payload for the fire/lightning "exposure" meters (0-100): builds up when that
 * element's damage lands, and continuously decays back to 0 every tick (see
 * {@link CombatTickHandler}) at a rate scaled by the victim's resistance to that element - see
 * {@link ResistanceScaling}. The chance to ignite/shock on a given hit is simply
 * {@code exposure / MAX} at the moment of that hit; for fire, the current value also drives
 * Burning's damage-per-tick live rather than a number baked in when it first ignited - see
 * {@link BurnData}. Not persisted across save/reload, same as bleed/poison (see
 * {@link TumultuAttachments}).
 */
public record ExposureData(float value) {
    public static final float MAX = 100f;
    public static final ExposureData INACTIVE = new ExposureData(0f);

    public boolean isActive() {
        return value > 0f;
    }

    public ExposureData withGain(float amount) {
        return new ExposureData(Math.min(MAX, value + amount));
    }

    public ExposureData decayedBy(double amount) {
        return new ExposureData((float) Math.max(0, value - amount));
    }
}
