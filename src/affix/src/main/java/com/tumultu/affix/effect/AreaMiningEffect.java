package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record AreaMiningEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<AreaMiningEffect> MAP_CODEC = MapCodec.unit(() -> new AreaMiningEffect(false));

    /// How big is the mining plane according to the affix tier, from smallest to largest
    private static final int[][] SIZES = {
            {2, 2},
            {3, 2},
            {3, 3},
            {4, 3},
            {4, 4}
    };

    @Override
    public String typeId() {
        return "area_mining";
    }

    public static int[] sizeFor(double rolledValue) {
        int index = Math.round((float) rolledValue) - 1;
        index = Math.max(0, Math.min(SIZES.length - 1, index));
        return SIZES[index];
    }

    @Override
    public Component describe(double rolledValue) {
        int[] size = sizeFor(rolledValue);
        return Component.translatable("tumultu.effect.area_mining", size[0], size[1]);
    }
}