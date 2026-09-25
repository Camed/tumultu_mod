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

/**
 * Basically PoE "Mirrored" system. Cannot "Outrule" already "Outruled" items.
 */
public class OutruledTooltipAppender implements TooltipAppender {

    @Override
    public void append(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                        @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder) {
        if (!stack.has(TumultuDataComponents.OUTRULED.get())) {
            return;
        }
        builder.accept(Component.translatable("tumultu.outruled_tag").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
}
