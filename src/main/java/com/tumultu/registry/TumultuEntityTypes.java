package com.tumultu.registry;

import com.tumultu.Tumultu;
import com.tumultu.mobs.entity.BlightlordEntity;
import com.tumultu.mobs.entity.TumultuBruteMonster;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TumultuEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Tumultu.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TumultuBruteMonster>> BLIGHTFANG = ENTITY_TYPES.register(
            "blightfang",
            registryName -> EntityType.Builder.of(TumultuBruteMonster::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<TumultuBruteMonster>> BLIGHTFANG_ALPHA = ENTITY_TYPES.register(
            "blightfang_alpha",
            registryName -> EntityType.Builder.of(TumultuBruteMonster::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BlightlordEntity>> BLIGHTLORD = ENTITY_TYPES.register(
            "blightlord",
            registryName -> EntityType.Builder.of(BlightlordEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 2.8F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<TumultuBruteMonster>> BLIGHTBONE = ENTITY_TYPES.register(
            "blightbone",
            registryName -> EntityType.Builder.of(TumultuBruteMonster::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.0F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
    );

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(BLIGHTFANG.get(), TumultuBruteMonster.createAttributes().build());
        event.put(BLIGHTFANG_ALPHA.get(), TumultuBruteMonster.createAttributes().build());
        event.put(BLIGHTLORD.get(), TumultuBruteMonster.createAttributes().build());
        event.put(BLIGHTBONE.get(), TumultuBruteMonster.createAttributes().build());
    }

    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                BLIGHTFANG.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                BLIGHTBONE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR
        );
    }
}
