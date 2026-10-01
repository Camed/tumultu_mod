package com.tumultu.blightidol;

import com.tumultu.registry.BlightlandsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;


// spawning boss and his "guards" logic
public class BlightIdolBlockEntity extends BlockEntity {
    private List<BlightIdolSpawnEntry> spawns = List.of();

    public BlightIdolBlockEntity(BlockPos pos, BlockState state) {
        super(BlightlandsBlockEntities.BLIGHT_IDOL.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (BlightIdolSpawnEntry entry : this.spawns) {
            EntityType<?> type = entry.type();
            for (int i = 0; i < entry.count(); i++) {
                BlockPos spawnPos = pos.offset(serverLevel.getRandom().nextInt(5) - 2, 0, serverLevel.getRandom().nextInt(5) - 2);
                type.spawn(serverLevel, spawnPos, EntitySpawnReason.TRIGGERED);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.spawns = input.read("Spawns", BlightIdolSpawnEntry.LIST_CODEC).orElse(List.of());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Spawns", BlightIdolSpawnEntry.LIST_CODEC, this.spawns);
    }
}
