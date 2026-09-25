package com.tumultu.loot;

import com.tumultu.Tumultu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class BonusDropHandler {
    private static final TagKey<Item> BONUS_DROP_CURRENCY =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Tumultu.MOD_ID, "bonus_drop_currency"));

    private static final double BASE_CHANCE = 0.5;
    private static final double DECAY_FACTOR = 0.5;
    private static final double LUCK_BONUS_PER_POINT = 0.1;
    private static final int MAX_BONUS_ROLLS = 10;

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        double luck = luckOf(event.getSource());
        RandomSource random = entity.getRandom();

        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (!stack.is(BONUS_DROP_CURRENCY)) {
                continue;
            }

            int bonus = rollBonusCount(random, luck);
            if (bonus > 0) {
                stack.grow(bonus);
                drop.setItem(stack);
            }
        }
    }

    private static double luckOf(DamageSource source) {
        Entity killer = source.getEntity();
        if (killer instanceof Player player) {
            return player.getAttributeValue(Attributes.LUCK);
        }
        return 0.0;
    }
    static int rollBonusCount(RandomSource random, double luck) {
        double luckMultiplier = 1 + luck * LUCK_BONUS_PER_POINT;
        double chance = BASE_CHANCE;
        int count = 0;
        while (count < MAX_BONUS_ROLLS && random.nextDouble() < Math.min(1.0, chance * luckMultiplier)) {
            count++;
            chance *= DECAY_FACTOR;
        }
        return count;
    }
}
