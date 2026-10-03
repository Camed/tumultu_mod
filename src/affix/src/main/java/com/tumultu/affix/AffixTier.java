package com.tumultu.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AffixTier(
        double minValue,
        double maxValue,
        int weight,
        List<Identifier> applicableTags,
        int requiredItemLevel
) {
    public static final Codec<AffixTier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("min_value").forGetter(AffixTier::minValue),
                    Codec.DOUBLE.fieldOf("max_value").forGetter(AffixTier::maxValue),
                    Codec.INT.optionalFieldOf("weight", 100).forGetter(AffixTier::weight),
                    Identifier.CODEC.listOf().optionalFieldOf("applicable_tags", List.of()).forGetter(AffixTier::applicableTags),
                    Codec.INT.optionalFieldOf("required_item_level", 1).forGetter(AffixTier::requiredItemLevel)
            ).apply(instance, AffixTier::new)
    );

    public AffixTier(double minValue, double maxValue, int weight, List<Identifier> applicableTags) {
        this(minValue, maxValue, weight, applicableTags, 1);
    }

    public double roll(RandomSource random) {
        double value = minValue + (maxValue - minValue) * random.nextDouble();
        return Math.round(value * 100.0) / 100.0;
    }

    public boolean isApplicableTo(ItemStack stack) {
        if (applicableTags.isEmpty()) return true;

        for (Identifier tagId : applicableTags) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
            if (stack.is(tag)) return true;
        }
        return false;
    }

    public boolean isEligibleFor(ItemStack stack, int itemLevel) {
        return isApplicableTo(stack) && requiredItemLevel <= itemLevel;
    }
}