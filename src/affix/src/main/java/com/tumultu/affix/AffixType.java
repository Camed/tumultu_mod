package com.tumultu.affix;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum AffixType implements StringRepresentable {
    PREFIX("prefix"),
    SUFFIX("suffix");

    public static final Codec<AffixType> CODEC = StringRepresentable.fromEnum(AffixType::values);
    public static final StreamCodec<ByteBuf, AffixType> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> values()[id], AffixType::ordinal
    );

    private final String name;

    AffixType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() { return name; }
}