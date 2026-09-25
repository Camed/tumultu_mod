package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** Only applies when the hit's {@code DamageSource.isDirect()} is false (e.g. an arrow, where the
 * direct entity is the projectile rather than the shooter) - see
 * {@code CombatEventHandler.onIncomingDamage}. Never stacks with {@link MeleeDamagePercentEffect}. */
public record RangedDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<RangedDamagePercentEffect> MAP_CODEC = MapCodec.unit(() -> new RangedDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "ranged_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Ranged Damage", rolledValue * 100.0));
    }
}
