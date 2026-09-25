package com.tumultu.shard;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.AffixTier;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.registry.TumultuUniqueItems;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DivineShard extends AbstractShard {

    public DivineShard(Properties properties) {
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
        AffixData data = target.get(TumultuDataComponents.AFFIX_DATA.get());
        if (data == null || data.rarity() != ItemRarity.UNIQUE) return false;

        Registry<AffixDefinition> registry = level.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        RandomSource random = player.getRandom();

        boolean rerolledAny = false;
        List<RolledAffix> rerolled = new ArrayList<>();
        for (RolledAffix affix : data.affixes()) {
            AffixDefinition def = registry.getValue(affix.affixId());
            AffixTier tier = (def != null && affix.tierIndex() < def.tiers().size()) ? def.tiers().get(affix.tierIndex()) : null;
            if (tier != null && tier.minValue() != tier.maxValue()) {
                rerolled.add(new RolledAffix(affix.affixId(), affix.tierIndex(), tier.roll(random)));
                rerolledAny = true;
            } else {
                rerolled.add(affix);
            }
        }
        if (!rerolledAny) return false;

        AffixData newData = data.withAffixes(List.copyOf(rerolled));
        target.set(TumultuDataComponents.AFFIX_DATA.get(), newData);
        resyncAttributeModifiers(target, newData);
        return true;
    }

    /** The handful of Uniques whose rerollable stat also drives a real (Curios) attribute
     * modifier need that modifier rebuilt to match - see {@code TumultuUniqueItems}'s own loot
     * functions, which do the exact same resync at generation time for the exact same reason. */
    private static void resyncAttributeModifiers(ItemStack target, AffixData data) {
        if (target.getItem() == TumultuUniqueItems.DEVOURER.get()) {
            double lifeToDamage = TumultuUniqueItems.valueOf(data, TumultuUniqueItems.DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID);
            TumultuUniqueItems.updateCurioAttributeModifier(target, Attributes.MAX_HEALTH, new AttributeModifier(
                    TumultuUniqueItems.DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID, -lifeToDamage, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (target.getItem() == TumultuUniqueItems.PILLAGERS_FAVOUR.get()) {
            double luck = TumultuUniqueItems.valueOf(data, TumultuUniqueItems.PILLAGERS_FAVOUR_LUCK_AFFIX_ID);
            TumultuUniqueItems.updateCurioAttributeModifier(target, Attributes.LUCK, new AttributeModifier(
                    TumultuUniqueItems.PILLAGERS_FAVOUR_LUCK_AFFIX_ID, luck, AttributeModifier.Operation.ADD_VALUE));
        } else if (target.getItem() == TumultuUniqueItems.VILLAGERS_GAMBLE.get()) {
            double maxHealth = TumultuUniqueItems.valueOf(data, TumultuUniqueItems.VILLAGERS_GAMBLE_MAX_HEALTH_AFFIX_ID);
            TumultuUniqueItems.updateCurioAttributeModifier(target, Attributes.MAX_HEALTH, new AttributeModifier(
                    TumultuUniqueItems.VILLAGERS_GAMBLE_MAX_HEALTH_AFFIX_ID, maxHealth, AttributeModifier.Operation.ADD_VALUE));
            double luck = TumultuUniqueItems.valueOf(data, TumultuUniqueItems.VILLAGERS_GAMBLE_LUCK_AFFIX_ID);
            TumultuUniqueItems.updateCurioAttributeModifier(target, Attributes.LUCK, new AttributeModifier(
                    TumultuUniqueItems.VILLAGERS_GAMBLE_LUCK_AFFIX_ID, luck, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
