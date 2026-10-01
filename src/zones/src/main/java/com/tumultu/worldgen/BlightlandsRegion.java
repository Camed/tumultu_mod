package com.tumultu.worldgen;

import com.mojang.datafixers.util.Pair;
import com.tumultu.TumultuMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

// todo: work on actual blightlands biome, as it is really empty now
public class BlightlandsRegion extends Region {
    public static final ResourceKey<Biome> BLIGHTLANDS =
            ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "blightlands"));

    private static final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
    private static final Climate.Parameter LAND_CONTINENTALNESS = Climate.Parameter.span(0.05F, 1.0F);
    private static final Climate.Parameter WEIRDNESS_BAND_LOW = Climate.Parameter.span(-0.45F, -0.3F);
    private static final Climate.Parameter WEIRDNESS_BAND_HIGH = Climate.Parameter.span(0.3F, 0.45F);
    private static final Climate.Parameter DEPTH_SURFACE = Climate.Parameter.point(0.0F);
    private static final Climate.Parameter DEPTH_UNDERGROUND = Climate.Parameter.point(1.0F);

    public BlightlandsRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        new OverworldBiomeBuilder().addBiomes(mapper);

        for (Climate.Parameter weirdness : new Climate.Parameter[]{WEIRDNESS_BAND_LOW, WEIRDNESS_BAND_HIGH}) {
            for (Climate.Parameter depth : new Climate.Parameter[]{DEPTH_SURFACE, DEPTH_UNDERGROUND}) {
                this.addBiome(mapper, FULL_RANGE, FULL_RANGE, LAND_CONTINENTALNESS, FULL_RANGE, weirdness, depth, 0.0F, BLIGHTLANDS);
            }
        }
    }
}
