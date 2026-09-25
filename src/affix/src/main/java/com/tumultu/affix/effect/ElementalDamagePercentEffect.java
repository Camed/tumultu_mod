package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ElementalDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ElementalDamagePercentEffect> MAP_CODEC = MapCodec.unit(() -> new ElementalDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "elemental_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Elemental Damage", rolledValue * 100.0));
    }
}
