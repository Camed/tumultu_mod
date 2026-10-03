package com.tumultu.loot;

import com.tumultu.registry.CurrencyItems;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.zones.DiscoveryShardChanceReloadListener;
import com.tumultu.zones.ItemLevel;
import com.tumultu.zones.WorldTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class ShardOfDiscoveryDropHandler {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity instanceof Monster) || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = entity.blockPosition();
        if (WorldTier.fromPosition(pos.getX(), pos.getZ()) == 0) {
            return;
        }

        int tier = ItemLevel.discoveryTierAt(serverLevel, pos);
        double chance = DiscoveryShardChanceReloadListener.current().multiplierFor(tier);
        if (entity.getRandom().nextDouble() >= chance) {
            return;
        }

        ItemStack shard = new ItemStack(CurrencyItems.SHARD_OF_DISCOVERY.get());
        shard.set(TumultuDataComponents.DISCOVERY_SHARD_TIER.get(), tier);
        event.getDrops().add(new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, shard));
    }
}
