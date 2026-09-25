package com.tumultu;

import com.tumultu.affix.AffixTooltipAppender;
import com.tumultu.affix.OutruledTooltipAppender;
import com.tumultu.combat.CombatEventHandler;
import com.tumultu.combat.CombatTickHandler;
import com.tumultu.combat.RangedEventHandler;
import com.tumultu.combat.TumultuAttachments;
import com.tumultu.combat.TumultuMobEffects;
import com.tumultu.core.command.TumultuCommands;
import com.tumultu.core.event.AreaMiningEventHandler;
import com.tumultu.loot.BonusDropHandler;
import com.tumultu.registry.BlightlandsBlockEntities;
import com.tumultu.registry.BlightlandsBlocks;
import com.tumultu.registry.CraftingBlockEntities;
import com.tumultu.registry.CraftingBlocks;
import com.tumultu.registry.CraftingMenus;
import com.tumultu.registry.CurrencyItems;
import com.tumultu.registry.TumultuBlockEntitiesRegistry;
import com.tumultu.registry.TumultuBlocksRegistry;
import com.tumultu.registry.TumultuCreativeTabs;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuEntityTypes;
import com.tumultu.registry.TumultuItemDefinitions;
import com.tumultu.registry.TumultuItemsRegistry;
import com.tumultu.registry.TumultuLootFunctions;
import com.tumultu.registry.TumultuMobRegistries;
import com.tumultu.registry.TumultuRecipeItems;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.registry.TumultuUniqueItems;
import com.tumultu.network.PreviewResultPayload;
import com.tumultu.worldgen.BlightlandsRegion;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import terrablender.api.Regions;
import org.slf4j.LoggerFactory;

@Mod(Tumultu.MOD_ID)
public class Tumultu {
    public static final String MOD_ID = TumultuMod.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Tumultu(IEventBus modBus) {
        TumultuRegistries.AFFIX_DEFINITIONS.register(modBus);
        TumultuMobRegistries.MOB_DEFINITIONS.register(modBus);

        TumultuDataComponents.DATA_COMPONENTS.register(modBus);

        TumultuEntityTypes.ENTITY_TYPES.register(modBus);

        // TumultuRecipeItems, TumultuUniqueItems, TumultuItemDefinitions and CurrencyItems each add
        // extra entries into TumultuItemsRegistry.ITEMS as a side effect of their own static
        // initializers (17 recipe items, the Unique items, the generic/spawn-egg items, and the
        // currency shards respectively) - none has its own DeferredRegister.Items, so they must be
        // touched here, before ITEMS.register(modBus) finalizes that registry, or those entries
        // would silently never get registered. TumultuBlocks/BlightlandsBlocks register their own
        // BlockItems directly (registerSimpleBlockItem), so they need the same touch before
        // ITEMS.register(modBus), not just before their own bucket's register(modBus) below.
        TumultuRecipeItems.ALL.size();
        TumultuUniqueItems.ALL.size();
        TumultuItemDefinitions.touch();
        CurrencyItems.touch();
        CraftingBlocks.CRAFTING_BENCH_ITEM.getId();
        BlightlandsBlocks.BLIGHT_IDOL.getId();

        TumultuItemsRegistry.ITEMS.register(modBus);

        TumultuBlocksRegistry.BLOCKS.register(modBus);

        CraftingBlockEntities.touch();
        BlightlandsBlockEntities.touch();
        TumultuBlockEntitiesRegistry.BLOCK_ENTITIES.register(modBus);

        CraftingMenus.MENUS.register(modBus);

        TumultuCreativeTabs.CREATIVE_MODE_TABS.register(modBus);

        TumultuAttachments.ATTACHMENTS.register(modBus);

        TumultuMobEffects.MOB_EFFECTS.register(modBus);

        TumultuLootFunctions.LOOT_FUNCTIONS.register(modBus);

        modBus.addListener(TumultuRegistries::onNewRegistry);

        modBus.addListener(TumultuMobRegistries::onNewRegistry);

        modBus.addListener(TumultuEntityTypes::onEntityAttributeCreation);

        modBus.addListener(TumultuEntityTypes::onRegisterSpawnPlacements);

        modBus.addListener(this::registerTooltipAppenders);

        modBus.addListener(this::registerPayloadHandlers);

        NeoForge.EVENT_BUS.register(AreaMiningEventHandler.class);

        NeoForge.EVENT_BUS.register(CombatEventHandler.class);

        NeoForge.EVENT_BUS.register(CombatTickHandler.class);

        NeoForge.EVENT_BUS.register(RangedEventHandler.class);

        NeoForge.EVENT_BUS.register(BonusDropHandler.class);

        NeoForge.EVENT_BUS.addListener(TumultuCommands::register);

        Regions.register(new BlightlandsRegion(Identifier.fromNamespaceAndPath(MOD_ID, "tier3_biome_region"), 20));

        LOGGER.info("Tumultu initialized");
    }

    private void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
        event.registerComponentAppenderAfterAll(TumultuDataComponents.AFFIX_DATA, new AffixTooltipAppender());
        event.registerComponentAppenderAfterAll(TumultuDataComponents.OUTRULED, new OutruledTooltipAppender());
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER) {
            event.registrar("1").playToClient(PreviewResultPayload.TYPE, PreviewResultPayload.STREAM_CODEC);
        }
    }
}