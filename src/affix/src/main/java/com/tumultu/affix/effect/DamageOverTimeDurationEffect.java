package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record DamageOverTimeDurationEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<DamageOverTimeDurationEffect> MAP_CODEC = MapCodec.unit(() -> new DamageOverTimeDurationEffect(true));

    @Override
    public String typeId() {
        return "damage_over_time_duration";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Damage over Time Duration", rolledValue * 100.0));
    }
}
