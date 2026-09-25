package com.tumultu.mobs.client;

import com.tumultu.TumultuMod;
import com.tumultu.mobs.entity.TumultuBruteMonster;
import com.tumultu.registry.TumultuModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

// blightlord renderer
public class BlightlordRenderer extends MobRenderer<TumultuBruteMonster, BlightlordRenderState, BlightlordModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "textures/entity/blightlord.png");

    public BlightlordRenderer(EntityRendererProvider.Context context) {
        super(context, new BlightlordModel(context.bakeLayer(TumultuModelLayers.BLIGHTLORD)), 0.9F);
    }

    @Override
    public Identifier getTextureLocation(BlightlordRenderState state) {
        return TEXTURE;
    }

    @Override
    public BlightlordRenderState createRenderState() {
        return new BlightlordRenderState();
    }

    @Override
    public void extractRenderState(TumultuBruteMonster entity, BlightlordRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }
}
