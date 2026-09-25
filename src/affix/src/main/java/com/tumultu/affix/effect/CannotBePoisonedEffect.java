package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record CannotBePoisonedEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CannotBePoisonedEffect> MAP_CODEC = MapCodec.unit(() -> new CannotBePoisonedEffect(false));

    @Override
    public String typeId() {
        return "cannot_be_poisoned";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal("Cannot be Poisoned");
    }
}
