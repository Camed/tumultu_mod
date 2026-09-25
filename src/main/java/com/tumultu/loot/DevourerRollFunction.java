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
public class DevourerRollFunction extends LootItemConditionalFunction {
    public static final MapCodec<DevourerRollFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance).apply(instance, DevourerRollFunction::new));

    protected DevourerRollFunction(List<LootItemCondition> predicates) {
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

        AffixData rerolled = TumultuUniqueItems.rerollSingleAffix(
                existing, TumultuUniqueItems.DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID, registry, random);
        stack.set(TumultuDataComponents.AFFIX_DATA.get(), rerolled);

        // The max-health cost is real vanilla-attribute state (see TumultuUniqueItems.DEVOURER's
        // own doc comment for why), so it needs updating here too, in lockstep with the AffixData
        // entry above - otherwise a freshly-rolled 30% damage bonus would still only cost the 20%
        // placeholder's worth of max health.
        double lifeToDamage = TumultuUniqueItems.valueOf(rerolled, TumultuUniqueItems.DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID);
        TumultuUniqueItems.updateCurioAttributeModifier(stack, Attributes.MAX_HEALTH, new AttributeModifier(
                TumultuUniqueItems.DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID, -lifeToDamage, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        return stack;
    }
}
