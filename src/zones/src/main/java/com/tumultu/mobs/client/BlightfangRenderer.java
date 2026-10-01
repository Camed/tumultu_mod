package com.tumultu.mobs.client;

import com.tumultu.mobs.entity.TumultuBruteMonster;
import com.tumultu.registry.TumultuModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

// blightfang renderer
public class BlightfangRenderer extends MobRenderer<TumultuBruteMonster, BlightfangRenderState, BlightfangModel> {
    private final Identifier texture;

    public BlightfangRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context, new BlightfangModel(context.bakeLayer(TumultuModelLayers.BLIGHTFANG)), 0.6F);
        this.texture = texture;
    }

    @Override
    public Identifier getTextureLocation(BlightfangRenderState state) {
        return this.texture;
    }

    @Override
    public BlightfangRenderState createRenderState() {
        return new BlightfangRenderState();
    }

    @Override
    public void extractRenderState(TumultuBruteMonster entity, BlightfangRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
    }
}
