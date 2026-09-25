package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixRoller;
import com.tumultu.affix.ItemRarity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TwistingShard extends AbstractShard {

    public TwistingShard(Properties properties) {
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
        if (data.rarity() != ItemRarity.RARE) return false;
        if (!isModifiable(data)) return false;

        var registryAccess = level.registryAccess();
        AffixData newData = AffixRoller.reroll(
                registryAccess,
                target,
                data,
                level.getRandom()
        );

        setAffixData(target, newData, registryAccess, level.getRandom());
        return true;
    }
}
