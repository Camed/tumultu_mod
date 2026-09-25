package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/**
 * Subtracts directly from the attacker's crit damage bonus (the same additive axis as
 * {@link CritDamagePercentEffect} and the pipeline's own implicit base bonus) rather than scaling
 * the final crit multiplier - a T1 roll of 0.50 turns a 200% crit multiplier into 150%, not 100%.
 * The combined bonus is floored at zero in the pipeline, so a crit can never deal less than a
 * normal hit, only lose its bonus entirely.
 */
public record CritDamageReductionEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CritDamageReductionEffect> MAP_CODEC = MapCodec.unit(() -> new CritDamageReductionEffect(true));

    @Override
    public String typeId() {
        return "crit_damage_reduction";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("-%.0f%% Critical Strike Damage Taken", rolledValue * 100.0));
    }
}
