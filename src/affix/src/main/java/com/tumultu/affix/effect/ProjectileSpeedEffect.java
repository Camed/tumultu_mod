package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ProjectileSpeedEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ProjectileSpeedEffect> MAP_CODEC = MapCodec.unit(() -> new ProjectileSpeedEffect(true));

    @Override
    public String typeId() {
        return "projectile_speed_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Projectile Speed", rolledValue * 100.0));
    }
}
