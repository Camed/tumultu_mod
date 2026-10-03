package com.tumultu.shard;

import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ShardOfDiscoveryItem extends Item implements StashApplicable {
    private static final int LEVELS_PER_TIER = 5;

    public ShardOfDiscoveryItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        Integer tier = itemStack.get(TumultuDataComponents.DISCOVERY_SHARD_TIER.get());
        int effectiveTier = tier != null ? tier : 1;
        builder.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tumultu.discovery_shard.tier", effectiveTier,
                        minLevelFor(effectiveTier), maxLevelFor(effectiveTier))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack shard = player.getMainHandItem();
        ItemStack target = player.getOffhandItem();
        if (target.isEmpty()) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!tryApplyShard((ServerLevel) level, shard, target)) return InteractionResult.PASS;
        shard.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private boolean tryApplyShard(ServerLevel level, ItemStack shard, ItemStack target) {
        if (!target.is(TumultuTags.AFFIXABLE)) return false;
        if (target.has(TumultuDataComponents.ITEM_LEVEL.get())) return false;

        Integer tier = shard.get(TumultuDataComponents.DISCOVERY_SHARD_TIER.get());
        int effectiveTier = tier != null ? tier : 1;

        RandomSource random = level.getRandom();
        int min = minLevelFor(effectiveTier);
        int max = maxLevelFor(effectiveTier);
        int rolledLevel = min + random.nextInt(max - min + 1);

        target.set(TumultuDataComponents.ITEM_LEVEL.get(), rolledLevel);
        return true;
    }

    private static int minLevelFor(int tier) {
        return LEVELS_PER_TIER * (tier - 1) + 1;
    }

    private static int maxLevelFor(int tier) {
        return LEVELS_PER_TIER * tier;
    }

    /** Required by {@link StashApplicable} to be concrete, but this item deliberately isn't wired
     * into the Shard Stash (see class doc) - this path is never exercised in normal play since it
     * has no way to know which specific stack's tier to use. */
    @Override
    public boolean tryApply(ServerLevel level, Player player, ItemStack target) {
        return false;
    }
}
