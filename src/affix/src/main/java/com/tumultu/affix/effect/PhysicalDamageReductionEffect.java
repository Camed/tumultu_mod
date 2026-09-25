package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record PhysicalDamageReductionEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<PhysicalDamageReductionEffect> MAP_CODEC = MapCodec.unit(() -> new PhysicalDamageReductionEffect(true));

    @Override
    public String typeId() {
        return "physical_damage_reduction";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% additional Physical Damage Reduction", rolledValue * 100.0));
    }
}
