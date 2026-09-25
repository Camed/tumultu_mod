package com.tumultu.registry;

import com.tumultu.TumultuMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

// Custom block entities registration blob for Tumultu
public class TumultuBlockEntitiesRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TumultuMod.MOD_ID);
}
