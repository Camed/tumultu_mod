package com.tumultu.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record AffixData(
        ItemRarity rarity,
        List<RolledAffix> affixes,
        boolean endfused,
        Optional<Identifier> craftedAffixId,
        Optional<Identifier> imbuedAffixId
) {
    public static final AffixData EMPTY =
            new AffixData(ItemRarity.NORMAL, List.of(), false, Optional.empty(), Optional.empty());

    /** Pre-existing call sites (tests, shards) that don't touch bench-crafting or imbuing. */
    public AffixData(ItemRarity rarity, List<RolledAffix> affixes, boolean endfused) {
        this(rarity, affixes, endfused, Optional.empty(), Optional.empty());
    }

    /** Pre-existing call sites that touch bench-crafting but predate imbuing. */
    public AffixData(ItemRarity rarity, List<RolledAffix> affixes, boolean endfused, Optional<Identifier> craftedAffixId) {
        this(rarity, affixes, endfused, craftedAffixId, Optional.empty());
    }

    public static final Codec<AffixData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemRarity.CODEC.fieldOf("rarity").forGetter(AffixData::rarity),
                    RolledAffix.CODEC.listOf().optionalFieldOf("affixes", List.of()).forGetter(AffixData::affixes),
                    Codec.BOOL.optionalFieldOf("endfused", false).forGetter(AffixData::endfused),
                    Identifier.CODEC.optionalFieldOf("crafted_affix_id").forGetter(AffixData::craftedAffixId),
                    Identifier.CODEC.optionalFieldOf("imbued_affix_id").forGetter(AffixData::imbuedAffixId)
            ).apply(instance, AffixData::new)
    );
    public static final StreamCodec<ByteBuf, AffixData> STREAM_CODEC = StreamCodec.composite(
            ItemRarity.STREAM_CODEC, AffixData::rarity,
            RolledAffix.STREAM_CODEC.apply(ByteBufCodecs.list()), AffixData::affixes,
            ByteBufCodecs.BOOL, AffixData::endfused,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), AffixData::craftedAffixId,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), AffixData::imbuedAffixId,
            AffixData::new
    );

    // helpers - all preserve craftedAffixId/imbuedAffixId unless explicitly changed via their
    // withX method, so e.g. adding a random affix with a shard doesn't silently forget a
    // bench-crafted or imbued one.
    public AffixData withAffixes(List<RolledAffix> newAffixes) {
        return new AffixData(rarity, newAffixes, endfused, craftedAffixId, imbuedAffixId);
    }

    public AffixData withRarity(ItemRarity newRarity) {
        return new AffixData(newRarity, affixes, endfused, craftedAffixId, imbuedAffixId);
    }
    public AffixData asEndfused() {
        return new AffixData(ItemRarity.ENDFUSED, affixes, true, craftedAffixId, imbuedAffixId);
    }

    public AffixData withCraftedAffixId(Optional<Identifier> newCraftedAffixId) {
        return new AffixData(rarity, affixes, endfused, newCraftedAffixId, imbuedAffixId);
    }

    public AffixData withImbuedAffixId(Optional<Identifier> newImbuedAffixId) {
        return new AffixData(rarity, affixes, endfused, craftedAffixId, newImbuedAffixId);
    }

    // Endfused are locked by design. Unique items have unique mods, so they are only rollable through other custom func.
    public boolean isModifiable() {
        return !endfused && rarity != ItemRarity.UNIQUE;
    }

    public int countByType(AffixType type, java.util.function.Function<RolledAffix, AffixType> typeLookup) {
        return (int) affixes.stream()
                .filter(a -> typeLookup.apply(a) == type)
                .count();
    }
}