package com.tumultu.registry;

import com.mojang.serialization.MapCodec;
import com.tumultu.Tumultu;
import com.tumultu.loot.WorldTierChanceCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TumultuLootConditions {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Tumultu.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<WorldTierChanceCondition>> WORLD_TIER_CHANCE =
            LOOT_CONDITIONS.register("world_tier_chance", () -> WorldTierChanceCondition.MAP_CODEC);
}
