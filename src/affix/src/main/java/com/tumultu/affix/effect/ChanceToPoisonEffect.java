package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ChanceToPoisonEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ChanceToPoisonEffect> MAP_CODEC = MapCodec.unit(() -> new ChanceToPoisonEffect(true));

    @Override
    public String typeId() {
        return "chance_to_poison";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% Chance to Poison", rolledValue * 100.0));
    }
}
