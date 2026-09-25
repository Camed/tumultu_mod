package com.tumultu.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.function.Supplier;

public class TumultuItemDefinitions {
    public static final Supplier<Item> RING = TumultuItemsRegistry.ITEMS.register(
            "ring",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1))
    );

    public static final Supplier<Item> AMULET = TumultuItemsRegistry.ITEMS.register(
            "amulet",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1))
    );
    public static final Supplier<Item> BLIGHTFANG_SPAWN_EGG = spawnEgg("blightfang_spawn_egg", TumultuEntityTypes.BLIGHTFANG);
    public static final Supplier<Item> BLIGHTFANG_ALPHA_SPAWN_EGG = spawnEgg("blightfang_alpha_spawn_egg", TumultuEntityTypes.BLIGHTFANG_ALPHA);
    public static final Supplier<Item> BLIGHTLORD_SPAWN_EGG = spawnEgg("blightlord_spawn_egg", TumultuEntityTypes.BLIGHTLORD);
    public static final Supplier<Item> BLIGHTBONE_SPAWN_EGG = spawnEgg("blightbone_spawn_egg", TumultuEntityTypes.BLIGHTBONE);
    public static void touch() {
    }

    private static Supplier<Item> spawnEgg(String name, Supplier<? extends EntityType<?>> type) {
        return TumultuItemsRegistry.ITEMS.register(
                name,
                registryName -> new SpawnEggItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName))
                        .stacksTo(64)
                        .component(DataComponents.ENTITY_DATA, TypedEntityData.<EntityType<?>>of(type.get(), new CompoundTag())))
        );
    }
}
