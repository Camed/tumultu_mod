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
}
