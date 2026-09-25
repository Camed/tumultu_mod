package com.tumultu.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tumultu.affix.effect.AffixEffect;
import net.minecraft.resources.Identifier;

import java.util.List;

public record AffixDefinition(
        AffixType type,
        String displayName,
        AffixEffect effect,
        List<AffixTier> tiers,
        int weight,
        String group,
        List<Identifier> applicableTags,
        String craftCategory
) {
    public static final Codec<AffixDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AffixType.CODEC.fieldOf("type").forGetter(AffixDefinition::type),
                    Codec.STRING.fieldOf("display_name").forGetter(AffixDefinition::displayName),
                    AffixEffect.CODEC.fieldOf("effect").forGetter(AffixDefinition::effect),
                    AffixTier.CODEC.listOf().fieldOf("tiers").forGetter(AffixDefinition::tiers),
                    Codec.INT.optionalFieldOf("weight", 100).forGetter(AffixDefinition::weight),
                    Codec.STRING.optionalFieldOf("group", "").forGetter(AffixDefinition::group),
                    Identifier.CODEC.listOf().optionalFieldOf("applicable_tags", List.of()).forGetter(AffixDefinition::applicableTags),
                    Codec.STRING.optionalFieldOf("craft_category", "").forGetter(AffixDefinition::craftCategory)
            ).apply(instance, AffixDefinition::new)
    );

    /** Pre-existing call sites (tests, debug commands) that don't care about bench-crafting categorization. */
    public AffixDefinition(AffixType type, String displayName, AffixEffect effect, List<AffixTier> tiers, int weight, String group, List<Identifier> applicableTags) {
        this(type, displayName, effect, tiers, weight, group, applicableTags, "");
    }
}
