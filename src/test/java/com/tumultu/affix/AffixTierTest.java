package com.tumultu.affix;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffixTierTest {

    @Mock
    private ItemStack stack;

    @Test
    void rollStaysWithinMinAndMaxInclusive() {
        AffixTier tier = TestFixtures.tier(3.0, 5.0);
        RandomSource random = RandomSource.create(42L);

        for (int i = 0; i < 200; i++) {
            double value = tier.roll(random);
            assertTrue(value >= 3.0 && value <= 5.0, "rolled value " + value + " out of range");
        }
    }

    @Test
    void isApplicableToIsUniversalWhenNoTagsConfigured() {
        AffixTier tier = TestFixtures.tier(1.0, 2.0);

        assertTrue(tier.isApplicableTo(stack));
    }

    @Test
    void isApplicableToMatchesWhenStackHasAnyConfiguredTag() {
        AffixTier tier = TestFixtures.tier(1.0, 2.0, List.of(TestFixtures.id("does_not_matter")));
        lenient().when(stack.is((net.minecraft.tags.TagKey<net.minecraft.world.item.Item>) org.mockito.ArgumentMatchers.any())).thenReturn(true);

        assertTrue(tier.isApplicableTo(stack));
    }

    @Test
    void isApplicableToRejectsWhenStackHasNoneOfTheConfiguredTags() {
        AffixTier tier = TestFixtures.tier(1.0, 2.0, List.of(TestFixtures.id("does_not_matter")));
        when(stack.is((net.minecraft.tags.TagKey<net.minecraft.world.item.Item>) org.mockito.ArgumentMatchers.any())).thenReturn(false);

        assertFalse(tier.isApplicableTo(stack));
    }
}