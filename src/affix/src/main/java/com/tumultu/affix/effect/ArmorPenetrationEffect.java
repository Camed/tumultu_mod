package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ArmorPenetrationEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ArmorPenetrationEffect> MAP_CODEC = MapCodec.unit(() -> new ArmorPenetrationEffect(true));

    @Override
    public String typeId() {
        return "armor_penetration";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% Armor Penetration", rolledValue * 100.0));
    }
}
