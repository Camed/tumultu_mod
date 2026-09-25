package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record HungerOnKillEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<HungerOnKillEffect> MAP_CODEC = MapCodec.unit(() -> new HungerOnKillEffect(false));

    @Override
    public String typeId() {
        return "hunger_on_kill";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.1f Hunger per Kill", rolledValue));
    }
}
