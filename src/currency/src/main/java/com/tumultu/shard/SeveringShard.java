package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SeveringShard extends AbstractShard {

    public SeveringShard(Properties properties) {
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
        if (data.rarity() == ItemRarity.NORMAL) return false;
        if (!isModifiable(data)) return false;
        if (data.affixes().isEmpty()) return false;

        // A bench-crafted affix is protected from random removal - it took more investment
        // (a recipe item plus a payment ingot) than a normal roll. An imbued affix is
        // permanently protected from all crafting, including this.
        List<RolledAffix> removable = new ArrayList<>(data.affixes());
        data.craftedAffixId().ifPresent(craftedId -> removable.removeIf(rolled -> rolled.affixId().equals(craftedId)));
        data.imbuedAffixId().ifPresent(imbuedId -> removable.removeIf(rolled -> rolled.affixId().equals(imbuedId)));
        if (removable.isEmpty()) return false;

        var random = level.getRandom();
        RolledAffix toRemove = removable.get(random.nextInt(removable.size()));
        List<RolledAffix> remaining = new ArrayList<>(data.affixes());
        remaining.remove(toRemove);

        AffixData newData;
        if (remaining.isEmpty()) {
            newData = AffixData.EMPTY;
        } else {
            newData = data.withAffixes(List.copyOf(remaining));
        }

        setAffixData(target, newData, level.registryAccess(), random);
        return true;
    }
}
