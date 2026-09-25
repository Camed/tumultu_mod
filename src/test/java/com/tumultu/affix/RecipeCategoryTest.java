package com.tumultu.affix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeCategoryTest {

    @Test
    void byIdRoundTripsEveryCategory() {
        for (RecipeCategory category : RecipeCategory.values()) {
            assertEquals(category, RecipeCategory.byId(category.id()).orElseThrow());
        }
    }

    @Test
    void byIdIsEmptyForAnUnknownId() {
        assertTrue(RecipeCategory.byId("not_a_real_category").isEmpty());
    }

    @Test
    void everyCategoryHasAUniqueId() {
        long distinctIds = java.util.Arrays.stream(RecipeCategory.values()).map(RecipeCategory::id).distinct().count();
        assertEquals(RecipeCategory.values().length, distinctIds);
    }
}
