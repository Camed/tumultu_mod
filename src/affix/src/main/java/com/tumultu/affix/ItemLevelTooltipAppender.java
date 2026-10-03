package com.tumultu.affix;

import com.tumultu.registry.TumultuDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ItemLevelTooltipAppender implements TooltipAppender {

    @Override
    public void append(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                        @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder) {
        Integer level = stack.get(TumultuDataComponents.ITEM_LEVEL.get());
        if (level == null) {
            return;
        }
        builder.accept(Component.translatable("tumultu.item_level", level).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
