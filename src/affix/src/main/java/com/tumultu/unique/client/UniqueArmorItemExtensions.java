package com.tumultu.unique.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class UniqueArmorItemExtensions implements IClientItemExtensions {
    private final ModelLayerLocation layer;
    private final EquipmentClientInfo.LayerType targetLayerType;
    private HumanoidModel<HumanoidRenderState> baked;

    public UniqueArmorItemExtensions(ModelLayerLocation layer, EquipmentClientInfo.LayerType targetLayerType) {
        this.layer = layer;
        this.targetLayerType = targetLayerType;
    }

    @Override
    public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        if (layerType != targetLayerType) {
            return original;
        }
        if (baked == null) {
            baked = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
        return baked;
    }
}
