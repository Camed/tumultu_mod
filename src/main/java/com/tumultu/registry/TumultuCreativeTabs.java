package com.tumultu.registry;

import com.tumultu.Tumultu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TumultuCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Tumultu.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TUMULTU_ITEMS_TAB = CREATIVE_MODE_TABS.register(
            "tumultu_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tumultu"))
                    .icon(() -> CurrencyItems.SHAPING_SHARD.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(CurrencyItems.SHAPING_SHARD.get());
                        output.accept(CurrencyItems.GROWTH_SHARD.get());
                        output.accept(CurrencyItems.HEIGHTENING_SHARD.get());
                        output.accept(CurrencyItems.INFUSION_SHARD.get());
                        output.accept(CurrencyItems.CLEANSING_SHARD.get());
                        output.accept(CurrencyItems.TWISTING_SHARD.get());
                        output.accept(CurrencyItems.PEAKING_SHARD.get());
                        output.accept(CurrencyItems.WEAVING_SHARD.get());
                        output.accept(CurrencyItems.SEVERING_SHARD.get());
                        output.accept(CurrencyItems.ENDFUSING_SHARD.get());
                        output.accept(CurrencyItems.UNSPOKEN_PROMISE.get());
                        output.accept(CurrencyItems.REBINDING_ASHES.get());
                        output.accept(CurrencyItems.EMPOWERING_GEM.get());
                        output.accept(CurrencyItems.IMBUING_SHARD.get());
                        output.accept(CurrencyItems.MYSTERIOUS_BOOKPAGE.get());
                        output.accept(CraftingBlocks.CRAFTING_BENCH_ITEM.get());
                        output.accept(CraftingBlocks.SHARD_STASH_ITEM.get());
                        output.accept(TumultuItemDefinitions.RING.get());
                        output.accept(TumultuItemDefinitions.AMULET.get());
                        output.accept(TumultuItemDefinitions.BLIGHTFANG_SPAWN_EGG.get());
                        output.accept(TumultuItemDefinitions.BLIGHTFANG_ALPHA_SPAWN_EGG.get());
                        output.accept(TumultuItemDefinitions.BLIGHTLORD_SPAWN_EGG.get());
                        output.accept(TumultuItemDefinitions.BLIGHTBONE_SPAWN_EGG.get());
                        for (var uniqueItem : TumultuUniqueItems.ALL) {
                            output.accept(uniqueItem.get());
                        }
                        for (var recipeItem : TumultuRecipeItems.ALL) {
                            output.accept(recipeItem.get());
                        }
                    })
                    .build()
    );
}
