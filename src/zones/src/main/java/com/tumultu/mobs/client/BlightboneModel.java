package com.tumultu.mobs.client;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// blightbone model
public class BlightboneModel extends EntityModel<BlightboneRenderState> {
	private final ModelPart base;
	private final ModelPart R_leg;
	private final ModelPart L_leg;
	private final ModelPart body;
	private final ModelPart body_mossstuffs;
	private final ModelPart L_arm;
	private final ModelPart R_arm;
	private final ModelPart neck;
	private final ModelPart head;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation attackAnimation;

	public BlightboneModel(ModelPart root) {
		super(root);
		this.base = root.getChild("base");
		this.R_leg = this.base.getChild("R_leg");
		this.L_leg = this.base.getChild("L_leg");
		this.body = this.base.getChild("body");
		this.body_mossstuffs = this.body.getChild("body_mossstuffs");
		this.L_arm = this.body.getChild("L_arm");
		this.R_arm = this.body.getChild("R_arm");
		this.neck = this.body.getChild("neck");
		this.head = this.neck.getChild("head");

		this.idleAnimation = BlightboneAnimation.idle.bake(root);
		this.walkAnimation = BlightboneAnimation.walk.bake(root);
		this.attackAnimation = BlightboneAnimation.attack.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition R_leg = base.addOrReplaceChild("R_leg", CubeListBuilder.create().texOffs(32, 41).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 26).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(50, 38).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(-2.0F, -18.0F, 0.0F));

		PartDefinition L_leg = base.addOrReplaceChild("L_leg", CubeListBuilder.create().texOffs(0, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.0F, 4.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(43, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.2F))
		.texOffs(52, 19).addBox(-2.0F, 4.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(2.0F, -18.0F, 0.0F));

		PartDefinition body = base.addOrReplaceChild("body", CubeListBuilder.create().texOffs(35, 13).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(35, 0).addBox(-4.0F, -11.0F, -3.0F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 33).addBox(0.0F, -9.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(17, 41).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -19.0F, 0.0F));

		PartDefinition body_mossstuffs = body.addOrReplaceChild("body_mossstuffs", CubeListBuilder.create().texOffs(56, 0).addBox(-6.5F, -2.0F, -3.0F, 7.0F, 13.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(-0.5F, -9.0F, 0.0F));

		PartDefinition L_arm = body.addOrReplaceChild("L_arm", CubeListBuilder.create().texOffs(17, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 50).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 23.0F, 2.0F, new CubeDeformation(0.2F))
		.texOffs(9, 47).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -6.0F, 0.0F));

		PartDefinition R_arm = body.addOrReplaceChild("R_arm", CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 47).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 23.0F, 2.0F, new CubeDeformation(0.2F))
		.texOffs(9, 63).addBox(-1.5F, 17.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(9, 55).addBox(-1.5F, 7.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -8.0F, 0.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(41, 41).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(31, 52).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2F))
		.texOffs(43, 21).addBox(1.0F, -8.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -8.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(BlightboneRenderState state) {
		super.setupAnim(state);
		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
	}
}
