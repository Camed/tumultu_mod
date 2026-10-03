package com.tumultu.unique.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class UniqueArmorModels {

    private static void addPlaceholderHead(PartDefinition root) {
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
    }

    private static void addPlaceholderTorso(PartDefinition root) {
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
    }

    private static void addPlaceholderArms(PartDefinition root) {
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
    }

    private static void addPlaceholderLegs(PartDefinition root) {
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
    }

    public static LayerDefinition createCrownOfStabilityLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -11.0F, -5.0F, 10.0F, 5.0F, 10.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        addPlaceholderTorso(root);
        addPlaceholderArms(root);
        addPlaceholderLegs(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createReversedForcesLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addPlaceholderHead(root);

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(48, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        addPlaceholderArms(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createReversedToxinsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addPlaceholderHead(root);
        addPlaceholderTorso(root);

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(48, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        addPlaceholderArms(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createDragonsHeartLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addPlaceholderHead(root);

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(36, 14).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(22, 0).addBox(-4.0F, -2.0F, -3.0F, 5.0F, 9.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -3.0F, 5.0F, 9.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        addPlaceholderLegs(root);
        return LayerDefinition.create(mesh, 64, 32);
    }
}
