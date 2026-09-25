package com.tumultu.registry;

import com.tumultu.TumultuMod;
import com.tumultu.affix.AffixDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;


// Registries for affixes and their definitions
public class TumultuRegistries {
    public static final ResourceKey<Registry<AffixDefinition>> AFFIX_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "affix"));

    public static final DeferredRegister<AffixDefinition> AFFIX_DEFINITIONS =
            DeferredRegister.create(AFFIX_KEY, TumultuMod.MOD_ID);

    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                AFFIX_KEY,
                AffixDefinition.CODEC,
                AffixDefinition.CODEC
        );
    }
}
