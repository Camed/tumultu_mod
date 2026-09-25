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

import java.util.List;

public class EndfusingShard extends AbstractShard {

    public EndfusingShard(Properties properties) {
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

        // Outcome 3 wipes to zero affixes entirely, which can't coexist with a permanently
        // protected (crafted or imbued) affix - fall back to just locking the item as-is instead.
        boolean hasProtectedAffix = data.craftedAffixId().isPresent() || data.imbuedAffixId().isPresent();

        var random = level.getRandom();
        var registryAccess = level.registryAccess();
        int outcome = random.nextInt(4);

        AffixData newData = switch (outcome) {
            case 0 -> AffixRoller.rerollForEndfusing(registryAccess, target, data, ItemRarity.ENDFUSED, random);
            case 1 -> data.asEndfused();
            case 2 -> AffixRoller.rerollForEndfusing(registryAccess, target, data, data.rarity(), random);
            case 3 -> hasProtectedAffix ? data.asEndfused() : new AffixData(ItemRarity.ENDFUSED, List.of(), true);
            default -> data.asEndfused();
        };

        setAffixData(target, newData, registryAccess, random);
        return true;
    }
}
