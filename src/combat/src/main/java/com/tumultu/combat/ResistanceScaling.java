package com.tumultu.combat;

public final class ResistanceScaling {
    // Resistance is already hard-capped at 75% by PlayerCombatStats.MAX_ELEMENTAL_RESISTANCE, so
    // this is just a defensive floor against a future change letting it approach 100%, where
    // scaledDecayRate would otherwise divide by ~0.
    // todo: revisit if resistance maths changes
    private static final double MAX_SAFE_RESISTANCE = 0.99;

    private ResistanceScaling() {}

    public static int scaledDuration(int baseDurationTicks, double resistance) {
        double clamped = Math.min(Math.max(resistance, 0), MAX_SAFE_RESISTANCE);
        return (int) Math.round(baseDurationTicks * (1 - clamped));
    }

    public static double scaledDecayRate(double baseDecayPerTick, double resistance) {
        double clamped = Math.min(Math.max(resistance, 0), MAX_SAFE_RESISTANCE);
        return baseDecayPerTick / (1 - clamped);
    }
}
