package com.tumultu.affix.effect;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public interface AffixEffect {
    Codec<AffixEffect> CODEC = Codec.STRING.dispatch("type", AffixEffect::typeId, AffixEffectTypes::codecFor);

    String typeId();
    Component describe(double rolledValue);
    boolean isPercentage();
}