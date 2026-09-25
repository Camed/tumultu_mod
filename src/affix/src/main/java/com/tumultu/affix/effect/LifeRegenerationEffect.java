package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record LifeRegenerationEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<LifeRegenerationEffect> MAP_CODEC = MapCodec.unit(() -> new LifeRegenerationEffect(false));

    @Override
    public String typeId() {
        return "life_regeneration";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.1f Life Regenerated per Second", rolledValue));
    }
}
