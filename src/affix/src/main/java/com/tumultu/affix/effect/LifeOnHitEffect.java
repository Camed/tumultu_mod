package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record LifeOnHitEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<LifeOnHitEffect> MAP_CODEC = MapCodec.unit(() -> new LifeOnHitEffect(false));

    @Override
    public String typeId() {
        return "life_on_hit";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.1f Life per Hit", rolledValue));
    }
}
