package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** A "more"/"less" multiplier on the wearer's TOTAL crit chance (after every {@link
 * CritChanceEffect} has already been summed), not another additive contribution to that sum -
 * see {@code PlayerCombatStats#critChance}. A rolled value of 1.0 means "+100% more", i.e. doubles
 * whatever crit chance the wearer already has. Introduced for Crown of Stability, but a genuinely
 * reusable stat. */
public record CritChanceMultiplierEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CritChanceMultiplierEffect> MAP_CODEC = MapCodec.unit(() -> new CritChanceMultiplierEffect(true));

    @Override
    public String typeId() {
        return "crit_chance_multiplier";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%+.0f%% more Crit Chance", rolledValue * 100.0));
    }
}
