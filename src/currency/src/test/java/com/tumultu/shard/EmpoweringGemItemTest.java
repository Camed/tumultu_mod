package com.tumultu.shard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmpoweringGemItemTest {

    @Test
    void mostRollsGiveOnlyOneLevel() {
        assertEquals(1, EnchantLevelRoll.pickLevelIncrease(0.0));
        assertEquals(1, EnchantLevelRoll.pickLevelIncrease(0.89));
    }

    @Test
    void middleBandGivesTwoLevels() {
        assertEquals(2, EnchantLevelRoll.pickLevelIncrease(0.90));
        assertEquals(2, EnchantLevelRoll.pickLevelIncrease(0.98));
    }

    @Test
    void topOnePercentGivesThreeLevels() {
        assertEquals(3, EnchantLevelRoll.pickLevelIncrease(0.99));
        assertEquals(3, EnchantLevelRoll.pickLevelIncrease(0.999));
    }
}
