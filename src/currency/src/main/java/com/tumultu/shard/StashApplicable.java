package com.tumultu.shard;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface StashApplicable {
    boolean tryApply(ServerLevel level, Player player, ItemStack target);
}
