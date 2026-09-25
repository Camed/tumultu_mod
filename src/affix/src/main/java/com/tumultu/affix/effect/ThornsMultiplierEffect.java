package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ThornsMultiplierEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ThornsMultiplierEffect> MAP_CODEC = MapCodec.unit(() -> new ThornsMultiplierEffect(true));

    @Override
    public String typeId() {
        return "thorns_multiplier";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% increased Thorns Damage", rolledValue * 100.0));
    }
}
