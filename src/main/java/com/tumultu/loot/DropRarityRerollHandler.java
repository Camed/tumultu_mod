package com.tumultu.loot;

import com.tumultu.Tumultu;
import com.tumultu.zones.DropRarityWeightsReloadListener;
import com.tumultu.zones.WorldTier;
import com.tumultu.zones.ZoneBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DropRarityRerollHandler {
    private static final TagKey<Item>[] BANDS = bandTags();

    @SuppressWarnings("unchecked")
    private static TagKey<Item>[] bandTags() {
        TagKey<Item>[] tags = new TagKey[4];
        for (int i = 0; i < tags.length; i++) {
            tags[i] = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Tumultu.MOD_ID, "drop_quality_" + i));
        }
        return tags;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        RandomSource random = entity.getRandom();

        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (bandOf(stack) < 0) {
                continue;
            }

            BlockPos pos = entity.blockPosition();
            int worldTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
            Holder<Biome> biome = entity.level().getBiome(pos);
            int effectiveTier = Math.min(worldTier + ZoneBiomeTags.constantFor(biome), WorldTier.MAX);
            List<Double> weights = DropRarityWeightsReloadListener.current().weightsFor(effectiveTier);

            int newBand = rollBand(random, weights);
            randomItemInBand(newBand, random).ifPresent(item -> drop.setItem(new ItemStack(item, stack.getCount())));
        }
    }

    private static int bandOf(ItemStack stack) {
        for (int i = BANDS.length - 1; i >= 0; i--) {
            if (stack.is(BANDS[i])) {
                return i;
            }
        }
        return -1;
    }

    private static int rollBand(RandomSource random, List<Double> weights) {
        double total = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) {
            return 0;
        }
        double roll = random.nextDouble() * total;
        double cumulative = 0;
        for (int i = 0; i < weights.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return i;
            }
        }
        return weights.size() - 1;
    }

    private static Optional<Item> randomItemInBand(int band, RandomSource random) {
        List<Item> candidates = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(BANDS[band])) {
            candidates.add(holder.value());
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }
}
