package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixEffectApplier;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public abstract class AbstractShard extends Item implements StashApplicable {

    public AbstractShard(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        builder.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }

    protected AffixData getAffixData(ItemStack stack) {
        AffixData data = stack.get(TumultuDataComponents.AFFIX_DATA.get());
        return data != null ? data : AffixData.EMPTY;
    }

    protected void setAffixData(ItemStack stack, AffixData data, RegistryAccess registryAccess, RandomSource random) {
        stack.set(TumultuDataComponents.AFFIX_DATA.get(), data);
        AffixEffectApplier.apply(stack, data, registryAccess, random);
    }

    protected boolean isAffixable(ItemStack stack) {
        return stack.is(TumultuTags.AFFIXABLE);
    }

    protected boolean isModifiable(AffixData data) {
        return data.isModifiable();
    }
}