package com.tumultu.zones;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ZoneTickHandler {
    // World tier only changes every 1000 blocks, so checking every tick is wasted work - once a
    // second is already far more often than a player can realistically cross a threshold twice.
    private static final int CHECK_INTERVAL_TICKS = 20;

    private static final int FADE_IN_TICKS = 10;
    private static final int STAY_TICKS = 90;
    private static final int FADE_OUT_TICKS = 20;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        BlockPos pos = player.blockPosition();
        int currentTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
        int lastTier = player.getData(ZoneAttachments.LAST_WORLD_TIER.get());
        if (currentTier == lastTier) return;

        player.setData(ZoneAttachments.LAST_WORLD_TIER.get(), currentTier);

        Component title = Component.translatable("tumultu.world_tier.entered", currentTier)
                .withStyle(ChatFormatting.BOLD, currentTier > lastTier ? ChatFormatting.RED : ChatFormatting.GREEN);
        Component subtitle = Component.translatable(currentTier > lastTier
                        ? "tumultu.world_tier.increased" : "tumultu.world_tier.decreased")
                .withStyle(ChatFormatting.GRAY);

        player.connection.send(new ClientboundSetTitlesAnimationPacket(FADE_IN_TICKS, STAY_TICKS, FADE_OUT_TICKS));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.level().playSound(null, pos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.AMBIENT, 1.0F, 1.0F);
    }
}
