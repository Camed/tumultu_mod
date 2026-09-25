package com.tumultu.registry;

import com.mojang.serialization.MapCodec;
import com.tumultu.Tumultu;
import com.tumultu.loot.DevourerRollFunction;
import com.tumultu.loot.PillagersFavourRollFunction;
import com.tumultu.loot.VillagersGambleRollFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class TumultuLootFunctions {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Tumultu.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<DevourerRollFunction>> DEVOURER_ROLL =
            LOOT_FUNCTIONS.register("devourer_roll", () -> DevourerRollFunction.MAP_CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<PillagersFavourRollFunction>> PILLAGERS_FAVOUR_ROLL =
            LOOT_FUNCTIONS.register("pillagers_favour_roll", () -> PillagersFavourRollFunction.MAP_CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<VillagersGambleRollFunction>> VILLAGERS_GAMBLE_ROLL =
            LOOT_FUNCTIONS.register("villagers_gamble_roll", () -> VillagersGambleRollFunction.MAP_CODEC);
}
