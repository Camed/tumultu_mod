package com.tumultu.affix;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AffixDataTest {

    @Test
    void emptyIsNormalWithNoAffixesAndNotEndfused() {
        assertEquals(ItemRarity.NORMAL, AffixData.EMPTY.rarity());
        assertTrue(AffixData.EMPTY.affixes().isEmpty());
        assertFalse(AffixData.EMPTY.endfused());
    }

    @Test
    void withAffixesPreservesRarityAndEndfusedFlag() {
        AffixData base = new AffixData(ItemRarity.RARE, List.of(), true);
        RolledAffix affix = new RolledAffix(TestFixtures.id("brutal"), 0, 5.0);

        AffixData result = base.withAffixes(List.of(affix));

        assertEquals(List.of(affix), result.affixes());
        assertEquals(ItemRarity.RARE, result.rarity());
        assertTrue(result.endfused());
    }

    @Test
    void withRarityPreservesAffixesAndEndfusedFlag() {
        RolledAffix affix = new RolledAffix(TestFixtures.id("brutal"), 0, 5.0);
        AffixData base = new AffixData(ItemRarity.MAGIC, List.of(affix), false);

        AffixData result = base.withRarity(ItemRarity.RARE);

        assertEquals(ItemRarity.RARE, result.rarity());
        assertEquals(List.of(affix), result.affixes());
        assertFalse(result.endfused());
    }

    @Test
    void asEndfusedSetsRarityAndFlagButKeepsAffixes() {
        RolledAffix affix = new RolledAffix(TestFixtures.id("brutal"), 0, 5.0);
        AffixData base = new AffixData(ItemRarity.RARE, List.of(affix), false);

        AffixData result = base.asEndfused();

        assertEquals(ItemRarity.ENDFUSED, result.rarity());
        assertTrue(result.endfused());
        assertEquals(List.of(affix), result.affixes());
    }

    @Test
    void isModifiableReflectsEndfusedFlag() {
        assertTrue(new AffixData(ItemRarity.RARE, List.of(), false).isModifiable());
        assertFalse(new AffixData(ItemRarity.ENDFUSED, List.of(), true).isModifiable());
    }

    @Test
    void countByTypeCountsOnlyMatchingAffixes() {
        RolledAffix prefixAffix = new RolledAffix(TestFixtures.id("brutal"), 0, 5.0);
        RolledAffix suffixAffix = new RolledAffix(TestFixtures.id("of_force"), 0, 0.2);
        AffixData data = new AffixData(ItemRarity.RARE, List.of(prefixAffix, suffixAffix), false);

        int prefixCount = data.countByType(AffixType.PREFIX,
                rolled -> rolled.affixId().equals(TestFixtures.id("brutal")) ? AffixType.PREFIX : AffixType.SUFFIX);

        assertEquals(1, prefixCount);
    }
}