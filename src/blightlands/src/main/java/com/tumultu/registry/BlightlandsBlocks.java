package com.tumultu.registry;

import com.tumultu.blightidol.BlightIdolBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

// blocks associated with blightlands biome
public class BlightlandsBlocks {
    // No BlockItem: worldgen-only prop, never obtainable or player-placeable (see BlightIdolBlock).
    public static final DeferredBlock<BlightIdolBlock> BLIGHT_IDOL = TumultuBlocksRegistry.BLOCKS.register(
            "blight_idol",
            registryName -> new BlightIdolBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(5.0F)
                    .requiresCorrectToolForDrops()
                    .noLootTable())
    );
}
