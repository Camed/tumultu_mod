package com.tumultu.affix;

import java.util.Optional;

// Current benchcrafting categories.
public enum RecipeCategory {
    PHYSICAL_OFFENSE("physical_offense"),
    ELEMENTAL_OFFENSE("elemental_offense"),
    CRITICAL("critical"),
    BLEED("bleed"),
    POISON("poison"),
    DOT_GENERAL("dot_general"),
    ELEMENTAL_DEFENSE("elemental_defense"),
    PHYSICAL_DEFENSE("physical_defense"),
    ARMOR_STATS("armor_stats"),
    LIFE("life"),
    SUSTENANCE("sustenance"),
    MOBILITY("mobility"),
    FORTUNE("fortune"),
    RANGED_UTILITY("ranged_utility"),
    MINING_SPEED("mining_speed"),
    MINING_TRAVERSAL("mining_traversal"),
    MINING_BONUS("mining_bonus");

    private final String id;

    RecipeCategory(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<RecipeCategory> byId(String id) {
        for (RecipeCategory category : values()) {
            if (category.id.equals(id)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
