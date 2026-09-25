package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record LifeOnKillEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<LifeOnKillEffect> MAP_CODEC = MapCodec.unit(() -> new LifeOnKillEffect(false));

    @Override
    public String typeId() {
        return "life_on_kill";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.1f Life per Kill", rolledValue));
    }
}
