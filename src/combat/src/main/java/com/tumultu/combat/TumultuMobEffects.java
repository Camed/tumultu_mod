package com.tumultu.combat;

import com.tumultu.TumultuMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// cosmetic dots effects
public class TumultuMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, TumultuMod.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING = MOB_EFFECTS.register(
            "bleeding", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xA6191B) {});

    public static final DeferredHolder<MobEffect, MobEffect> POISONED = MOB_EFFECTS.register(
            "poisoned", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4E9331) {});

    public static final DeferredHolder<MobEffect, MobEffect> BURNING = MOB_EFFECTS.register(
            "burning", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFF7800) {});

    public static final DeferredHolder<MobEffect, MobEffect> SHOCKED = MOB_EFFECTS.register(
            "shocked", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4DA6FF) {});

    public static final DeferredHolder<MobEffect, MobEffect> FIRE_EXPOSURE = MOB_EFFECTS.register(
            "fire_exposure", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF9E2C) {});

    public static final DeferredHolder<MobEffect, MobEffect> LIGHTNING_EXPOSURE = MOB_EFFECTS.register(
            "lightning_exposure", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x00CFFF) {});

    public static final DeferredHolder<MobEffect, MobEffect> FREEZING = MOB_EFFECTS.register(
            "freezing", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x66CCFF) {});
}
