package com.tumultu.zones;

import com.tumultu.TumultuMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.Map;

public class DropRarityWeightsReloadListener extends SimpleJsonResourceReloadListener<DropRarityWeights> {
    private static volatile DropRarityWeights current = DropRarityWeights.IDENTITY;

    public DropRarityWeightsReloadListener() {
        super(DropRarityWeights.CODEC, FileToIdConverter.json("drop_rarity_weights"));
    }

    public static DropRarityWeights current() {
        return current;
    }

    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "drop_rarity_weights"), new DropRarityWeightsReloadListener());
    }

    @Override
    protected void apply(Map<Identifier, DropRarityWeights> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        current = data.values().stream().findFirst().orElse(DropRarityWeights.IDENTITY);
    }
}
