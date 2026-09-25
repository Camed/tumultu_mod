package com.tumultu.registry;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import com.tumultu.unique.CinderheartItem;
import com.tumultu.unique.ReflectionOfIronItem;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosDataComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// Unique items are "hardcoded", specific items, mostly unmodifiable (if there is a unique item with rolls, there is one specific shard to roll them (divine shard)).
public class TumultuUniqueItems {

    // Cinderheart
    // Uses overcapped fire resistance in order to scale players damage - roughly 50% of overcap as "more" damage.
    private static final Identifier CINDERHEART_FIRE_DAMAGE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/cinderheart_fire_damage");
    private static final Identifier CINDERHEART_FIRE_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/cinderheart_fire_resistance");

    public static final Supplier<Item> CINDERHEART = TumultuItemsRegistry.ITEMS.register(
            "cinderheart",
            registryName -> new CinderheartItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .sword(ToolMaterial.NETHERITE, 3.0f, -2.4f) //netherite base
                    .fireResistant()
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.cinderheart").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of( // unique affixes
                                    new RolledAffix(CINDERHEART_FIRE_DAMAGE_AFFIX_ID, 0, 8.0),
                                    new RolledAffix(CINDERHEART_FIRE_RESISTANCE_AFFIX_ID, 0, 0.3)),
                            false)))
    );


    // devourers affixes
    private static final Identifier DEVOURER_FIRE_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/devourer_fire_resistance");
    private static final Identifier DEVOURER_COLD_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/devourer_cold_resistance");
    private static final Identifier DEVOURER_LIGHTNING_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/devourer_lightning_resistance");
    private static final Identifier DEVOURER_MOVEMENT_SPEED_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/devourer_movement_speed");
    public static final Identifier DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/devourer_life_to_damage");

    public static final Supplier<Item> DEVOURER = TumultuItemsRegistry.ITEMS.register(
            "devourer",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.devourer").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(DEVOURER_FIRE_RESISTANCE_AFFIX_ID, 0, 0.10),
                                    new RolledAffix(DEVOURER_COLD_RESISTANCE_AFFIX_ID, 0, 0.10),
                                    new RolledAffix(DEVOURER_LIGHTNING_RESISTANCE_AFFIX_ID, 0, 0.10),
                                    new RolledAffix(DEVOURER_MOVEMENT_SPEED_AFFIX_ID, 0, 0.10),
                                    new RolledAffix(DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID, 0, 0.20)), // this is hardcoded only for "creation" reasons, like getting this item from commands/creative menu
                            false))
                    .component(CuriosDataComponents.ATTRIBUTE_MODIFIERS, CurioAttributeModifiers.builder()
                            .addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                                    DEVOURER_MOVEMENT_SPEED_AFFIX_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
                            .addModifier(Attributes.MAX_HEALTH, new AttributeModifier(
                                    DEVOURER_LIFE_TO_DAMAGE_AFFIX_ID, -0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                            .build()))
    );

    // these methods below are needed to reroll affixes on curio unique items (like earlier declared devourer)
    public static void updateCurioAttributeModifier(ItemStack stack, Holder<Attribute> attribute, AttributeModifier modifier) {
        CurioAttributeModifiers existing = CuriosDataComponents.getCurioAttributeModifiersOrEmpty(stack);
        CurioAttributeModifiers.Builder builder = CurioAttributeModifiers.builder();
        for (CurioAttributeModifiers.Entry entry : existing.modifiers()) {
            if (!entry.attributeHolder().equals(attribute)) {
                builder.addModifier(entry.attributeHolder(), entry.modifier(), entry.slotType());
            }
        }
        builder.addModifier(attribute, modifier);
        stack.set(CuriosDataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    public static AffixData rerollSingleAffix(AffixData data, Identifier affixId, Registry<AffixDefinition> registry, RandomSource random) {
        AffixDefinition def = registry.getValue(affixId);
        if (def == null || def.tiers().isEmpty()) {
            return data;
        }
        double rolledValue = def.tiers().get(0).roll(random);

        List<RolledAffix> affixes = new ArrayList<>();
        for (RolledAffix rolled : data.affixes()) {
            affixes.add(rolled.affixId().equals(affixId) ? new RolledAffix(affixId, rolled.tierIndex(), rolledValue) : rolled);
        }
        return data.withAffixes(List.copyOf(affixes));
    }

    // the current rolled value of affix on data, or zero if absent
    public static double valueOf(AffixData data, Identifier affixId) {
        for (RolledAffix rolled : data.affixes()) {
            if (rolled.affixId().equals(affixId)) {
                return rolled.rolledValue();
            }
        }
        return 0;
    }


    // possibly gamebreaking unique :D
    // takes as a base a cobblestone axe, applies a netherite durability to it
    // the more armour you stack, the better it gets
    // balance should revolve around the multiplicative modifier and/or applied attack speed
    private static final ToolMaterial REFLECTION_OF_IRON_MATERIAL = new ToolMaterial(
            ToolMaterial.STONE.incorrectBlocksForDrops(),
            ToolMaterial.NETHERITE.durability(),
            ToolMaterial.STONE.speed(),
            ToolMaterial.STONE.attackDamageBonus(),
            ToolMaterial.NETHERITE.enchantmentValue(),
            ToolMaterial.NETHERITE.repairItems()
    );

    public static final Supplier<Item> REFLECTION_OF_IRON = TumultuItemsRegistry.ITEMS.register(
            "reflection_of_iron",
            registryName -> new ReflectionOfIronItem(REFLECTION_OF_IRON_MATERIAL, 7.0f, -3.6f, new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.reflection_of_iron").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(ItemRarity.UNIQUE, List.of(), false)))
    );


    // unique chestplate Dragons Heart
    // greatly increases health
    // greatly reduces resists
    // grants immunity to status effects
    private static final Identifier DRAGONS_HEART_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/dragons_heart_resistance");
    private static final Identifier DRAGONS_HEART_MAX_HEALTH_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/dragons_heart_max_health");
    private static final Identifier DRAGONS_HEART_CANNOT_BE_POISONED_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/dragons_heart_cannot_be_poisoned");
    private static final Identifier DRAGONS_HEART_CANNOT_BE_BLED_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/dragons_heart_cannot_be_bled");
    private static final Identifier DRAGONS_HEART_CANNOT_BE_WITHERED_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/dragons_heart_cannot_be_withered");

    public static final Supplier<Item> DRAGONS_HEART = TumultuItemsRegistry.ITEMS.register(
            "dragons_heart",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.DIAMOND, ArmorType.CHESTPLATE)
                    .attributes(armorAttributesWithMaxHealth(ArmorMaterials.DIAMOND, ArmorType.CHESTPLATE,
                            EquipmentSlotGroup.CHEST, DRAGONS_HEART_MAX_HEALTH_AFFIX_ID, 100.0))
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.dragons_heart").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(DRAGONS_HEART_RESISTANCE_AFFIX_ID, 0, -0.50),
                                    new RolledAffix(DRAGONS_HEART_MAX_HEALTH_AFFIX_ID, 0, 100.0),
                                    new RolledAffix(DRAGONS_HEART_CANNOT_BE_POISONED_AFFIX_ID, 0, 1.0),
                                    new RolledAffix(DRAGONS_HEART_CANNOT_BE_BLED_AFFIX_ID, 0, 1.0),
                                    new RolledAffix(DRAGONS_HEART_CANNOT_BE_WITHERED_AFFIX_ID, 0, 1.0)),
                            false)))
    );

    private static ItemAttributeModifiers armorAttributesWithMaxHealth(
            ArmorMaterial material, ArmorType type, EquipmentSlotGroup slotGroup, Identifier maxHealthAffixId, double maxHealthBonus) {
        ItemAttributeModifiers base = material.createAttributes(type);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        base.modifiers().forEach(entry -> builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display()));
        builder.add(Attributes.MAX_HEALTH,
                new AttributeModifier(maxHealthAffixId, maxHealthBonus, AttributeModifier.Operation.ADD_VALUE),
                slotGroup, ItemAttributeModifiers.Display.hidden());
        return builder.build();
    }

    // public - {@code PillagersFavourRollFunction} and {@code DivineShard} needs them. TODO: Maybe should me moved elsewhere?
    public static final Identifier PILLAGERS_FAVOUR_DAMAGE_PERCENT_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/pillagers_favour_damage_percent");
    public static final Identifier PILLAGERS_FAVOUR_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/pillagers_favour_resistance");
    public static final Identifier PILLAGERS_FAVOUR_LUCK_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/pillagers_favour_luck");


    // Pillagers favour
    // unique ring, inspired by PoE Le Heup of All
    // not the best in slot, but allows fixing some issues sometimes
    public static final Supplier<Item> PILLAGERS_FAVOUR = TumultuItemsRegistry.ITEMS.register(
            "pillagers_favour",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.pillagers_favour").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(PILLAGERS_FAVOUR_DAMAGE_PERCENT_AFFIX_ID, 0, 0.20),
                                    new RolledAffix(PILLAGERS_FAVOUR_RESISTANCE_AFFIX_ID, 0, 0.10),
                                    new RolledAffix(PILLAGERS_FAVOUR_LUCK_AFFIX_ID, 0, 2.0)),
                            false))
                    .component(CuriosDataComponents.ATTRIBUTE_MODIFIERS, CurioAttributeModifiers.builder()
                            .addModifier(Attributes.LUCK, new AttributeModifier(
                                    PILLAGERS_FAVOUR_LUCK_AFFIX_ID, 2.0, AttributeModifier.Operation.ADD_VALUE))
                            .build()))
    );

    // public - Same as above, {@code PillagersFavourRollFunction} and {@code DivineShard} needs them. TODO: Maybe should me moved elsewhere?
    public static final Identifier VILLAGERS_GAMBLE_MAX_HEALTH_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/villagers_gamble_max_health");
    public static final Identifier VILLAGERS_GAMBLE_FIRE_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/villagers_gamble_fire_resistance");
    public static final Identifier VILLAGERS_GAMBLE_COLD_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/villagers_gamble_cold_resistance");
    public static final Identifier VILLAGERS_GAMBLE_LIGHTNING_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/villagers_gamble_lightning_resistance");
    public static final Identifier VILLAGERS_GAMBLE_LUCK_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/villagers_gamble_luck");


    // unique gamble ring - Villagers gamble
    // five unique rolls, can be EXTREMELY good, also EXTREMELY bad item.
    // Divineable.
    public static final Supplier<Item> VILLAGERS_GAMBLE = TumultuItemsRegistry.ITEMS.register(
            "villagers_gamble",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.villagers_gamble").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(VILLAGERS_GAMBLE_MAX_HEALTH_AFFIX_ID, 0, 0.0),
                                    new RolledAffix(VILLAGERS_GAMBLE_FIRE_RESISTANCE_AFFIX_ID, 0, 0.0),
                                    new RolledAffix(VILLAGERS_GAMBLE_COLD_RESISTANCE_AFFIX_ID, 0, 0.0),
                                    new RolledAffix(VILLAGERS_GAMBLE_LIGHTNING_RESISTANCE_AFFIX_ID, 0, 0.0),
                                    new RolledAffix(VILLAGERS_GAMBLE_LUCK_AFFIX_ID, 0, 0.0)),
                            false))
                    .component(CuriosDataComponents.ATTRIBUTE_MODIFIERS, CurioAttributeModifiers.builder()
                            .addModifier(Attributes.MAX_HEALTH, new AttributeModifier(
                                    VILLAGERS_GAMBLE_MAX_HEALTH_AFFIX_ID, 0.0, AttributeModifier.Operation.ADD_VALUE))
                            .addModifier(Attributes.LUCK, new AttributeModifier(
                                    VILLAGERS_GAMBLE_LUCK_AFFIX_ID, 0.0, AttributeModifier.Operation.ADD_VALUE))
                            .build()))
    );

    // TODO: maybe move all these affix_ids somewhere?
    private static final Identifier CROWN_OF_STABILITY_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/crown_of_stability_resistance");
    private static final Identifier CROWN_OF_STABILITY_MAX_HEALTH_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/crown_of_stability_max_health");
    private static final Identifier CROWN_OF_STABILITY_CRIT_CHANCE_MULTIPLIER_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/crown_of_stability_crit_chance_multiplier");
    private static final Identifier CROWN_OF_STABILITY_CRIT_DAMAGE_MULTIPLIER_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/crown_of_stability_crit_damage_multiplier");


    // Crit normalization item
    public static final Supplier<Item> CROWN_OF_STABILITY = TumultuItemsRegistry.ITEMS.register(
            "crown_of_stability",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.HELMET)
                    .attributes(armorAttributesWithMaxHealth(ArmorMaterials.NETHERITE, ArmorType.HELMET,
                            EquipmentSlotGroup.HEAD, CROWN_OF_STABILITY_MAX_HEALTH_AFFIX_ID, 4.0))
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.crown_of_stability").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(CROWN_OF_STABILITY_RESISTANCE_AFFIX_ID, 0, 0.15),
                                    new RolledAffix(CROWN_OF_STABILITY_MAX_HEALTH_AFFIX_ID, 0, 4.0),
                                    new RolledAffix(CROWN_OF_STABILITY_CRIT_CHANCE_MULTIPLIER_AFFIX_ID, 0, 1.0),
                                    new RolledAffix(CROWN_OF_STABILITY_CRIT_DAMAGE_MULTIPLIER_AFFIX_ID, 0, -0.50)),
                            false)))
    );

    private static final Identifier REVERSED_FORCES_MAX_HEALTH_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/reversed_forces_max_health");
    private static final Identifier REVERSED_FORCES_MOVEMENT_SPEED_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/reversed_forces_movement_speed");
    private static final Identifier REVERSED_FORCES_FIRE_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/reversed_forces_fire_resistance");


    // Actually pretty funny item:
    // converts all the gravity modifiers on a player to a movement speed
    // gotta go fast!
    public static final Supplier<Item> REVERSED_FORCES = TumultuItemsRegistry.ITEMS.register(
            "reversed_forces",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.LEGGINGS)
                    .attributes(reversedForcesAttributes())
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.reversed_forces").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(REVERSED_FORCES_MAX_HEALTH_AFFIX_ID, 0, 10.0),
                                    new RolledAffix(REVERSED_FORCES_MOVEMENT_SPEED_AFFIX_ID, 0, 0.05),
                                    new RolledAffix(REVERSED_FORCES_FIRE_RESISTANCE_AFFIX_ID, 0, 0.10)),
                            false)))
    );

    private static ItemAttributeModifiers reversedForcesAttributes() {
        ItemAttributeModifiers base = ArmorMaterials.NETHERITE.createAttributes(ArmorType.LEGGINGS);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        base.modifiers().forEach(entry -> builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display()));
        builder.add(Attributes.MAX_HEALTH,
                new AttributeModifier(REVERSED_FORCES_MAX_HEALTH_AFFIX_ID, 10.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.LEGS, ItemAttributeModifiers.Display.hidden());
        builder.add(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(REVERSED_FORCES_MOVEMENT_SPEED_AFFIX_ID, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.LEGS, ItemAttributeModifiers.Display.hidden());
        return builder.build();
    }

    private static final Identifier REVERSED_TOXINS_MOVEMENT_SPEED_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/reversed_toxins_movement_speed");
    private static final Identifier REVERSED_TOXINS_RESISTANCE_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/reversed_toxins_resistance");
    private static final Identifier REVERSED_TOXINS_REVERSE_POISON_AFFIX_ID =
            Identifier.fromNamespaceAndPath(com.tumultu.TumultuMod.MOD_ID, "unique/reversed_toxins_reverse_poison");


    // another conversion unique, but on another axis
    // it converts incoming poison damage (both vanilla and tumultu origin) as healing.
    // possible self-stacking poison build?
    public static final Supplier<Item> REVERSED_TOXINS = TumultuItemsRegistry.ITEMS.register(
            "reversed_toxins",
            registryName -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.BOOTS)
                    .attributes(reversedToxinsAttributes())
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.CUSTOM_NAME,
                            Component.translatable("item.tumultu.reversed_toxins").withStyle(ItemRarity.UNIQUE.color()))
                    .component(TumultuDataComponents.AFFIX_DATA.get(), new AffixData(
                            ItemRarity.UNIQUE,
                            List.of(
                                    new RolledAffix(REVERSED_TOXINS_MOVEMENT_SPEED_AFFIX_ID, 0, 0.25),
                                    new RolledAffix(REVERSED_TOXINS_RESISTANCE_AFFIX_ID, 0, -0.10),
                                    new RolledAffix(REVERSED_TOXINS_REVERSE_POISON_AFFIX_ID, 0, 1.0)),
                            false)))
    );

    private static ItemAttributeModifiers reversedToxinsAttributes() {
        ItemAttributeModifiers base = ArmorMaterials.NETHERITE.createAttributes(ArmorType.BOOTS);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        base.modifiers().forEach(entry -> builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display()));
        builder.add(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(REVERSED_TOXINS_MOVEMENT_SPEED_AFFIX_ID, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.FEET, ItemAttributeModifiers.Display.hidden());
        return builder.build();
    }

    // after adding new unique, add it to this list (!!!)
    public static final List<Supplier<Item>> ALL = List.of(CINDERHEART, DEVOURER, REFLECTION_OF_IRON, DRAGONS_HEART, PILLAGERS_FAVOUR, VILLAGERS_GAMBLE, CROWN_OF_STABILITY, REVERSED_FORCES, REVERSED_TOXINS);
}
