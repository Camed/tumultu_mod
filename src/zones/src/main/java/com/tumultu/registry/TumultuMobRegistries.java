package com.tumultu.registry;

import com.tumultu.TumultuMod;
import com.tumultu.mobs.definition.MobDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

// mob registry for blightlands, todo: probably will need refactor when new biomes will appear
public class TumultuMobRegistries {
    public static final ResourceKey<Registry<MobDefinition>> MOB_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "mob"));

    public static final DeferredRegister<MobDefinition> MOB_DEFINITIONS =
            DeferredRegister.create(MOB_KEY, TumultuMod.MOD_ID);

    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                MOB_KEY,
                MobDefinition.CODEC,
                MobDefinition.CODEC
        );
    }
}
