package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WeavingShard extends AbstractShard {

    public WeavingShard(Properties properties) {
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

        var registryAccess = level.registryAccess();
        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        var random = level.getRandom();

        List<RolledAffix> rerolled = new ArrayList<>();
        for (RolledAffix affix : data.affixes()) {
            // an imbued affix's value is permanent - leave it untouched.
            boolean isImbued = data.imbuedAffixId().isPresent() && data.imbuedAffixId().get().equals(affix.affixId());
            AffixDefinition def = registry.getValue(affix.affixId());
            if (!isImbued && def != null && affix.tierIndex() < def.tiers().size()) {
                double newValue = def.tiers().get(affix.tierIndex()).roll(random);
                rerolled.add(new RolledAffix(affix.affixId(), affix.tierIndex(), newValue));
            } else {
                rerolled.add(affix);
            }
        }

        setAffixData(target, data.withAffixes(List.copyOf(rerolled)), registryAccess, random);
        return true;
    }
}
