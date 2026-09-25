package com.tumultu.mobs.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// as these mobs are coming from this mod, they are the way we give player a challenge - they need to have some specific scaling angles, so some builds are better and some worse for them.
// they inherit this mod stat system
public record MobDefinition(
        int tier,
        double maxHealth,
        double attackDamage,
        double armor,
        double movementSpeed,
        double followRange,
        double knockbackResistance,
        int xpReward,
        double fireResistance,
        double coldResistance,
        double lightningResistance,
        double poisonChanceOnHit,
        double poisonDamagePerSecond,
        int poisonDurationTicks,
        double scale
) {
    public static final Codec<MobDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("tier").forGetter(MobDefinition::tier),
                    Codec.DOUBLE.fieldOf("max_health").forGetter(MobDefinition::maxHealth),
                    Codec.DOUBLE.fieldOf("attack_damage").forGetter(MobDefinition::attackDamage),
                    Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(MobDefinition::armor),
                    Codec.DOUBLE.fieldOf("movement_speed").forGetter(MobDefinition::movementSpeed),
                    Codec.DOUBLE.optionalFieldOf("follow_range", 32.0).forGetter(MobDefinition::followRange),
                    Codec.DOUBLE.optionalFieldOf("knockback_resistance", 0.0).forGetter(MobDefinition::knockbackResistance),
                    Codec.INT.optionalFieldOf("xp_reward", 5).forGetter(MobDefinition::xpReward),
                    Codec.DOUBLE.optionalFieldOf("fire_resistance", 0.0).forGetter(MobDefinition::fireResistance),
                    Codec.DOUBLE.optionalFieldOf("cold_resistance", 0.0).forGetter(MobDefinition::coldResistance),
                    Codec.DOUBLE.optionalFieldOf("lightning_resistance", 0.0).forGetter(MobDefinition::lightningResistance),
                    Codec.DOUBLE.optionalFieldOf("poison_chance_on_hit", 0.0).forGetter(MobDefinition::poisonChanceOnHit),
                    Codec.DOUBLE.optionalFieldOf("poison_damage_per_second", 0.0).forGetter(MobDefinition::poisonDamagePerSecond),
                    Codec.INT.optionalFieldOf("poison_duration_ticks", 0).forGetter(MobDefinition::poisonDurationTicks),
                    Codec.DOUBLE.optionalFieldOf("scale", 1.0).forGetter(MobDefinition::scale)
            ).apply(instance, MobDefinition::new)
    );
}
