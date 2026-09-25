package com.tumultu.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


// Server-to-client only. The one place in the whole mod that needs custom networking - the
// crafting bench's Unspoken Promise preview rolls a real, random tier/value server-side, and
// unlike everything else in the bench (which both sides can independently and deterministically
// recompute from data they already share), a random roll can't be recomputed client-side, so the
// result has to be sent over.
public record PreviewResultPayload(Identifier affixId, int tier, double value) implements CustomPacketPayload {
    // constructing the Type directly with an explicit Identifier is the only way
    // I found to register it under this mod's own namespace.
    public static final Type<PreviewResultPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("tumultu", "preview_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PreviewResultPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, PreviewResultPayload::affixId,
            ByteBufCodecs.INT, PreviewResultPayload::tier,
            ByteBufCodecs.DOUBLE, PreviewResultPayload::value,
            PreviewResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
