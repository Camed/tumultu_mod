package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ChanceToBleedEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ChanceToBleedEffect> MAP_CODEC = MapCodec.unit(() -> new ChanceToBleedEffect(true));

    @Override
    public String typeId() {
        return "chance_to_bleed";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% Chance to Bleed", rolledValue * 100.0));
    }
}
