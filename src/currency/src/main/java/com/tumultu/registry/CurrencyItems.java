package com.tumultu.registry;

import com.tumultu.shard.CleansingShard;
import com.tumultu.shard.DivineShard;
import com.tumultu.shard.EmpoweringGemItem;
import com.tumultu.shard.EndfusingShard;
import com.tumultu.shard.GrowthShard;
import com.tumultu.shard.HeighteningShard;
import com.tumultu.shard.ImbuingShard;
import com.tumultu.shard.InfusionShard;
import com.tumultu.shard.MysteriousBookpageItem;
import com.tumultu.shard.PeakingShard;
import com.tumultu.shard.RebindingAshesShard;
import com.tumultu.shard.SeveringShard;
import com.tumultu.shard.ShapingShard;
import com.tumultu.shard.TwistingShard;
import com.tumultu.shard.WeavingShard;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

// currency items registry
public class CurrencyItems {
    //transmute/shape - make normal item magic item
    public static final Supplier<Item> SHAPING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "shaping_shard",
            registryName -> new ShapingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //aug/grow - add new random affix to magic item with one affix
    public static final Supplier<Item> GROWTH_SHARD = TumultuItemsRegistry.ITEMS.register(
            "growth_shard",
            registryName -> new GrowthShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //regal/height - upgrade magic to rare with one new random affix
    public static final Supplier<Item> HEIGHTENING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "heightening_shard",
            registryName -> new HeighteningShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );


    //alch/infuse - upgrade normal item to rare with 4 random affixes
    public static final Supplier<Item> INFUSION_SHARD = TumultuItemsRegistry.ITEMS.register(
            "infusion_shard",
            registryName -> new InfusionShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //scour/cleanse - remove all affixes, make item normal again
    public static final Supplier<Item> CLEANSING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "cleansing_shard",
            registryName -> new CleansingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //chaos/twist - reroll all affixes on a rare item
    public static final Supplier<Item> TWISTING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "twisting_shard",
            registryName -> new TwistingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //exalt/peak - add new affix to rare item
    public static final Supplier<Item> PEAKING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "peaking_shard",
            registryName -> new PeakingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //divine/weave - reroll numeric values within affixes of the same tier
    public static final Supplier<Item> WEAVING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "weaving_shard",
            registryName -> new WeavingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //annul/sever - remove one random affix from the item
    public static final Supplier<Item> SEVERING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "severing_shard",
            registryName -> new SeveringShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //vaal/corrupt/endfuse - modify item unpredictably and lock it
    public static final Supplier<Item> ENDFUSING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "endfusing_shard",
            registryName -> new EndfusingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    // Registered so they exist in-world and can drop from loot tables. None of these five
    // have crafting-use behavior wired up yet (plain Item, no AbstractShard subclass) -
    // each described mechanic (preview-lock, resistance swap, enchant upgrade, permanent
    // affix lock, item duplication) is its own separate implementation task.

    //hinekora's lock equivalent - preview a crafting outcome before committing to it
    public static final Supplier<Item> UNSPOKEN_PROMISE = TumultuItemsRegistry.ITEMS.register(
            "unspoken_promise",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(16))
    );

    //flux/horticrafting ele-swap equivalent - randomly swaps one resistance type for another
    public static final Supplier<Item> REBINDING_ASHES = TumultuItemsRegistry.ITEMS.register(
            "rebinding_ashes",
            registryName -> new RebindingAshesShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //upgrades a random enchantment on the item by 1-3 levels, higher levels extremely rare
    public static final Supplier<Item> EMPOWERING_GEM = TumultuItemsRegistry.ITEMS.register(
            "empowering_gem",
            registryName -> new EmpoweringGemItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //fracturing orb equivalent - permanently locks one affix from all further crafting
    public static final Supplier<Item> IMBUING_SHARD = TumultuItemsRegistry.ITEMS.register(
            "imbuing_shard",
            registryName -> new ImbuingShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32))
    );

    //mirror of kalandra equivalent - extreme rarity
    public static final Supplier<Item> MYSTERIOUS_BOOKPAGE = TumultuItemsRegistry.ITEMS.register(
            "mysterious_bookpage",
            registryName -> new MysteriousBookpageItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    // Forced enchantment-glint, same mechanism vanilla uses for the enchanted
                    // golden apple - a permanent shine with no actual enchantment involved.
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );

    //divine orb equivalent, scoped to Uniques - WeavingShard already fills this role for
    //Normal/Magic/Rare items, but Uniques are locked out of isModifiable() entirely
    public static final Supplier<Item> DIVINE_SHARD = TumultuItemsRegistry.ITEMS.register(
            "divine_shard",
            registryName -> new DivineShard(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(32)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );

    // forces this class's static initializer to run before {@code ITEMS.register(modBus)} does
    public static void touch() {
    }
}
