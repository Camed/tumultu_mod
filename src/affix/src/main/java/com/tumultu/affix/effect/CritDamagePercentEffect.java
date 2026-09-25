package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record CritDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<CritDamagePercentEffect> MAP_CODEC = MapCodec.unit(() -> new CritDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "crit_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Critical Strike Damage", rolledValue * 100.0));
    }
}
