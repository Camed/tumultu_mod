package com.tumultu.zones;

import com.tumultu.TumultuMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.Map;

public class WorldTierScalingReloadListener extends SimpleJsonResourceReloadListener<WorldTierScaling> {
    private static volatile WorldTierScaling current = WorldTierScaling.IDENTITY;

    public WorldTierScalingReloadListener() {
        super(WorldTierScaling.CODEC, FileToIdConverter.json("world_tier_scaling"));
    }

    public static WorldTierScaling current() {
        return current;
    }

    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "world_tier_scaling"), new WorldTierScalingReloadListener());
    }

    @Override
    protected void apply(Map<Identifier, WorldTierScaling> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        current = data.values().stream().findFirst().orElse(WorldTierScaling.IDENTITY);
    }
}
