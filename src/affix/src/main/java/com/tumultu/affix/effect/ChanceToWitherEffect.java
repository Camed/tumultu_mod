package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
public record ChanceToWitherEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ChanceToWitherEffect> MAP_CODEC = MapCodec.unit(() -> new ChanceToWitherEffect(true));

    @Override
    public String typeId() {
        return "chance_to_wither";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% Chance to Wither", rolledValue * 100.0));
    }
}
