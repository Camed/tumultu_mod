package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** "Increased Damage" from all sources (physical AND elemental alike) - an "increased" (additive)
 * bonus, not a "more" (separate multiplicative) one, so it stacks into whichever pool a given hit
 * actually uses: {@link PhysicalDamagePercentEffect}'s pool for a physical hit, {@link
 * ElementalDamagePercentEffect}'s pool for an elemental follow-up - see {@code
 * CombatEventHandler.onIncomingDamage}/{@code onDamagePost}. Introduced for Pillager's Favour
 * (mimicking PoE's "Le Heup of All"), but a genuinely reusable stat, not a one-off hardcoded
 * unique power. */
public record AllDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<AllDamagePercentEffect> MAP_CODEC = MapCodec.unit(() -> new AllDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "all_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%+.0f%% Damage", rolledValue * 100.0));
    }
}
