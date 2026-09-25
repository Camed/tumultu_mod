package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ChanceToFireAdditionalArrowEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ChanceToFireAdditionalArrowEffect> MAP_CODEC = MapCodec.unit(() -> new ChanceToFireAdditionalArrowEffect(true));

    @Override
    public String typeId() {
        return "chance_extra_arrow";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% Chance to Fire an Additional Arrow", rolledValue * 100.0));
    }
}
