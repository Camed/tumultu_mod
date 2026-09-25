package com.tumultu.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.registry.TumultuUniqueItems;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.List;

public class PillagersFavourRollFunction extends LootItemConditionalFunction {
    public static final MapCodec<PillagersFavourRollFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance).apply(instance, PillagersFavourRollFunction::new));

    protected PillagersFavourRollFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        AffixData existing = stack.get(TumultuDataComponents.AFFIX_DATA.get());
        if (existing == null) {
            return stack;
        }

        Registry<AffixDefinition> registry = context.getLevel().registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        RandomSource random = context.getRandom();

        AffixData rerolled = existing;
        rerolled = TumultuUniqueItems.rerollSingleAffix(rerolled, TumultuUniqueItems.PILLAGERS_FAVOUR_DAMAGE_PERCENT_AFFIX_ID, registry, random);
        rerolled = TumultuUniqueItems.rerollSingleAffix(rerolled, TumultuUniqueItems.PILLAGERS_FAVOUR_RESISTANCE_AFFIX_ID, registry, random);
        rerolled = TumultuUniqueItems.rerollSingleAffix(rerolled, TumultuUniqueItems.PILLAGERS_FAVOUR_LUCK_AFFIX_ID, registry, random);
        stack.set(TumultuDataComponents.AFFIX_DATA.get(), rerolled);

        double luck = TumultuUniqueItems.valueOf(rerolled, TumultuUniqueItems.PILLAGERS_FAVOUR_LUCK_AFFIX_ID);
        TumultuUniqueItems.updateCurioAttributeModifier(stack, Attributes.LUCK, new AttributeModifier(
                TumultuUniqueItems.PILLAGERS_FAVOUR_LUCK_AFFIX_ID, luck, AttributeModifier.Operation.ADD_VALUE));

        return stack;
    }
}
