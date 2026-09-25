package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record CritChanceEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CritChanceEffect> MAP_CODEC = MapCodec.unit(() -> new CritChanceEffect(true));

    @Override
    public String typeId() {
        return "crit_chance";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Critical Strike Chance", rolledValue * 100.0));
    }
}
