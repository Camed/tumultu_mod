package com.tumultu.blightidol;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

// blightlands specific block - it summons biome boss encounter when broken. Unobtainable.
public class BlightIdolBlock extends Block implements EntityBlock {
    public BlightIdolBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlightIdolBlockEntity(pos, state);
    }
}
