package com.tumultu.affix;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Optional;

// The better the ingot within crafting bench, the better rolls it skews towards.
public enum PaymentTier {
    IRON(1.0),
    GOLD(1.7),
    DIAMOND(2.5),
    NETHERITE(4.0);

    private final double biasExponent;

    PaymentTier(double biasExponent) {
        this.biasExponent = biasExponent;
    }

    public double biasExponent() {
        return biasExponent;
    }

    public static Optional<PaymentTier> of(Item item) {
        if (item == Items.IRON_INGOT) return Optional.of(IRON);
        if (item == Items.GOLD_INGOT) return Optional.of(GOLD);
        if (item == Items.DIAMOND) return Optional.of(DIAMOND);
        if (item == Items.NETHERITE_INGOT) return Optional.of(NETHERITE);
        return Optional.empty();
    }
}
