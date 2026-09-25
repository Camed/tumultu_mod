package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record CannotBeBledEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CannotBeBledEffect> MAP_CODEC = MapCodec.unit(() -> new CannotBeBledEffect(false));

    @Override
    public String typeId() {
        return "cannot_be_bled";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal("Cannot Bleed");
    }
}
