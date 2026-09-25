package com.tumultu.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TumultuTags {

    public static final TagKey<Item> AFFIXABLE = ItemTags.create(
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "affixable")
    );

    /** Marks an item as a Curios accessory whose attribute-type affixes should also be projected
     * into Curios' own {@code CuriosDataComponents.ATTRIBUTE_MODIFIERS} component (see
     * {@link com.tumultu.affix.AffixEffectApplier}) - vanilla's own attribute-modifiers
     * component only applies in a real equipment slot or the player's hand, neither of which a
     * Curios-slotted item occupies. */
    public static final TagKey<Item> CURIOS = ItemTags.create(
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "curios")
    );
}
