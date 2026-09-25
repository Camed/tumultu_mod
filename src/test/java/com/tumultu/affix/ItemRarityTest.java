package com.tumultu.affix;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRarityTest {

    @ParameterizedTest
    @EnumSource(ItemRarity.class)
    void maxTotalIsSumOfPrefixesAndSuffixes(ItemRarity rarity) {
        assertEquals(rarity.maxPrefixes() + rarity.maxSuffixes(), rarity.maxTotal());
    }

    @Test
    void normalAndUniqueRollNoAffixes() {
        assertEquals(0, ItemRarity.NORMAL.maxTotal());
        assertEquals(0, ItemRarity.UNIQUE.maxTotal());
    }

    @Test
    void magicAllowsOnePrefixAndOneSuffix() {
        assertEquals(1, ItemRarity.MAGIC.maxPrefixes());
        assertEquals(1, ItemRarity.MAGIC.maxSuffixes());
    }

    @Test
    void rareAllowsThreePrefixesAndThreeSuffixes() {
        assertEquals(3, ItemRarity.RARE.maxPrefixes());
        assertEquals(3, ItemRarity.RARE.maxSuffixes());
    }

    @Test
    void endfusedAllowsMoreAffixesThanRare() {
        assertEquals(4, ItemRarity.ENDFUSED.maxPrefixes());
        assertEquals(4, ItemRarity.ENDFUSED.maxSuffixes());
        assertEquals(ItemRarity.RARE.maxTotal() + 2, ItemRarity.ENDFUSED.maxTotal());
    }
}