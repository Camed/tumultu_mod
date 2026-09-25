package com.tumultu.mobs.client;

import com.tumultu.TumultuMod;
import com.tumultu.mobs.entity.TumultuBruteMonster;
import com.tumultu.registry.TumultuModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

// blightbone renderer
public class BlightboneRenderer extends MobRenderer<TumultuBruteMonster, BlightboneRenderState, BlightboneModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "textures/entity/blightbone.png");

    public BlightboneRenderer(EntityRendererProvider.Context context) {
        super(context, new BlightboneModel(context.bakeLayer(TumultuModelLayers.BLIGHTBONE)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(BlightboneRenderState state) {
        return TEXTURE;
    }

    @Override
    public BlightboneRenderState createRenderState() {
        return new BlightboneRenderState();
    }

    @Override
    public void extractRenderState(TumultuBruteMonster entity, BlightboneRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }
}
