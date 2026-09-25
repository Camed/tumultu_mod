package com.tumultu.registry;

import com.tumultu.TumultuMod;
import com.tumultu.bench.CraftingBenchMenu;
import com.tumultu.stash.ShardStashMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CraftingMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TumultuMod.MOD_ID);

    public static final Supplier<MenuType<CraftingBenchMenu>> CRAFTING_BENCH_MENU = MENUS.register(
            "crafting_bench",
            () -> new MenuType<>(CraftingBenchMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final Supplier<MenuType<ShardStashMenu>> SHARD_STASH_MENU = MENUS.register(
            "shard_stash",
            () -> new MenuType<>(ShardStashMenu::new, FeatureFlags.VANILLA_SET)
    );
}
