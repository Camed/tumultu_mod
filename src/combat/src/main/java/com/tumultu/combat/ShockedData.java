package com.tumultu.combat;
public record ShockedData(int ticksRemaining) {
    public static final ShockedData INACTIVE = new ShockedData(0);

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public ShockedData tickedDown() {
        return new ShockedData(Math.max(0, ticksRemaining - 1));
    }
}
