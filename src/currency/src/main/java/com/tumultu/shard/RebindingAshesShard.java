package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.AffixRoller;
import com.tumultu.affix.AffixTier;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.ElementalResistanceEffect;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
public class RebindingAshesShard extends AbstractShard {

    public RebindingAshesShard(Properties properties) {
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
        if (data.affixes().isEmpty()) return false;

        var registryAccess = level.registryAccess();
        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        var random = level.getRandom();

        List<RolledAffix> swappable = new ArrayList<>();
        for (RolledAffix rolled : data.affixes()) {
            if (data.craftedAffixId().isPresent() && data.craftedAffixId().get().equals(rolled.affixId())) continue;
            if (data.imbuedAffixId().isPresent() && data.imbuedAffixId().get().equals(rolled.affixId())) continue;
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null && def.effect() instanceof ElementalResistanceEffect) {
                swappable.add(rolled);
            }
        }
        if (swappable.isEmpty()) return false;

        RolledAffix chosen = swappable.get(random.nextInt(swappable.size()));

        List<AffixDefinition> alternatives = new ArrayList<>();
        List<Identifier> alternativeIds = new ArrayList<>();
        for (var entry : registry.entrySet()) {
            AffixDefinition def = entry.getValue();
            Identifier id = entry.getKey().identifier();
            if (id.equals(chosen.affixId())) continue;
            if (!(def.effect() instanceof ElementalResistanceEffect)) continue;
            if (!AffixRoller.isApplicable(def, target)) continue;
            if (!AffixRoller.hasEligibleTiers(def, target)) continue;
            alternatives.add(def);
            alternativeIds.add(id);
        }
        if (alternatives.isEmpty()) return false;

        int pick = random.nextInt(alternatives.size());
        AffixDefinition newDef = alternatives.get(pick);
        Identifier newId = alternativeIds.get(pick);

        int tierIndex = Math.min(chosen.tierIndex(), newDef.tiers().size() - 1);
        AffixTier tier = newDef.tiers().get(tierIndex);
        double newValue = tier.roll(random);

        List<RolledAffix> newAffixes = new ArrayList<>(data.affixes());
        newAffixes.set(newAffixes.indexOf(chosen), new RolledAffix(newId, tierIndex, newValue));

        setAffixData(target, data.withAffixes(List.copyOf(newAffixes)), registryAccess, random);
        return true;
    }
}
