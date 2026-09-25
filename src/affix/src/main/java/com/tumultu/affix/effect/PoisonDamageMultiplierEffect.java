package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** Stacks additively with the shared {@link DamageOverTimeMultiplierEffect}, but only affects poison. */
public record PoisonDamageMultiplierEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<PoisonDamageMultiplierEffect> MAP_CODEC = MapCodec.unit(() -> new PoisonDamageMultiplierEffect(true));

    @Override
    public String typeId() {
        return "poison_damage_multiplier";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Poison Damage", rolledValue * 100.0));
    }
}
