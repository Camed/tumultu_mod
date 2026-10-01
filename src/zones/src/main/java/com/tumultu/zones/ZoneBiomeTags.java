package com.tumultu.zones;

import com.tumultu.TumultuMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ZoneBiomeTags {
    private static final TagKey<Biome>[] TIER_TAGS = buildTierTags();

    private ZoneBiomeTags() {
    }

    @SuppressWarnings("unchecked")
    private static TagKey<Biome>[] buildTierTags() {
        TagKey<Biome>[] tags = new TagKey[WorldTier.MAX + 1];
        for (int tier = 1; tier <= WorldTier.MAX; tier++) {
            tags[tier] = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "tier" + tier + "_biome"));
        }
        return tags;
    }

    public static int constantFor(Holder<Biome> biome) {
        for (int tier = WorldTier.MAX; tier >= 1; tier--) {
            if (biome.is(TIER_TAGS[tier])) {
                return tier;
            }
        }
        return 0;
    }
}
