package com.tumultu.shard;

final class EnchantLevelRoll {
    private EnchantLevelRoll() {
    }
    static int pickLevelIncrease(double roll) {
        if (roll < 0.90) return 1;
        if (roll < 0.99) return 2;
        return 3;
    }
}
