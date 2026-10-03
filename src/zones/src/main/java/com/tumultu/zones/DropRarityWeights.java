package com.tumultu.zones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collections;
import java.util.List;

public record DropRarityWeights(List<List<Double>> bands) {
    public static final Codec<DropRarityWeights> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.listOf().listOf().fieldOf("bands").forGetter(DropRarityWeights::bands)
            ).apply(instance, DropRarityWeights::new));

    public static final DropRarityWeights IDENTITY =
            new DropRarityWeights(Collections.nCopies(WorldTier.MAX + 1, List.of(1.0, 1.0, 1.0, 1.0)));

    public List<Double> weightsFor(int tier) {
        if (bands.isEmpty()) {
            return List.of(1.0, 1.0, 1.0, 1.0);
        }
        int clamped = Math.max(0, Math.min(tier, bands.size() - 1));
        return bands.get(clamped);
    }
}
