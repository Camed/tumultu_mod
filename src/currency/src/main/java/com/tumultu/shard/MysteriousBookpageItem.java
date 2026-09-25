package com.tumultu.shard;

import com.tumultu.registry.TumultuDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
public class MysteriousBookpageItem extends Item implements StashApplicable {

    public MysteriousBookpageItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        builder.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack target = player.getOffhandItem();
        if (target.isEmpty()) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!tryApply((ServerLevel) level, player, target)) return InteractionResult.PASS;
        player.getMainHandItem().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean tryApply(ServerLevel level, Player player, ItemStack target) {
        if (target.has(TumultuDataComponents.OUTRULED.get())) return false;

        ItemStack copy = target.copyWithCount(1);
        copy.set(TumultuDataComponents.OUTRULED.get(), Unit.INSTANCE);
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
        return true;
    }
}
