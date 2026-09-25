package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.RolledAffix;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

// Permanently locks one random existing affix from all future crafting
public class ImbuingShard extends AbstractShard {

    private static final int MIN_AFFIXES_REQUIRED = 4;

    public ImbuingShard(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack target = player.getOffhandItem();
        if (target.isEmpty()) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!tryApply((ServerLevel) level, player, target)) return InteractionResult.PASS;
        player.getMainHandItem().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean tryApply(ServerLevel level, Player player, ItemStack target) {
        if (!isAffixable(target)) return false;

        AffixData data = getAffixData(target);
        if (!isModifiable(data)) return false;
        if (data.affixes().size() < MIN_AFFIXES_REQUIRED) return false;
        if (data.imbuedAffixId().isPresent()) return false;

        var random = level.getRandom();
        RolledAffix chosen = data.affixes().get(random.nextInt(data.affixes().size()));
        AffixData newData = data.withImbuedAffixId(Optional.of(chosen.affixId()));

        setAffixData(target, newData, level.registryAccess(), random);
        return true;
    }
}
