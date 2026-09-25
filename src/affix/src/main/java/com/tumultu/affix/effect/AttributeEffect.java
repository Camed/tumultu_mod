package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record AttributeEffect(
        Holder<Attribute> attribute,
        AttributeModifier.Operation operation
) implements AffixEffect {
    public static final MapCodec<AttributeEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Attribute.CODEC.fieldOf("attribute").forGetter(AttributeEffect::attribute),
                    StringRepresentable.fromEnum(AttributeModifier.Operation::values)
                            .optionalFieldOf("operation", AttributeModifier.Operation.ADD_VALUE)
                            .forGetter(AttributeEffect::operation)
            ).apply(instance, AttributeEffect::new)
    );

    @Override
    public String typeId() {
        return "attribute";
    }

    @Override
    public Component describe(double rolledValue) {
        String key = rolledValue >= 0
                ? "attribute.modifier.plus." + operation.id()
                : "attribute.modifier.take." + operation.id();

        String formatted = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(AttributeDisplayScale.displayAmount(rolledValue, operation));
        return Component.translatable(key, formatted, Component.translatable(attribute.value().getDescriptionId()));
    }

    // Derived from `operation` rather than an independent component - the two must never disagree.
    @Override
    public boolean isPercentage() {
        return AttributeDisplayScale.isPercentage(operation);
    }
}