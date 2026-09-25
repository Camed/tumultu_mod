    package com.tumultu.affix;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ItemRarity implements StringRepresentable {
    NORMAL("normal", 0, 0, ChatFormatting.WHITE),
    MAGIC("magic", 1, 1, ChatFormatting.BLUE),
    RARE("rare", 3, 3, ChatFormatting.YELLOW),
    ENDFUSED("endfused", 4, 4, ChatFormatting.RED),
    UNIQUE("unique", 0, 0, ChatFormatting.GOLD);

    public static final Codec<ItemRarity> CODEC = StringRepresentable.fromEnum(ItemRarity::values);
    public static final StreamCodec<ByteBuf, ItemRarity> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> values()[id], ItemRarity::ordinal
    );

    private final String name;
    private final int maxPrefixes;
    private final int maxSuffixes;
    private final ChatFormatting color;

    ItemRarity(String name, int maxPrefixes, int maxSuffixes, ChatFormatting color) {
        this.name = name;
        this.maxPrefixes = maxPrefixes;
        this.maxSuffixes = maxSuffixes;
        this.color = color;
    }

    public int maxPrefixes() { return maxPrefixes; }
    public int maxSuffixes() { return maxSuffixes; }
    public int maxTotal() { return maxPrefixes + maxSuffixes; }
    public ChatFormatting color() { return color; }

    @Override
    public String getSerializedName() { return name; }
}