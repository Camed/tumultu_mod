package com.tumultu.affix;

import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
    Targeted, not RNG based affix crafting system through crafting bench
 */
public class BenchCrafting {

    public record CraftableAffix(Identifier id, AffixDefinition def) {}

    /**
     * Every affix in category that could currently be crafted onto target item:  needs to be valid
     * for the item, has a tier eligible for it, not already blocked by a used group, and of a
     * prefix/suffix type that still has room. Only one crafted affix is allowed per item.
     */
    public static List<CraftableAffix> validCategoryAffixes(RegistryAccess registryAccess, ItemStack target, AffixData existing, RecipeCategory category) {
        if (existing.craftedAffixId().isPresent()) {
            return List.of();
        }

        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        Set<String> usedGroups = new HashSet<>();
        int prefixCount = 0;
        int suffixCount = 0;
        for (RolledAffix rolled : existing.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) continue;
            if (!def.group().isEmpty()) {
                usedGroups.add(def.group());
            }
            if (def.type() == AffixType.PREFIX) prefixCount++;
            else suffixCount++;
        }

        boolean prefixRoom = prefixCount < existing.rarity().maxPrefixes();
        boolean suffixRoom = suffixCount < existing.rarity().maxSuffixes();

        List<CraftableAffix> result = new ArrayList<>();
        for (var entry : registry.entrySet()) {
            AffixDefinition def = entry.getValue();
            if (!def.craftCategory().equals(category.id())) continue;
            if (!AffixRoller.isApplicable(def, target)) continue;
            if (!AffixRoller.hasEligibleTiers(def, target)) continue;
            if (!def.group().isEmpty() && usedGroups.contains(def.group())) continue;
            if (def.type() == AffixType.PREFIX && !prefixRoom) continue;
            if (def.type() == AffixType.SUFFIX && !suffixRoom) continue;

            result.add(new CraftableAffix(entry.getKey().identifier(), def));
        }
        return result;
    }

    public static Optional<AffixData> craftAffix(
            RegistryAccess registryAccess, ItemStack target, AffixData existing,
            RecipeCategory category, int chosenIndex, PaymentTier tier, RandomSource random
    ) {
        if (!existing.isModifiable() || existing.craftedAffixId().isPresent()) {
            return Optional.empty();
        }

        List<CraftableAffix> valid = validCategoryAffixes(registryAccess, target, existing, category);
        if (chosenIndex < 0 || chosenIndex >= valid.size()) {
            return Optional.empty();
        }
        CraftableAffix chosen = valid.get(chosenIndex);

        List<AffixTier> eligibleTiers = new ArrayList<>();
        List<Integer> eligibleIndices = new ArrayList<>();
        for (int t = 0; t < chosen.def().tiers().size(); t++) {
            AffixTier affixTier = chosen.def().tiers().get(t);
            if (affixTier.isApplicableTo(target)) {
                eligibleTiers.add(affixTier);
                eligibleIndices.add(t);
            }
        }
        if (eligibleTiers.isEmpty()) {
            return Optional.empty();
        }

        int pick = pickWeightedTierIndex(eligibleTiers.size(), tier.biasExponent(), random.nextDouble());
        int tierIndex = eligibleIndices.get(pick);
        double value = eligibleTiers.get(pick).roll(random);

        List<RolledAffix> newAffixes = new ArrayList<>(existing.affixes());
        newAffixes.add(new RolledAffix(chosen.id(), tierIndex, value));

        AffixData result = existing.withAffixes(List.copyOf(newAffixes)).withCraftedAffixId(Optional.of(chosen.id()));
        return Optional.of(result);
    }

    // just the crafted affix removal
    public static Optional<AffixData> removeCraftedAffix(AffixData existing) {
        if (!existing.isModifiable() || existing.craftedAffixId().isEmpty()) {
            return Optional.empty();
        }

        Identifier craftedId = existing.craftedAffixId().get();

        // check if crafted is imbued
        if (existing.imbuedAffixId().isPresent() && existing.imbuedAffixId().get().equals(craftedId)) {
            return Optional.empty();
        }
        List<RolledAffix> remaining = existing.affixes().stream()
                .filter(rolled -> !rolled.affixId().equals(craftedId))
                .toList();

        return Optional.of(existing.withAffixes(remaining).withCraftedAffixId(Optional.empty()));
    }

    /**
     * Weight for eligible-tier-position (0 = worst, n-1 = best) is
     * (i+1)^biasExponent- higher exponent skews harder toward the best tier without ever
     * fully excluding the others.
     */
    static int pickWeightedTierIndex(int eligibleTierCount, double biasExponent, double roll) {
        double[] weights = new double[eligibleTierCount];
        double total = 0;
        for (int i = 0; i < eligibleTierCount; i++) {
            weights[i] = Math.pow(i + 1, biasExponent);
            total += weights[i];
        }

        double target = roll * total;
        double cumulative = 0;
        for (int i = 0; i < eligibleTierCount; i++) {
            cumulative += weights[i];
            if (target < cumulative) {
                return i;
            }
        }
        return eligibleTierCount - 1;
    }
}
