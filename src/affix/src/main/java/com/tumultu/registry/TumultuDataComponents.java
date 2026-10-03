package com.tumultu.registry;

import com.tumultu.TumultuMod;
import com.tumultu.affix.AffixData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
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

    // Set once, permanently, by a Shard of Discovery - gates which affixes an item is eligible
    // for (AffixRoller/BenchCrafting) and whether any other currency can be used on it at all
    // (AbstractShard.isAffixable). Absence of this component, not a value of 0, means "never
    // discovered yet" - nothing else in the codebase writes to it once set.
    public static final Supplier<DataComponentType<Integer>> ITEM_LEVEL =
            DATA_COMPONENTS.registerComponentType("item_level", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
            );

    // The world-tier/biome-constant tier (1-21) a Shard of Discovery dropped at - stamped once at
    // drop time, read when the shard is consumed to pick its item-level roll band. Shown in its
    // own tooltip too, hence networked.
    public static final Supplier<DataComponentType<Integer>> DISCOVERY_SHARD_TIER =
            DATA_COMPONENTS.registerComponentType("discovery_shard_tier", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
            );
}