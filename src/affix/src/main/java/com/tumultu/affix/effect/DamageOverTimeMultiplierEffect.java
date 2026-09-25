package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record DamageOverTimeMultiplierEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<DamageOverTimeMultiplierEffect> MAP_CODEC = MapCodec.unit(() -> new DamageOverTimeMultiplierEffect(true));

    @Override
    public String typeId() {
        return "damage_over_time_multiplier";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Damage over Time", rolledValue * 100.0));
    }
}
