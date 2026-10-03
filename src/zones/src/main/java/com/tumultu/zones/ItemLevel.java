package com.tumultu.zones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

public final class ItemLevel {
    public static final int MAX_DISCOVERY_TIER = 21;

    private ItemLevel() {
    }

    public static int discoveryTierAt(ServerLevel level, BlockPos pos) {
        int worldTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
        Holder<Biome> biome = level.getBiome(pos);
        return Math.min(worldTier + ZoneBiomeTags.constantFor(biome), MAX_DISCOVERY_TIER);
    }
}
