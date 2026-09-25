package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record PhysicalDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<PhysicalDamagePercentEffect> MAP_CODEC = MapCodec.unit(() -> new PhysicalDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "physical_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Physical Damage", rolledValue * 100.0));
    }
}
