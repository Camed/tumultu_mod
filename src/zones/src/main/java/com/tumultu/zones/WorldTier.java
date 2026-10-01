package com.tumultu.zones;

public final class WorldTier {
    public static final int MIN = 0;
    public static final int MAX = 16;
    private static final int BLOCKS_PER_TIER = 1000;

    private WorldTier() {
    }

    public static int fromPosition(int x, int z) {
        int distance = Math.max(Math.abs(x), Math.abs(z));
        return Math.min(distance / BLOCKS_PER_TIER, MAX);
    }
}
