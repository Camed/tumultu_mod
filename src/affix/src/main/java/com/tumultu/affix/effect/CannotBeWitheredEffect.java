package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record CannotBeWitheredEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CannotBeWitheredEffect> MAP_CODEC = MapCodec.unit(() -> new CannotBeWitheredEffect(false));

    @Override
    public String typeId() {
        return "cannot_be_withered";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal("Cannot be Withered");
    }
}
