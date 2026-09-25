package com.tumultu.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record RolledAffix(
        Identifier affixId,
        int tierIndex,
        double rolledValue
) {
    public static final Codec<RolledAffix> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("affix_id").forGetter(RolledAffix::affixId),
                    Codec.INT.fieldOf("tier").forGetter(RolledAffix::tierIndex),
                    Codec.DOUBLE.fieldOf("value").forGetter(RolledAffix::rolledValue)
            ).apply(instance, RolledAffix::new));

    public static final StreamCodec<ByteBuf, RolledAffix> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, RolledAffix::affixId,
            ByteBufCodecs.INT, RolledAffix::tierIndex,
            ByteBufCodecs.DOUBLE, RolledAffix::rolledValue,
            RolledAffix::new
    );
}
