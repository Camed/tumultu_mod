package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of known {@link AffixEffect} kinds, keyed by the "type" string used in JSON.
 * To add a new effect kind: create its record + MAP_CODEC, then add one {@link #register}
 * call below. Nothing else in the affix pipeline needs to change.
 */
public class AffixEffectTypes {
    private static final Map<String, MapCodec<? extends AffixEffect>> BY_ID = new HashMap<>();

    static {
        register("attribute", AttributeEffect.MAP_CODEC);
        register("area_mining", AreaMiningEffect.MAP_CODEC);
        register("physical_damage_percent", PhysicalDamagePercentEffect.MAP_CODEC);
        register("elemental_damage_percent", ElementalDamagePercentEffect.MAP_CODEC);
        register("flat_elemental_damage", FlatElementalDamageEffect.MAP_CODEC);
        register("elemental_resistance", ElementalResistanceEffect.MAP_CODEC);
        register("chance_to_bleed", ChanceToBleedEffect.MAP_CODEC);
        register("chance_to_poison", ChanceToPoisonEffect.MAP_CODEC);
        register("chance_to_wither", ChanceToWitherEffect.MAP_CODEC);
        register("life_on_hit", LifeOnHitEffect.MAP_CODEC);
        register("crit_chance", CritChanceEffect.MAP_CODEC);
        register("life_on_kill", LifeOnKillEffect.MAP_CODEC);
        register("hunger_on_kill", HungerOnKillEffect.MAP_CODEC);
        register("damage_over_time_multiplier", DamageOverTimeMultiplierEffect.MAP_CODEC);
        register("damage_over_time_duration", DamageOverTimeDurationEffect.MAP_CODEC);
        register("hybrid_armor_life", HybridArmorLifeEffect.MAP_CODEC);
        register("armor_applies_to_elemental", ArmorAppliesToElementalEffect.MAP_CODEC);
        register("thorns_multiplier", ThornsMultiplierEffect.MAP_CODEC);
        register("physical_damage_reduction", PhysicalDamageReductionEffect.MAP_CODEC);
        register("cannot_be_poisoned", CannotBePoisonedEffect.MAP_CODEC);
        register("life_regeneration", LifeRegenerationEffect.MAP_CODEC);
        register("flat_physical_damage", FlatPhysicalDamageEffect.MAP_CODEC);
        register("elemental_penetration", ElementalPenetrationEffect.MAP_CODEC);
        register("bleed_damage_multiplier", BleedDamageMultiplierEffect.MAP_CODEC);
        register("poison_damage_multiplier", PoisonDamageMultiplierEffect.MAP_CODEC);
        register("projectile_speed_percent", ProjectileSpeedEffect.MAP_CODEC);
        register("crit_damage_percent", CritDamagePercentEffect.MAP_CODEC);
        register("chance_extra_arrow", ChanceToFireAdditionalArrowEffect.MAP_CODEC);
        register("crit_damage_reduction", CritDamageReductionEffect.MAP_CODEC);
        register("melee_damage_percent", MeleeDamagePercentEffect.MAP_CODEC);
        register("ranged_damage_percent", RangedDamagePercentEffect.MAP_CODEC);
        register("max_health_to_physical_damage_percent", MaxHealthToPhysicalDamagePercentEffect.MAP_CODEC);
        register("cannot_be_bled", CannotBeBledEffect.MAP_CODEC);
        register("cannot_be_withered", CannotBeWitheredEffect.MAP_CODEC);
        register("all_damage_percent", AllDamagePercentEffect.MAP_CODEC);
        register("all_elemental_resistance", AllElementalResistanceEffect.MAP_CODEC);
        register("crit_chance_multiplier", CritChanceMultiplierEffect.MAP_CODEC);
        register("crit_damage_multiplier", CritDamageMultiplierEffect.MAP_CODEC);
        register("reverse_poison", ReversePoisonEffect.MAP_CODEC);
    }

    private static <T extends AffixEffect> void register(String id, MapCodec<T> codec) {
        BY_ID.put(id, codec);
    }

    static MapCodec<? extends AffixEffect> codecFor(String typeId) {
        MapCodec<? extends AffixEffect> codec = BY_ID.get(typeId);
        if (codec == null) {
            throw new IllegalArgumentException("Unknown affix effect type: " + typeId);
        }
        return codec;
    }
}