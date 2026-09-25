package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
public record FlatPhysicalDamageEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<FlatPhysicalDamageEffect> MAP_CODEC = MapCodec.unit(() -> new FlatPhysicalDamageEffect(false));

    @Override
    public String typeId() {
        return "flat_physical_damage";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.1f Physical Damage", rolledValue));
    }
}
