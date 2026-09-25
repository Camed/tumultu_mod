package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
public record HybridArmorLifeEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<HybridArmorLifeEffect> MAP_CODEC = MapCodec.unit(() -> new HybridArmorLifeEffect(true));

    public static final double LIFE_PER_ARMOR_PERCENT = 20.0;

    @Override
    public String typeId() {
        return "hybrid_armor_life";
    }

    public double lifeFor(double rolledArmorPercent) {
        return rolledArmorPercent * LIFE_PER_ARMOR_PERCENT;
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.0f%% Increased Armour and +%.1f Life", rolledValue * 100.0, lifeFor(rolledValue)));
    }

}
