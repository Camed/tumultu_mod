package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** Only applies when the hit's {@code DamageSource.isDirect()} is true (the attacker itself
 * touched the victim) - see {@code CombatEventHandler.onIncomingDamage}. A bow/crossbow/thrown-
 * trident hit is never direct, so this never stacks with {@link RangedDamagePercentEffect}. */
public record MeleeDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<MeleeDamagePercentEffect> MAP_CODEC = MapCodec.unit(() -> new MeleeDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "melee_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Melee Damage", rolledValue * 100.0));
    }
}
