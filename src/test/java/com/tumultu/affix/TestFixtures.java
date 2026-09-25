package com.tumultu.affix;

import com.tumultu.affix.effect.AffixEffect;
import com.tumultu.affix.effect.AreaMiningEffect;
import net.minecraft.resources.Identifier;

import java.util.List;

final class TestFixtures {
    private TestFixtures() {
    }

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("tumultu", path);
    }

    static AffixTier tier(double min, double max) {
        return new AffixTier(min, max, 100, List.of());
    }

    static AffixTier tier(double min, double max, List<Identifier> applicableTags) {
        return new AffixTier(min, max, 100, applicableTags);
    }

    static AffixDefinition definition(AffixType type, String group, int weight, List<AffixTier> tiers) {
        return new AffixDefinition(type, group, new AreaMiningEffect(false), tiers, weight, group, List.of());
    }

    static AffixDefinition definition(AffixType type, String group, int weight, List<AffixTier> tiers, AffixEffect effect) {
        return new AffixDefinition(type, group, effect, tiers, weight, group, List.of());
    }
}