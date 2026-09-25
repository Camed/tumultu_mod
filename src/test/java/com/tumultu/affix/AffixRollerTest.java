package com.tumultu.affix;

import com.tumultu.affix.effect.AreaMiningEffect;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffixRollerTest {

    @Mock
    private RegistryAccess registryAccess;
    @Mock
    private Registry<AffixDefinition> registry;
    @Mock
    private ItemStack stack;

    private void stubRegistry(Map<Identifier, AffixDefinition> definitions) {
        lenient().when(registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY)).thenReturn(registry);

        Set<Map.Entry<ResourceKey<AffixDefinition>, AffixDefinition>> entries = new HashSet<>();
        for (var e : definitions.entrySet()) {
            entries.add(new AbstractMap.SimpleEntry<>(
                    ResourceKey.create(TumultuRegistries.AFFIX_KEY, e.getKey()), e.getValue()));
        }
        lenient().when(registry.entrySet()).thenReturn(entries);
        Identifier anyId = any();
        lenient().when(registry.getValue(anyId))
                .thenAnswer(inv -> definitions.get(inv.getArgument(0)));
    }

    private static Map<Identifier, AffixDefinition> mapOf(Object... kv) {
        Map<Identifier, AffixDefinition> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((Identifier) kv[i], (AffixDefinition) kv[i + 1]);
        }
        return map;
    }

    @Test
    void rollForRarityNormalReturnsEmptyWithoutTouchingRegistry() {
        AffixData result = AffixRoller.rollForRarity(registryAccess, stack, ItemRarity.NORMAL, RandomSource.create(1L));

        assertEquals(AffixData.EMPTY, result);
        verifyNoInteractions(registryAccess);
    }

    @Test
    void rollForRarityMagicRespectsCapsAndTierRanges() {
        Identifier prefixId = TestFixtures.id("test_prefix");
        Identifier suffixId = TestFixtures.id("test_suffix");
        stubRegistry(mapOf(
                prefixId, TestFixtures.definition(AffixType.PREFIX, "prefix_group", 100, List.of(TestFixtures.tier(1.0, 2.0))),
                suffixId, TestFixtures.definition(AffixType.SUFFIX, "suffix_group", 100, List.of(TestFixtures.tier(10.0, 20.0)))
        ));

        AffixData result = AffixRoller.rollForRarity(registryAccess, stack, ItemRarity.MAGIC, RandomSource.create(7L));

        assertEquals(ItemRarity.MAGIC, result.rarity());
        assertFalse(result.endfused());
        assertTrue(result.affixes().size() <= ItemRarity.MAGIC.maxTotal());
        for (RolledAffix rolled : result.affixes()) {
            if (rolled.affixId().equals(prefixId)) {
                assertTrue(rolled.rolledValue() >= 1.0 && rolled.rolledValue() <= 2.0);
            } else {
                assertEquals(suffixId, rolled.affixId());
                assertTrue(rolled.rolledValue() >= 10.0 && rolled.rolledValue() <= 20.0);
            }
        }
    }

    @Test
    void rollForRarityEndfusedMarksEndfusedFlag() {
        stubRegistry(Map.of());

        AffixData result = AffixRoller.rollForRarity(registryAccess, stack, ItemRarity.ENDFUSED, RandomSource.create(3L));

        assertEquals(ItemRarity.ENDFUSED, result.rarity());
        assertTrue(result.endfused());
    }

    @Test
    void rerollPreservesExistingRarity() {
        stubRegistry(Map.of());
        AffixData existing = new AffixData(ItemRarity.RARE, List.of(), false);

        AffixData result = AffixRoller.reroll(registryAccess, stack, existing, RandomSource.create(9L));

        assertEquals(ItemRarity.RARE, result.rarity());
    }

    @Test
    void rerollPreservesCraftedAffixWithItsOriginalTierAndValue() {
        Identifier craftedId = TestFixtures.id("crafted_affix");
        stubRegistry(mapOf(
                craftedId, TestFixtures.definition(AffixType.PREFIX, "crafted_group", 100,
                        List.of(TestFixtures.tier(1.0, 2.0), TestFixtures.tier(5.0, 6.0)))
        ));
        RolledAffix originalCrafted = new RolledAffix(craftedId, 1, 5.5);
        AffixData existing = new AffixData(ItemRarity.RARE, List.of(originalCrafted), false, Optional.of(craftedId));

        AffixData result = AffixRoller.reroll(registryAccess, stack, existing, RandomSource.create(42L));

        assertTrue(result.affixes().contains(originalCrafted),
                "the crafted affix must survive a reroll with its exact original tier/value");
        assertEquals(Optional.of(craftedId), result.craftedAffixId());
    }

    @Test
    void rerollPreservesBothCraftedAndImbuedAffixesIndependently() {
        Identifier craftedId = TestFixtures.id("crafted_one");
        Identifier imbuedId = TestFixtures.id("imbued_one");
        stubRegistry(mapOf(
                craftedId, TestFixtures.definition(AffixType.PREFIX, "g1", 100, List.of(TestFixtures.tier(1.0, 1.0))),
                imbuedId, TestFixtures.definition(AffixType.SUFFIX, "g2", 100, List.of(TestFixtures.tier(9.0, 9.0)))
        ));
        RolledAffix craftedRolled = new RolledAffix(craftedId, 0, 1.0);
        RolledAffix imbuedRolled = new RolledAffix(imbuedId, 0, 9.0);
        AffixData existing = new AffixData(ItemRarity.RARE, List.of(craftedRolled, imbuedRolled), false,
                Optional.of(craftedId), Optional.of(imbuedId));

        AffixData result = AffixRoller.reroll(registryAccess, stack, existing, RandomSource.create(3L));

        assertTrue(result.affixes().contains(craftedRolled));
        assertTrue(result.affixes().contains(imbuedRolled));
        assertEquals(Optional.of(craftedId), result.craftedAffixId());
        assertEquals(Optional.of(imbuedId), result.imbuedAffixId());
    }

    @Test
    void addRandomAffixSkipsCandidateSharingGroupWithExistingAffix() {
        Identifier existingId = TestFixtures.id("existing_prefix");
        Identifier blockedId = TestFixtures.id("blocked_same_group");
        stubRegistry(mapOf(
                existingId, TestFixtures.definition(AffixType.PREFIX, "shared_group", 100, List.of(TestFixtures.tier(1.0, 1.0))),
                blockedId, TestFixtures.definition(AffixType.SUFFIX, "shared_group", 100, List.of(TestFixtures.tier(2.0, 2.0)))
        ));
        AffixData existing = new AffixData(ItemRarity.RARE, List.of(new RolledAffix(existingId, 0, 1.0)), false);

        AffixData result = AffixRoller.addRandomAffix(registryAccess, stack, existing, RandomSource.create(5L));

        assertEquals(existing.affixes(), result.affixes(),
                "the only other candidate shares a group with an existing affix and must be filtered out");
    }

    @Test
    void addRandomAffixReturnsExistingWhenPoolIsEmpty() {
        stubRegistry(Map.of());
        AffixData existing = new AffixData(ItemRarity.RARE, List.of(), false);

        AffixData result = AffixRoller.addRandomAffix(registryAccess, stack, existing, RandomSource.create(2L));

        assertEquals(existing, result);
    }

    @Test
    void addRandomAffixRespectsPrefixCap() {
        Identifier candidateId = TestFixtures.id("only_prefix_candidate");
        Identifier p1 = TestFixtures.id("p1");
        Identifier p2 = TestFixtures.id("p2");
        Identifier p3 = TestFixtures.id("p3");
        stubRegistry(mapOf(
                candidateId, TestFixtures.definition(AffixType.PREFIX, "candidate_group", 100, List.of(TestFixtures.tier(1.0, 1.0))),
                p1, TestFixtures.definition(AffixType.PREFIX, "group_p1", 100, List.of(TestFixtures.tier(1.0, 1.0))),
                p2, TestFixtures.definition(AffixType.PREFIX, "group_p2", 100, List.of(TestFixtures.tier(1.0, 1.0))),
                p3, TestFixtures.definition(AffixType.PREFIX, "group_p3", 100, List.of(TestFixtures.tier(1.0, 1.0)))
        ));
        // RARE allows at most 3 prefixes; these three already fill the cap.
        AffixData existing = new AffixData(ItemRarity.RARE, List.of(
                new RolledAffix(p1, 0, 1.0),
                new RolledAffix(p2, 0, 1.0),
                new RolledAffix(p3, 0, 1.0)
        ), false);

        AffixData result = AffixRoller.addRandomAffix(registryAccess, stack, existing, RandomSource.create(11L));

        assertEquals(existing.affixes(), result.affixes(),
                "prefix cap already reached: the only candidate (a prefix) must not be added");
    }

    @Test
    void isApplicableIsUniversalWhenDefinitionHasNoTags() {
        AffixDefinition def = TestFixtures.definition(AffixType.PREFIX, "g", 100, List.of(TestFixtures.tier(1.0, 1.0)));

        assertTrue(AffixRoller.isApplicable(def, stack));
        verifyNoInteractions(stack);
    }

    @Test
    void isApplicableFalseWhenStackMatchesNoConfiguredTag() {
        AffixDefinition def = new AffixDefinition(AffixType.PREFIX, "d", new AreaMiningEffect(false),
                List.of(TestFixtures.tier(1.0, 1.0)), 100, "g", List.of(TestFixtures.id("some_tag")));
        TagKey<Item> anyTag = any();
        when(stack.is(anyTag)).thenReturn(false);

        assertFalse(AffixRoller.isApplicable(def, stack));
    }

    @Test
    void hasEligibleTiersTrueWhenAtLeastOneTierApplies() {
        AffixDefinition def = TestFixtures.definition(AffixType.PREFIX, "g", 100, List.of(TestFixtures.tier(1.0, 1.0)));

        assertTrue(AffixRoller.hasEligibleTiers(def, stack));
    }

    @Test
    void rollCountIsZeroWhenMaxIsZero() {
        assertEquals(0, AffixRoller.rollCount(RandomSource.create(1L), 0));
    }

    @Test
    void rollCountIsBetweenOneAndMaxInclusive() {
        RandomSource random = RandomSource.create(4L);
        for (int i = 0; i < 100; i++) {
            int count = AffixRoller.rollCount(random, 3);
            assertTrue(count >= 1 && count <= 3);
        }
    }

    /**
     * Reproduces the mod's actual tier layout (2 universal tiers + a tier2-exclusive + a
     * tier1-exclusive, matching e.g. melee/brutal.json) against a stack that matches ONLY the
     * tier1 tag - exactly a netherite sword's real tag membership (tier1_weapons/tier2_weapons
     * are flat, mutually-exclusive tags with no overlap or inheritance). Verifies the actual
     * {@link AffixRoller#addRandomAffix} tier pick (not a reimplementation of it) never lands on
     * the tier2-only tier, and spreads roughly evenly across the three tiers it IS eligible for -
     * catching a regression where eligibility or the random pick itself became biased toward the
     * top tier.
     */
    @Test
    void tierSelectionNeverPicksTier2ExclusiveTierForATier1OnlyStackAndSpreadsAcrossTheRest() {
        Identifier affixId = TestFixtures.id("melee_affix");
        Identifier tier1Tag = TestFixtures.id("tier1_weapons");
        Identifier tier2Tag = TestFixtures.id("tier2_weapons");
        List<AffixTier> tiers = List.of(
                TestFixtures.tier(0.05, 0.1),
                TestFixtures.tier(0.1, 0.15),
                TestFixtures.tier(0.15, 0.2, List.of(tier2Tag)),
                TestFixtures.tier(0.2, 0.3, List.of(tier1Tag))
        );
        stubRegistry(mapOf(affixId, TestFixtures.definition(AffixType.PREFIX, "g", 100, tiers)));

        TagKey<Item> anyTag = any();
        lenient().when(stack.is(anyTag)).thenAnswer(inv -> {
            TagKey<Item> tag = inv.getArgument(0);
            return tag.location().getPath().equals("tier1_weapons");
        });

        int[] counts = new int[4];
        RandomSource random = RandomSource.create(123L);
        AffixData empty = new AffixData(ItemRarity.RARE, List.of(), false);
        int trials = 3000;
        for (int i = 0; i < trials; i++) {
            AffixData result = AffixRoller.addRandomAffix(registryAccess, stack, empty, random);
            assertEquals(1, result.affixes().size());
            counts[result.affixes().get(0).tierIndex()]++;
        }

        assertEquals(0, counts[2], "the tier2-only tier must never be picked for a tier1-only stack");
        // Generous tolerance around the expected ~1/3 each - this only needs to catch a real skew
        // (e.g. always landing on index 3), not enforce a statistically perfect split.
        for (int index : List.of(0, 1, 3)) {
            assertTrue(counts[index] > trials * 0.2 && counts[index] < trials * 0.47,
                    "tier index " + index + " got " + counts[index] + "/" + trials + ", expected roughly 1/3");
        }
    }

    @Test
    void selectWeightedPicksAccordingToWeightBucket() {
        AffixDefinition low = TestFixtures.definition(AffixType.PREFIX, "a", 10, List.of(TestFixtures.tier(1.0, 1.0)));
        AffixDefinition high = TestFixtures.definition(AffixType.PREFIX, "b", 90, List.of(TestFixtures.tier(1.0, 1.0)));
        List<AffixRoller.WeightedAffix> pool = List.of(
                new AffixRoller.WeightedAffix(TestFixtures.id("low"), low),
                new AffixRoller.WeightedAffix(TestFixtures.id("high"), high)
        );
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(100)).thenReturn(50); // lands past the first (weight-10) bucket, inside the second

        AffixRoller.WeightedAffix selected = AffixRoller.selectWeighted(pool, random);

        assertEquals(TestFixtures.id("high"), selected.id());
    }
}