package com.tumultu.shard;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Upgrades a random enchantment on the target item by 1 level (rarely 2, extremely rarely 3).
 * Doesn't touch AffixData at all - works on any enchanted item, Tumultu-affixable or not - so
 * this extends {@link Item} directly rather than {@link AbstractShard}.
 */
public class EmpoweringGemItem extends Item implements StashApplicable {

    public EmpoweringGemItem(Properties properties) {
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
        ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(target);
        if (current.isEmpty()) return false;

        List<Holder<Enchantment>> eligible = new ArrayList<>();
        for (Holder<Enchantment> holder : current.keySet()) {
            if (current.getLevel(holder) < holder.value().getMaxLevel()) {
                eligible.add(holder);
            }
        }
        if (eligible.isEmpty()) return false;

        var random = level.getRandom();
        Holder<Enchantment> chosen = eligible.get(random.nextInt(eligible.size()));
        int currentLevel = current.getLevel(chosen);
        int maxLevel = chosen.value().getMaxLevel();
        int newLevel = Math.min(currentLevel + EnchantLevelRoll.pickLevelIncrease(random.nextDouble()), maxLevel);

        EnchantmentHelper.updateEnchantments(target, mutable -> mutable.set(chosen, newLevel));
        return true;
    }
}
