package com.tumultu.zones;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ZoneTickHandler {
    // World tier only changes every 1000 blocks, so checking every tick is wasted work - once a
    // second is already far more often than a player can realistically cross a threshold twice.
    private static final int CHECK_INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        BlockPos pos = player.blockPosition();
        int currentTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
        int lastTier = player.getData(ZoneAttachments.LAST_WORLD_TIER.get());
        if (currentTier == lastTier) return;

        player.setData(ZoneAttachments.LAST_WORLD_TIER.get(), currentTier);
        player.sendOverlayMessage(Component.translatable("tumultu.world_tier.entered", currentTier));
    }
}
