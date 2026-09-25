package com.tumultu.client;

import com.tumultu.Tumultu;
import com.tumultu.bench.CraftingBenchScreen;
import com.tumultu.stash.ShardStashScreen;
import com.tumultu.mobs.client.BlightboneModel;
import com.tumultu.mobs.client.BlightboneRenderer;
import com.tumultu.mobs.client.BlightfangModel;
import com.tumultu.mobs.client.BlightfangRenderer;
import com.tumultu.mobs.client.BlightlordModel;
import com.tumultu.mobs.client.BlightlordRenderer;
import com.tumultu.network.PreviewResultPayload;
import com.tumultu.registry.CraftingMenus;
import com.tumultu.registry.TumultuEntityTypes;
import com.tumultu.registry.TumultuModelLayers;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Tumultu.MOD_ID, value = Dist.CLIENT)
public class TumultuClientEvents {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(CraftingMenus.CRAFTING_BENCH_MENU.get(), CraftingBenchScreen::new);
        event.register(CraftingMenus.SHARD_STASH_MENU.get(), ShardStashScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TumultuModelLayers.BLIGHTFANG, BlightfangModel::createBodyLayer);
        event.registerLayerDefinition(TumultuModelLayers.BLIGHTLORD, BlightlordModel::createBodyLayer);
        event.registerLayerDefinition(TumultuModelLayers.BLIGHTBONE, BlightboneModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TumultuEntityTypes.BLIGHTFANG.get(), context ->
                new BlightfangRenderer(context, Identifier.fromNamespaceAndPath(Tumultu.MOD_ID, "textures/entity/blightfang.png")));
        event.registerEntityRenderer(TumultuEntityTypes.BLIGHTFANG_ALPHA.get(), context ->
                new BlightfangRenderer(context, Identifier.fromNamespaceAndPath(Tumultu.MOD_ID, "textures/entity/blightfang_alpha.png")));
        event.registerEntityRenderer(TumultuEntityTypes.BLIGHTLORD.get(), BlightlordRenderer::new);
        event.registerEntityRenderer(TumultuEntityTypes.BLIGHTBONE.get(), BlightboneRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(StatPanelOverlay.class);
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(PreviewResultPayload.TYPE, PreviewResultPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        CraftingBenchScreen.handlePreviewResult(payload.affixId(), payload.tier(), payload.value())));
    }
}
