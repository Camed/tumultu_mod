package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ElementalPenetrationEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ElementalPenetrationEffect> MAP_CODEC = MapCodec.unit(() -> new ElementalPenetrationEffect(true));

    @Override
    public String typeId() {
        return "elemental_penetration";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% Elemental Penetration", rolledValue * 100.0));
    }
}
