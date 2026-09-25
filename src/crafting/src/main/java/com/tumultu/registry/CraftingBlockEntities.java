package com.tumultu.registry;

import com.tumultu.bench.CraftingBenchBlockEntity;
import com.tumultu.stash.ShardStashBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

// crafting bench and shard stash entities
public class CraftingBlockEntities {
    public static final Supplier<BlockEntityType<CraftingBenchBlockEntity>> CRAFTING_BENCH = TumultuBlockEntitiesRegistry.BLOCK_ENTITIES.register(
            "crafting_bench",
            () -> new BlockEntityType<>(CraftingBenchBlockEntity::new, CraftingBlocks.CRAFTING_BENCH.get())
    );

    public static final Supplier<BlockEntityType<ShardStashBlockEntity>> SHARD_STASH = TumultuBlockEntitiesRegistry.BLOCK_ENTITIES.register(
            "shard_stash",
            () -> new BlockEntityType<>(ShardStashBlockEntity::new, CraftingBlocks.SHARD_STASH.get())
    );
    public static void touch() {
    }
}
