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

// blightlord model
public class BlightlordModel extends EntityModel<BlightlordRenderState> {
	private final ModelPart body;
	private final ModelPart legs;
	private final ModelPart leg_left;
	private final ModelPart leg_left_upper;
	private final ModelPart leg_left_lower;
	private final ModelPart leg_right;
	private final ModelPart leg_right_upper;
	private final ModelPart leg_right_lower;
	private final ModelPart chest;
	private final ModelPart chest_lower;
	private final ModelPart chest_mid;
	private final ModelPart chest_upper;
	private final ModelPart arms;
	private final ModelPart arm_left;
	private final ModelPart arm_left_upper;
	private final ModelPart arm_left_lower;
	private final ModelPart arm_right;
	private final ModelPart arm_right_upper;
	private final ModelPart arm_right_lower;
	private final ModelPart head;
	private final ModelPart horns;
	private final ModelPart main;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation attackAnimation;

	public BlightlordModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.legs = this.body.getChild("legs");
		this.leg_left = this.legs.getChild("leg_left");
		this.leg_left_upper = this.leg_left.getChild("leg_left_upper");
		this.leg_left_lower = this.leg_left.getChild("leg_left_lower");
		this.leg_right = this.legs.getChild("leg_right");
		this.leg_right_upper = this.leg_right.getChild("leg_right_upper");
		this.leg_right_lower = this.leg_right.getChild("leg_right_lower");
		this.chest = this.body.getChild("chest");
		this.chest_lower = this.chest.getChild("chest_lower");
		this.chest_mid = this.chest.getChild("chest_mid");
		this.chest_upper = this.chest.getChild("chest_upper");
		this.arms = this.body.getChild("arms");
		this.arm_left = this.arms.getChild("arm_left");
		this.arm_left_upper = this.arm_left.getChild("arm_left_upper");
		this.arm_left_lower = this.arm_left.getChild("arm_left_lower");
		this.arm_right = this.arms.getChild("arm_right");
		this.arm_right_upper = this.arm_right.getChild("arm_right_upper");
		this.arm_right_lower = this.arm_right.getChild("arm_right_lower");
		this.head = this.body.getChild("head");
		this.horns = this.head.getChild("horns");
		this.main = this.head.getChild("main");

		this.idleAnimation = BlightlordAnimation.idle.bake(root);
		this.walkAnimation = BlightlordAnimation.walk.bake(root);
		this.attackAnimation = BlightlordAnimation.attack.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition legs = body.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_left = legs.addOrReplaceChild("leg_left", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_left_upper = leg_left.addOrReplaceChild("leg_left_upper", CubeListBuilder.create().texOffs(0, 18).addBox(1.0F, -14.0F, -2.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_left_lower = leg_left.addOrReplaceChild("leg_left_lower", CubeListBuilder.create().texOffs(40, 32).addBox(3.0F, -1.0F, -3.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(40, 14).addBox(2.0F, -7.0F, -1.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_right = legs.addOrReplaceChild("leg_right", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_right_upper = leg_right.addOrReplaceChild("leg_right_upper", CubeListBuilder.create().texOffs(20, 18).addBox(-6.0F, -14.0F, -2.0F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leg_right_lower = leg_right.addOrReplaceChild("leg_right_lower", CubeListBuilder.create().texOffs(40, 38).addBox(-6.0F, -1.0F, -3.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(40, 23).addBox(-6.0F, -7.0F, -1.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_lower = chest.addOrReplaceChild("chest_lower", CubeListBuilder.create().texOffs(0, 9).addBox(-5.0F, -18.0F, -2.0F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_mid = chest.addOrReplaceChild("chest_mid", CubeListBuilder.create().texOffs(0, 30).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_upper = chest.addOrReplaceChild("chest_upper", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -29.0F, -2.0F, 12.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arms = body.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arm_left = arms.addOrReplaceChild("arm_left", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arm_left_upper = arm_left.addOrReplaceChild("arm_left_upper", CubeListBuilder.create().texOffs(34, 0).addBox(6.0F, -28.0F, -1.0F, 2.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arm_left_lower = arm_left.addOrReplaceChild("arm_left_lower", CubeListBuilder.create().texOffs(10, 40).addBox(6.0F, -17.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arm_right = arms.addOrReplaceChild("arm_right", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arm_right_upper = arm_right.addOrReplaceChild("arm_right_upper", CubeListBuilder.create().texOffs(0, 40).addBox(-8.0F, -28.0F, -1.0F, 2.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arm_right_lower = arm_right.addOrReplaceChild("arm_right_lower", CubeListBuilder.create().texOffs(44, 0).addBox(-8.0F, -17.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition horns = head.addOrReplaceChild("horns", CubeListBuilder.create().texOffs(25, 44).addBox(-8.0F, -43.0F, 0.0F, 16.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition main = head.addOrReplaceChild("main", CubeListBuilder.create().texOffs(22, 30).addBox(-3.0F, -40.0F, -1.0F, 6.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(BlightlordRenderState state) {
		super.setupAnim(state);
		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
	}
}
