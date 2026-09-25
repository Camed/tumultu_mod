package com.tumultu.registry;

import com.tumultu.TumultuMod;
import com.tumultu.affix.AffixData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TumultuDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TumultuMod.MOD_ID);

    public static final Supplier<DataComponentType<AffixData>> AFFIX_DATA =
            DATA_COMPONENTS.registerComponentType("affix_data", builder -> builder
                    .persistent(AffixData.CODEC)
                    .networkSynchronized(AffixData.STREAM_CODEC)
            );


    // specific mirrored/outruled modifier handler
    public static final Supplier<DataComponentType<Unit>> OUTRULED =
            DATA_COMPONENTS.registerComponentType("outruled", builder -> builder
                    .persistent(Unit.CODEC)
                    .networkSynchronized(Unit.STREAM_CODEC)
            );
}