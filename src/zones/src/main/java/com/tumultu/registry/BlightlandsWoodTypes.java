package com.tumultu.registry;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class BlightlandsWoodTypes {
    public static final String NAME = "tumultu_blighted";

    public static final BlockSetType BLIGHTED_SET = BlockSetType.register(new BlockSetType(NAME));
    public static final WoodType BLIGHTED = WoodType.register(new WoodType(NAME, BLIGHTED_SET));
}
