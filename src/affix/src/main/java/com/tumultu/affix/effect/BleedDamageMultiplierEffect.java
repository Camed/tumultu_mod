package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
public record BleedDamageMultiplierEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<BleedDamageMultiplierEffect> MAP_CODEC = MapCodec.unit(() -> new BleedDamageMultiplierEffect(true));

    @Override
    public String typeId() {
        return "bleed_damage_multiplier";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Bleed Damage", rolledValue * 100.0));
    }
}
