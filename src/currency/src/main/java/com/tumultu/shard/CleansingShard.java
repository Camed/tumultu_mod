package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class CleansingShard extends AbstractShard {

    public CleansingShard(Properties properties) {
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

        // An imbued affix is permanently protected - cleansing can't wipe it like everything
        // else, so instead of refusing outright, it reduces the item to Magic with just that one
        // affix, the closest a normal cleanse can get.
        AffixData newData = AffixData.EMPTY;
        Optional<Identifier> imbuedAffixId = data.imbuedAffixId();
        if (imbuedAffixId.isPresent()) {
            RolledAffix imbuedAffix = data.affixes().stream()
                    .filter(rolled -> rolled.affixId().equals(imbuedAffixId.get()))
                    .findFirst()
                    .orElse(null);
            if (imbuedAffix != null) {
                Optional<Identifier> keptCraftedAffixId = data.craftedAffixId().filter(imbuedAffixId.get()::equals);
                newData = new AffixData(ItemRarity.MAGIC, List.of(imbuedAffix), false, keptCraftedAffixId, imbuedAffixId);
            }
        }

        setAffixData(target, newData, level.registryAccess(), level.getRandom());
        return true;
    }
}
