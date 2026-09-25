package com.tumultu.registry;

import com.tumultu.bench.CraftingBenchBlock;
import com.tumultu.stash.ShardStashBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

// register crafting blocks
public class CraftingBlocks {
    public static final DeferredBlock<CraftingBenchBlock> CRAFTING_BENCH = TumultuBlocksRegistry.BLOCKS.register(
            "crafting_bench",
            registryName -> new CraftingBenchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(3.5F)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> CRAFTING_BENCH_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(CRAFTING_BENCH);

    public static final DeferredBlock<ShardStashBlock> SHARD_STASH = TumultuBlocksRegistry.BLOCKS.register(
            "shard_stash",
            registryName -> new ShardStashBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(3.0F)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHARD_STASH_ITEM =
            TumultuItemsRegistry.ITEMS.registerSimpleBlockItem(SHARD_STASH);
}
