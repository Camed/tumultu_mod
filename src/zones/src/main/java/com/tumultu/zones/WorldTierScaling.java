package com.tumultu.zones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collections;
import java.util.List;

public record WorldTierScaling(List<Double> multipliers) {
    public static final Codec<WorldTierScaling> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.listOf().fieldOf("multipliers").forGetter(WorldTierScaling::multipliers)
            ).apply(instance, WorldTierScaling::new)
    );

    public static final WorldTierScaling IDENTITY =
            new WorldTierScaling(Collections.nCopies(WorldTier.MAX + 1, 1.0));

    public double multiplierFor(int tier) {
        if (multipliers.isEmpty()) {
            return 1.0;
        }
        int clamped = Math.max(0, Math.min(tier, multipliers.size() - 1));
        return multipliers.get(clamped);
    }
}
