package com.tumultu.affix.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * Split out of {@link AttributeEffect} so this pure formatting logic is unit-testable on its own -
 * {@code AttributeEffect}'s static {@code MAP_CODEC} field references {@code Attribute.CODEC},
 * which touches {@code BuiltInRegistries} at class-init time and can't run outside a real
 * ModLauncher environment; merely calling a static method on {@code AttributeEffect} would trigger
 * that same class-init and blow up in plain JUnit here, even for logic that itself never touches
 * Attribute/registries.
 */
final class AttributeDisplayScale {
    private AttributeDisplayScale() {}

    /**
     * Mirrors {@code ItemAttributeModifiers}' own tooltip generation: percentage-style operations
     * are displayed ×100 (0.1 -> "10", read as "10%" once the translation key appends the % sign),
     * while ADD_VALUE's key has no % sign and expects the raw number as-is.
     */
    static double displayAmount(double rolledValue, Operation operation) {
        return isPercentage(operation) ? Math.abs(rolledValue) * 100.0 : Math.abs(rolledValue);
    }

    static boolean isPercentage(Operation operation) {
        return operation == Operation.ADD_MULTIPLIED_BASE || operation == Operation.ADD_MULTIPLIED_TOTAL;
    }
}
