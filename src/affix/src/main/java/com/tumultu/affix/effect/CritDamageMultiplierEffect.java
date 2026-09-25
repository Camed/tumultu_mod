package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** A "more"/"less" multiplier on the ATTACKER's own total crit damage bonus (the base +100% plus
 * any {@link CritDamagePercentEffect}, before the victim's own {@link CritDamageReductionEffect}
 * chips away at the result) - see {@code CombatEventHandler#critMultiplier}. A rolled value of
 * -0.50 means "50% less", i.e. halves that whole bonus. Introduced for Crown of Stability (+100%
 * more Crit Chance traded for -50% Crit Damage bonus), but a genuinely reusable stat. */
public record CritDamageMultiplierEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CritDamageMultiplierEffect> MAP_CODEC = MapCodec.unit(() -> new CritDamageMultiplierEffect(true));

    @Override
    public String typeId() {
        return "crit_damage_multiplier";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%+.0f%% more Crit Damage", rolledValue * 100.0));
    }
}
