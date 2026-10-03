package com.tumultu.registry;

import com.tumultu.TumultuMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;


// where are the models stored
public class TumultuModelLayers {
    public static final ModelLayerLocation BLIGHTFANG =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "blightfang"), "main");
    public static final ModelLayerLocation BLIGHTLORD =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "blightlord"), "main");
    public static final ModelLayerLocation BLIGHTBONE =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "blightbone"), "main");
    public static final ModelLayerLocation BLIGHTED_BOAT =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "boat/tumultu_blighted"), "main");
    public static final ModelLayerLocation BLIGHTED_CHEST_BOAT =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "chest_boat/tumultu_blighted"), "main");
    public static final ModelLayerLocation CROWN_OF_STABILITY_ARMOR =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "crown_of_stability"), "main");
    public static final ModelLayerLocation REVERSED_FORCES_ARMOR =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "reversed_forces"), "main");
    public static final ModelLayerLocation REVERSED_TOXINS_ARMOR =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "reversed_toxins"), "main");
    public static final ModelLayerLocation DRAGONS_HEART_ARMOR =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "dragons_heart"), "main");
}
