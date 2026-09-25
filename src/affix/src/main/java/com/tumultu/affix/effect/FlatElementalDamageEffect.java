package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

public record FlatElementalDamageEffect(ElementKind element, boolean isPercentage) implements AffixEffect {
    public static final MapCodec<FlatElementalDamageEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ElementKind.CODEC.fieldOf("element").forGetter(FlatElementalDamageEffect::element),
                    MapCodec.unit(false).forGetter(FlatElementalDamageEffect::isPercentage)
            ).apply(instance, FlatElementalDamageEffect::new)
    );

    @Override
    public String typeId() {
        return "flat_elemental_damage";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("+%.1f %s Damage", rolledValue, element.displayName()));
    }
}
