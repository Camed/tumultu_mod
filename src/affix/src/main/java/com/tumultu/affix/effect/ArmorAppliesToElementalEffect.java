package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ArmorAppliesToElementalEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ArmorAppliesToElementalEffect> MAP_CODEC = MapCodec.unit(() -> new ArmorAppliesToElementalEffect(true));

    @Override
    public String typeId() {
        return "armor_applies_to_elemental";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%.0f%% of Armour applies to Elemental Damage", rolledValue * 100.0));
    }
}
