package com.tumultu.affix;

import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class AffixRoller {

    public static AffixData rollForRarity(RegistryAccess registryAccess, ItemStack stack, ItemRarity rarity, RandomSource random) {
        if (rarity == ItemRarity.NORMAL) {
            return AffixData.EMPTY;
        }

        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        int targetPrefixes = rollCount(random, rarity.maxPrefixes());
        int targetSuffixes = rollCount(random, rarity.maxSuffixes());

        List<RolledAffix> rolled = new ArrayList<>();
        Set<String> usedGroups = new HashSet<>();

        rollAffixes(registry, stack, AffixType.PREFIX, targetPrefixes, usedGroups, rolled, random);
        rollAffixes(registry, stack, AffixType.SUFFIX, targetSuffixes, usedGroups, rolled, random);

        return new AffixData(rarity, List.copyOf(rolled), rarity == ItemRarity.ENDFUSED);
    }

    public static AffixData reroll(RegistryAccess registryAccess, ItemStack stack, AffixData existing, RandomSource random) {
        return rerollPreservingProtected(registryAccess, stack, existing, existing.rarity(),
                existing.rarity(), existing.rarity() == ItemRarity.ENDFUSED, random);
    }

    /**
        Custom endfusing rerolling - modifying item over standard amount of affixes etc.
     */
    public static AffixData rerollForEndfusing(RegistryAccess registryAccess, ItemStack stack, AffixData existing, ItemRarity rollRarity, RandomSource random) {
        return rerollPreservingProtected(registryAccess, stack, existing, rollRarity, ItemRarity.ENDFUSED, true, random);
    }

    /**
        Some items can have protected affixes - for example imbued ones.
        Here we make sure they are not rolled on edit.
     */
    private static AffixData rerollPreservingProtected(
            RegistryAccess registryAccess, ItemStack stack, AffixData existing,
            ItemRarity rollRarity, ItemRarity finalRarity, boolean finalEndfused, RandomSource random
    ) {
        List<RolledAffix> protectedAffixes = collectProtectedAffixes(existing);
        AffixData fresh = rollForRarity(registryAccess, stack, rollRarity, random);

        if (protectedAffixes.isEmpty()) {
            return new AffixData(finalRarity, fresh.affixes(), finalEndfused, existing.craftedAffixId(), existing.imbuedAffixId());
        }

        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        Set<Identifier> protectedIds = protectedAffixes.stream().map(RolledAffix::affixId).collect(Collectors.toSet());
        Set<String> protectedGroups = new HashSet<>();
        for (RolledAffix p : protectedAffixes) {
            AffixDefinition def = registry.getValue(p.affixId());
            if (def != null && !def.group().isEmpty()) protectedGroups.add(def.group());
        }

        List<RolledAffix> combined = new ArrayList<>();
        for (RolledAffix a : fresh.affixes()) {
            if (protectedIds.contains(a.affixId())) continue;
            AffixDefinition def = registry.getValue(a.affixId());
            if (def != null && !def.group().isEmpty() && protectedGroups.contains(def.group())) continue;
            combined.add(a);
        }
        combined.addAll(protectedAffixes);

        trimExcess(combined, registry, AffixType.PREFIX, rollRarity.maxPrefixes(), protectedIds);
        trimExcess(combined, registry, AffixType.SUFFIX, rollRarity.maxSuffixes(), protectedIds);

        return new AffixData(finalRarity, List.copyOf(combined), finalEndfused, existing.craftedAffixId(), existing.imbuedAffixId());
    }

    private static List<RolledAffix> collectProtectedAffixes(AffixData existing) {
        List<RolledAffix> result = new ArrayList<>();
        existing.craftedAffixId().ifPresent(id ->
                existing.affixes().stream().filter(a -> a.affixId().equals(id)).findFirst().ifPresent(result::add));
        existing.imbuedAffixId().ifPresent(id -> {
            if (result.stream().noneMatch(a -> a.affixId().equals(id))) {
                existing.affixes().stream().filter(a -> a.affixId().equals(id)).findFirst().ifPresent(result::add);
            }
        });
        return result;
    }

    private static void trimExcess(List<RolledAffix> combined, Registry<AffixDefinition> registry, AffixType type, int max, Set<Identifier> protectedIds) {
        List<RolledAffix> ofType = combined.stream()
                .filter(a -> {
                    AffixDefinition def = registry.getValue(a.affixId());
                    return def != null && def.type() == type;
                })
                .toList();
        int excess = ofType.size() - max;
        for (int i = ofType.size() - 1; i >= 0 && excess > 0; i--) {
            RolledAffix candidate = ofType.get(i);
            if (protectedIds.contains(candidate.affixId())) continue;
            combined.remove(candidate);
            excess--;
        }
    }

    public static AffixData addRandomAffix(RegistryAccess registryAccess, ItemStack stack, AffixData existing, RandomSource random) {
        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        Set<Identifier> usedIds = existing.affixes().stream()
                .map(RolledAffix::affixId)
                .collect(Collectors.toSet());

        Set<String> usedGroups = new HashSet<>();
        for (RolledAffix rolled : existing.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null && !def.group().isEmpty()) {
                usedGroups.add(def.group());
            }
        }

        int prefixCount = 0;
        int suffixCount = 0;
        for (RolledAffix rolled : existing.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null) {
                if (def.type() == AffixType.PREFIX) prefixCount++;
                else suffixCount++;
            }
        }

        List<WeightedAffix> pool = new ArrayList<>();
        for (var entry : registry.entrySet()) {
            AffixDefinition def = entry.getValue();
            Identifier id = entry.getKey().identifier();

            if (usedIds.contains(id)) continue;
            if (!isApplicable(def, stack)) continue;
            if (!def.group().isEmpty() && usedGroups.contains(def.group())) continue;
            if (def.type() == AffixType.PREFIX && prefixCount >= existing.rarity().maxPrefixes()) continue;
            if (def.type() == AffixType.SUFFIX && suffixCount >= existing.rarity().maxSuffixes()) continue;
            if (!hasEligibleTiers(def, stack)) continue;

            pool.add(new WeightedAffix(id, def));
        }

        if (pool.isEmpty()) return existing;

        WeightedAffix selected = selectWeighted(pool, random);
        if (selected == null) return existing;

        List<AffixTier> eligibleTiers = new ArrayList<>();
        List<Integer> eligibleIndices = new ArrayList<>();
        for (int t = 0; t < selected.def.tiers().size(); t++) {
            AffixTier tier = selected.def.tiers().get(t);
            if (tier.isApplicableTo(stack)) {
                eligibleTiers.add(tier);
                eligibleIndices.add(t);
            }
        }

        if (eligibleTiers.isEmpty()) return existing;

        int pick = random.nextInt(eligibleTiers.size());
        int tierIndex = eligibleIndices.get(pick);
        double value = eligibleTiers.get(pick).roll(random);

        List<RolledAffix> newAffixes = new ArrayList<>(existing.affixes());
        newAffixes.add(new RolledAffix(selected.id, tierIndex, value));

        return existing.withAffixes(List.copyOf(newAffixes));
    }

    private static void rollAffixes(
            Registry<AffixDefinition> registry,
            ItemStack stack,
            AffixType type,
            int count,
            Set<String> usedGroups,
            List<RolledAffix> result,
            RandomSource random
    ) {
        List<WeightedAffix> pool = new ArrayList<>();
        for (var entry : registry.entrySet()) {
            AffixDefinition def = entry.getValue();
            if (def.type() != type) continue;
            if (!isApplicable(def, stack)) continue;
            if (!def.group().isEmpty() && usedGroups.contains(def.group())) continue;
            if (!hasEligibleTiers(def, stack)) continue;

            pool.add(new WeightedAffix(entry.getKey().identifier(), def));
        }

        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            WeightedAffix selected = selectWeighted(pool, random);
            if (selected == null) break;

            List<AffixTier> eligibleTiers = new ArrayList<>();
            List<Integer> eligibleIndices = new ArrayList<>();
            for (int t = 0; t < selected.def.tiers().size(); t++) {
                AffixTier tier = selected.def.tiers().get(t);
                if (tier.isApplicableTo(stack)) {
                    eligibleTiers.add(tier);
                    eligibleIndices.add(t);
                }
            }

            if (eligibleTiers.isEmpty()) break;

            int pick = random.nextInt(eligibleTiers.size());
            int tierIndex = eligibleIndices.get(pick);
            double value = eligibleTiers.get(pick).roll(random);

            result.add(new RolledAffix(selected.id, tierIndex, value));

            // Remove from pool and mark group used
            final WeightedAffix sel = selected;
            pool.removeIf(w -> w.id.equals(sel.id));
            if (!selected.def.group().isEmpty()) {
                usedGroups.add(selected.def.group());
                pool.removeIf(w -> w.def.group().equals(sel.def.group()));
            }
        }
    }

    public static boolean isApplicable(AffixDefinition def, ItemStack stack) {
        if (def.applicableTags().isEmpty()) return true;

        for (Identifier tagId : def.applicableTags()) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
            if (stack.is(tag)) return true;
        }
        return false;
    }

    public static boolean hasEligibleTiers(AffixDefinition def, ItemStack stack) {
        for (AffixTier tier : def.tiers()) {
            if (tier.isApplicableTo(stack)) return true;
        }
        return false;
    }

    static WeightedAffix selectWeighted(List<WeightedAffix> pool, RandomSource random) {
        int totalWeight = pool.stream().mapToInt(w -> w.def.weight()).sum();
        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedAffix w : pool) {
            cumulative += w.def.weight();
            if (roll < cumulative) {
                return w;
            }
        }
        return null;
    }

    static int rollCount(RandomSource random, int max) {
        if (max <= 0) return 0;
        return 1 + random.nextInt(max);
    }

    record WeightedAffix(Identifier id, AffixDefinition def) {}
}