package com.tumultu.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FlammableBlock extends Block {
    private final int igniteOdds;
    private final int burnOdds;

    public FlammableBlock(Properties properties, int igniteOdds, int burnOdds) {
        super(properties);
        this.igniteOdds = igniteOdds;
        this.burnOdds = burnOdds;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return igniteOdds;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return burnOdds;
    }
}
