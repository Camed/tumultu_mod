package com.tumultu.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tumultu.zones.DropChanceScalingReloadListener;
import com.tumultu.zones.WorldTier;
import com.tumultu.zones.ZoneBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public record WorldTierChanceCondition(float baseChance) implements LootItemCondition {
    public static final MapCodec<WorldTierChanceCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("base_chance").forGetter(WorldTierChanceCondition::baseChance)
            ).apply(instance, WorldTierChanceCondition::new));

    @Override
    public MapCodec<WorldTierChanceCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext context) {
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        double multiplier = 1.0;
        if (origin != null) {
            BlockPos pos = BlockPos.containing(origin);
            int worldTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
            Holder<Biome> biome = context.getLevel().getBiome(pos);
            int effectiveTier = Math.min(worldTier + ZoneBiomeTags.constantFor(biome), WorldTier.MAX);
            multiplier = DropChanceScalingReloadListener.current().multiplierFor(effectiveTier);
        }
        double chance = Math.min(1.0, baseChance * multiplier);
        return context.getRandom().nextDouble() < chance;
    }
}
