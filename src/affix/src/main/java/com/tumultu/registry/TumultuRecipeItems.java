package com.tumultu.registry;

import com.tumultu.affix.RecipeCategory;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Currently existing, one per {@link RecipeCategory} - each is consumed by the
 * crafting bench to unlock crafting from that category.
 */
public class TumultuRecipeItems {
    private static final Map<RecipeCategory, Supplier<Item>> BY_CATEGORY = new EnumMap<>(RecipeCategory.class);

    public static final Supplier<Item> PHYSICAL_OFFENSE = register("recipe_physical_offense", RecipeCategory.PHYSICAL_OFFENSE);
    public static final Supplier<Item> ELEMENTAL_OFFENSE = register("recipe_elemental_offense", RecipeCategory.ELEMENTAL_OFFENSE);
    public static final Supplier<Item> CRITICAL = register("recipe_critical", RecipeCategory.CRITICAL);
    public static final Supplier<Item> BLEED = register("recipe_bleed", RecipeCategory.BLEED);
    public static final Supplier<Item> POISON = register("recipe_poison", RecipeCategory.POISON);
    public static final Supplier<Item> DOT_GENERAL = register("recipe_dot_general", RecipeCategory.DOT_GENERAL);
    public static final Supplier<Item> ELEMENTAL_DEFENSE = register("recipe_elemental_defense", RecipeCategory.ELEMENTAL_DEFENSE);
    public static final Supplier<Item> PHYSICAL_DEFENSE = register("recipe_physical_defense", RecipeCategory.PHYSICAL_DEFENSE);
    public static final Supplier<Item> ARMOR_STATS = register("recipe_armor_stats", RecipeCategory.ARMOR_STATS);
    public static final Supplier<Item> LIFE = register("recipe_life", RecipeCategory.LIFE);
    public static final Supplier<Item> SUSTENANCE = register("recipe_sustenance", RecipeCategory.SUSTENANCE);
    public static final Supplier<Item> MOBILITY = register("recipe_mobility", RecipeCategory.MOBILITY);
    public static final Supplier<Item> FORTUNE = register("recipe_fortune", RecipeCategory.FORTUNE);
    public static final Supplier<Item> RANGED_UTILITY = register("recipe_ranged_utility", RecipeCategory.RANGED_UTILITY);
    public static final Supplier<Item> MINING_SPEED = register("recipe_mining_speed", RecipeCategory.MINING_SPEED);
    public static final Supplier<Item> MINING_TRAVERSAL = register("recipe_mining_traversal", RecipeCategory.MINING_TRAVERSAL);
    public static final Supplier<Item> MINING_BONUS = register("recipe_mining_bonus", RecipeCategory.MINING_BONUS);

    public static final List<Supplier<Item>> ALL = List.of(
            PHYSICAL_OFFENSE, ELEMENTAL_OFFENSE, CRITICAL, BLEED, POISON, DOT_GENERAL,
            ELEMENTAL_DEFENSE, PHYSICAL_DEFENSE, ARMOR_STATS, LIFE, SUSTENANCE, MOBILITY,
            FORTUNE, RANGED_UTILITY, MINING_SPEED, MINING_TRAVERSAL, MINING_BONUS
    );

    private static Supplier<Item> register(String name, RecipeCategory category) {
        Supplier<Item> item = TumultuItemsRegistry.ITEMS.register(
                name,
                registryName -> new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName))
                        .stacksTo(32))
        );
        BY_CATEGORY.put(category, item);
        return item;
    }

    /** Linear scan over 17 entries, resolved lazily since the underlying DeferredRegister
     * suppliers aren't safe to call during this class's own static initialization. Only ever
     * called from bench GUI interactions, not a hot path. */
    public static Optional<RecipeCategory> categoryOf(Item item) {
        for (Map.Entry<RecipeCategory, Supplier<Item>> entry : BY_CATEGORY.entrySet()) {
            if (entry.getValue().get() == item) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
}
