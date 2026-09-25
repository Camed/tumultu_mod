package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
public record ElementalResistanceEffect(ElementKind element, boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ElementalResistanceEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ElementKind.CODEC.fieldOf("element").forGetter(ElementalResistanceEffect::element),
                    MapCodec.unit(true).forGetter(ElementalResistanceEffect::isPercentage)
            ).apply(instance, ElementalResistanceEffect::new)
    );

    @Override
    public String typeId() {
        return "elemental_resistance";
    }

    @Override
    public Component describe(double rolledValue) {
        // %+ (not a literal "+") so a negative roll (e.g. Dragon's Heart's -50%) reads as "-50%"
        // instead of doubling up into "+-50%".
        return Component.literal(String.format("%+.0f%% %s Resistance", rolledValue * 100.0, element.displayName()));
    }
}
