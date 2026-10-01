package com.tumultu.registry;

import com.tumultu.blightidol.BlightIdolBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

// block entities connected with blightlands biome
public class BlightlandsBlockEntities {
    public static final Supplier<BlockEntityType<BlightIdolBlockEntity>> BLIGHT_IDOL = TumultuBlockEntitiesRegistry.BLOCK_ENTITIES.register(
            "blight_idol",
            () -> new BlockEntityType<>(BlightIdolBlockEntity::new, BlightlandsBlocks.BLIGHT_IDOL.get())
    );

    public static void touch() {
    }
}
