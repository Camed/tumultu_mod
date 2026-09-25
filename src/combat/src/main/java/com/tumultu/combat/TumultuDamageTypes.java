package com.tumultu.combat;

import com.tumultu.TumultuMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * Datapack-defined damage types (data/tumultu/damage_type/*.json) - these are pure data,
 * like affix registry, so only a ResourceKey per type is needed in code, mirroring
 * vanilla's own DamageTypes interface.
 */
public interface TumultuDamageTypes {
    ResourceKey<DamageType> FIRE = key("fire");
    ResourceKey<DamageType> COLD = key("cold");
    ResourceKey<DamageType> LIGHTNING = key("lightning");
    ResourceKey<DamageType> BLEED = key("bleed");
    ResourceKey<DamageType> POISON = key("poison");
    ResourceKey<DamageType> BURN = key("burn");

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, path));
    }
}
