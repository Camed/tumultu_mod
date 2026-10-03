package com.tumultu.registry;

import com.tumultu.TumultuMod;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class BlightlandsWoodTypes {
    public static final String NAME = "tumultu_blighted";

    public static final BlockSetType BLIGHTED_SET = BlockSetType.register(new BlockSetType(NAME));
    public static final WoodType BLIGHTED = WoodType.register(new WoodType(TumultuMod.MOD_ID + ":" + NAME, BLIGHTED_SET));
}
