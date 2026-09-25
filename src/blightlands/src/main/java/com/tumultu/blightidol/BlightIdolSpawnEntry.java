package com.tumultu.blightidol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.util.List;

// who/what is spawned after breaking BlightIdol block
public record BlightIdolSpawnEntry(EntityType<?> type, int count) {
    public static final Codec<BlightIdolSpawnEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(BlightIdolSpawnEntry::type),
            Codec.INT.fieldOf("count").forGetter(BlightIdolSpawnEntry::count)
    ).apply(instance, BlightIdolSpawnEntry::new));

    public static final Codec<List<BlightIdolSpawnEntry>> LIST_CODEC = CODEC.listOf();
}
