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

// blightfang model
public class BlightfangModel extends EntityModel<BlightfangRenderState> {
	private final ModelPart Body;
	private final ModelPart thorns;
	private final ModelPart legs;
	private final ModelPart right;
	private final ModelPart front_right;
	private final ModelPart mid_right;
	private final ModelPart back_right;
	private final ModelPart left;
	private final ModelPart front_left;
	private final ModelPart mid_left;
	private final ModelPart back_left;
	private final ModelPart tail;
	private final ModelPart smaller;
	private final ModelPart bigger;
	private final ModelPart head;
	private final ModelPart torso;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation hurtAnimation;
	private final KeyframeAnimation deathAnimation;

	public BlightfangModel(ModelPart root) {
		super(root);
		this.Body = root.getChild("Body");
		this.thorns = this.Body.getChild("thorns");
		this.legs = this.Body.getChild("legs");
		this.right = this.legs.getChild("right");
		this.front_right = this.right.getChild("front_right");
		this.mid_right = this.right.getChild("mid_right");
		this.back_right = this.right.getChild("back_right");
		this.left = this.legs.getChild("left");
		this.front_left = this.left.getChild("front_left");
		this.mid_left = this.left.getChild("mid_left");
		this.back_left = this.left.getChild("back_left");
		this.tail = this.Body.getChild("tail");
		this.smaller = this.tail.getChild("smaller");
		this.bigger = this.tail.getChild("bigger");
		this.head = this.Body.getChild("head");
		this.torso = this.Body.getChild("torso");

		this.idleAnimation = BlightfangAnimation.idle.bake(root);
		this.walkAnimation = BlightfangAnimation.walk.bake(root);
		this.attackAnimation = BlightfangAnimation.attack.bake(root);
		this.hurtAnimation = BlightfangAnimation.hurt.bake(root);
		this.deathAnimation = BlightfangAnimation.death.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition thorns = Body.addOrReplaceChild("thorns", CubeListBuilder.create().texOffs(22, 14).addBox(2.5F, -6.0F, -2.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(18, 22).addBox(-4.0F, -6.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 24).addBox(-2.0F, -5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(22, 22).addBox(1.5F, -6.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 24).addBox(-0.5F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-1.0F, -6.0F, 2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 24).addBox(-4.0F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 17).addBox(2.5F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 19).addBox(5.0F, -4.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 16).addBox(5.0F, -3.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 25).addBox(-6.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(22, 10).addBox(-7.0F, -4.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition legs = Body.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = legs.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(-4.0F, 0.0F, -2.0F));

		PartDefinition front_right = right.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(14, 10).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition mid_right = right.addOrReplaceChild("mid_right", CubeListBuilder.create().texOffs(16, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition back_right = right.addOrReplaceChild("back_right", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.0F));

		PartDefinition left = legs.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition front_left = left.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(14, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, -2.0F));

		PartDefinition mid_left = left.addOrReplaceChild("mid_left", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -2.0F, 2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, -2.0F));

		PartDefinition back_left = left.addOrReplaceChild("back_left", CubeListBuilder.create().texOffs(8, 18).addBox(-1.0F, -2.0F, 5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, -2.0F));

		PartDefinition tail = Body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition smaller = tail.addOrReplaceChild("smaller", CubeListBuilder.create().texOffs(22, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 7.0F));

		PartDefinition bigger = tail.addOrReplaceChild("bigger", CubeListBuilder.create().texOffs(8, 22).addBox(-2.0F, -3.0F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = Body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -5.0F, -6.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition torso = Body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.0F, -3.0F, 10.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(BlightfangRenderState state) {
		super.setupAnim(state);
		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
		this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
	}
}
