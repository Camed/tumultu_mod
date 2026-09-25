package com.tumultu.affix;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchCraftingTest {

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("tumultu", path);
    }

    private static RolledAffix rolled(Identifier affixId) {
        return new RolledAffix(affixId, 0, 0.1);
    }

    @Test
    void singleEligibleTierAlwaysPicksItRegardlessOfRollOrBias() {
        assertEquals(0, BenchCrafting.pickWeightedTierIndex(1, 1.0, 0.0));
        assertEquals(0, BenchCrafting.pickWeightedTierIndex(1, 4.0, 0.999));
    }

    @Test
    void rollOfZeroAlwaysPicksTheWorstTier() {
        assertEquals(0, BenchCrafting.pickWeightedTierIndex(4, 1.0, 0.0));
        assertEquals(0, BenchCrafting.pickWeightedTierIndex(4, 4.0, 0.0));
    }

    @Test
    void rollJustBelowOnePicksTheBestTier() {
        assertEquals(3, BenchCrafting.pickWeightedTierIndex(4, 1.0, 0.9999));
        assertEquals(3, BenchCrafting.pickWeightedTierIndex(4, 4.0, 0.9999));
    }

    @ParameterizedTest
    @CsvSource({
            "0.05, 0",  // target 0.5 < 1
            "0.15, 1",  // target 1.5 within (1,3]
            "0.35, 2",  // target 3.5 within (3,6]
            "0.55, 2",  // target 5.5 within (3,6]
            "0.95, 3",  // target 9.5 within (6,10]
    })
    void linearBiasMatchesHandComputedCumulativeWeights(double roll, int expectedIndex) {
        assertEquals(expectedIndex, BenchCrafting.pickWeightedTierIndex(4, 1.0, roll));
    }

    @Test
    void higherBiasExponentShiftsDistributionTowardTheBestTierOnTheSameRoll() {
        int lowBias = BenchCrafting.pickWeightedTierIndex(5, 1.0, 0.5);
        int highBias = BenchCrafting.pickWeightedTierIndex(5, 6.0, 0.5);

        org.junit.jupiter.api.Assertions.assertTrue(highBias >= lowBias,
                "stronger bias should skew at least as far toward the top tier at the same roll");
    }

    @Test
    void zeroExponentIsUniform() {
        assertEquals(0, BenchCrafting.pickWeightedTierIndex(4, 0.0, 0.0));
        assertEquals(1, BenchCrafting.pickWeightedTierIndex(4, 0.0, 0.26));
        assertEquals(2, BenchCrafting.pickWeightedTierIndex(4, 0.0, 0.51));
        assertEquals(3, BenchCrafting.pickWeightedTierIndex(4, 0.0, 0.76));
    }

    @Test
    void removeCraftedAffixDropsOnlyTheCraftedOneAndClearsTheFlag() {
        Identifier randomId = id("random_affix");
        Identifier craftedId = id("crafted_affix");
        AffixData data = new AffixData(ItemRarity.RARE, List.of(rolled(randomId), rolled(craftedId)), false, Optional.of(craftedId));

        AffixData result = BenchCrafting.removeCraftedAffix(data).orElseThrow();

        assertEquals(List.of(rolled(randomId)), result.affixes());
        assertTrue(result.craftedAffixId().isEmpty());
    }

    @Test
    void removeCraftedAffixIsEmptyWhenThereIsNoCraftedAffix() {
        AffixData data = new AffixData(ItemRarity.RARE, List.of(rolled(id("random_affix"))), false);

        assertTrue(BenchCrafting.removeCraftedAffix(data).isEmpty());
    }

    @Test
    void removeCraftedAffixIsEmptyWhenTheCraftedAffixIsAlsoImbued() {
        Identifier craftedId = id("crafted_and_imbued");
        AffixData data = new AffixData(ItemRarity.RARE, List.of(rolled(craftedId)), false,
                Optional.of(craftedId), Optional.of(craftedId));

        assertTrue(BenchCrafting.removeCraftedAffix(data).isEmpty());
    }

    @Test
    void removeCraftedAffixIsEmptyWhenTheItemIsEndfused() {
        Identifier craftedId = id("crafted_affix");
        AffixData data = new AffixData(ItemRarity.ENDFUSED, List.of(rolled(craftedId)), true, Optional.of(craftedId));

        assertTrue(BenchCrafting.removeCraftedAffix(data).isEmpty());
    }
}
