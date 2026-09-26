package com.tumultu.registry;

import com.tumultu.TumultuMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlightlandsEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TumultuMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> BLIGHTED_BOAT = ENTITY_TYPES.register(
            "blighted_boat",
            registryName -> EntityType.Builder.of(
                            (EntityType<Boat> type, Level level) -> new Boat(type, level, () -> BlightlandsBlocks.BLIGHTED_BOAT_ITEM.get()), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ChestBoat>> BLIGHTED_CHEST_BOAT = ENTITY_TYPES.register(
            "blighted_chest_boat",
            registryName -> EntityType.Builder.of(
                            (EntityType<ChestBoat> type, Level level) -> new ChestBoat(type, level, () -> BlightlandsBlocks.BLIGHTED_CHEST_BOAT_ITEM.get()), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
    );
}
