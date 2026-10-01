package com.tumultu.mobs.entity;

import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.combat.CombatEventHandler;
import com.tumultu.combat.PlayerCombatStats;
import com.tumultu.combat.TieredCombatant;
import com.tumultu.mobs.definition.MobDefinition;
import com.tumultu.registry.TumultuMobRegistries;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.zones.WorldTier;
import com.tumultu.zones.WorldTierScaling;
import com.tumultu.zones.WorldTierScalingReloadListener;
import com.tumultu.zones.ZoneBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

// common base for mobs
// todo: move stats somewhere else
public abstract class AbstractTumultuMonster extends Monster implements TieredCombatant {
    private int tier = 1;

    // todo: all of these should be somewhere else, they can be getted/setted here yet not kept there
    private double fireResistance;
    private double coldResistance;
    private double lightningResistance;

    // thats the ugly one, fact we have it here
    private double poisonChanceOnHit;
    private double poisonDamagePerSecond;
    private int poisonDurationTicks;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    protected AbstractTumultuMonster(EntityType<? extends AbstractTumultuMonster> type, Level level) {
        super(type, level);
    }

    public int tier() {
        return this.tier;
    }


    // todo: to be changed when things above are changed too
    @Override
    public double elementalResistance(ElementKind kind) {
        return switch (kind) {
            case FIRE -> this.fireResistance;
            case COLD -> this.coldResistance;
            case LIGHTNING -> this.lightningResistance;
        };
    }

    // todo: to be changed when things above are changed too
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean success = super.doHurtTarget(level, target);
        if (success && this.poisonChanceOnHit > 0 && target instanceof LivingEntity livingTarget) {
            Registry<AffixDefinition> registry = level.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
            if (this.random.nextDouble() < this.poisonChanceOnHit && !PlayerCombatStats.isImmuneToPoison(livingTarget, registry)) {
                CombatEventHandler.inflictPoison(livingTarget, this.getUUID(), (float) this.poisonDamagePerSecond, this.poisonDurationTicks);
            }
        }
        return success;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        boolean moving = !this.isDeadOrDying() && this.walkAnimation.isMoving();
        this.idleAnimationState.animateWhen(!this.isDeadOrDying() && !moving, this.tickCount);
        this.walkAnimationState.animateWhen(moving, this.tickCount);
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 2) {
            this.hurtAnimationState.start(this.tickCount);
        } else if (id == 3) {
            this.deathAnimationState.start(this.tickCount);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData
    ) {
        groupData = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        applyMobDefinition(level);
        return groupData;
    }

    private void applyMobDefinition(ServerLevelAccessor level) {
        Identifier ownId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        Registry<MobDefinition> registry = level.registryAccess().lookupOrThrow(TumultuMobRegistries.MOB_KEY);
        MobDefinition definition = registry.getValue(ownId);
        if (definition == null) {
            return;
        }

        // definition.tier() itself is no longer what drives difficulty - world position does (see
        // WorldTier), plus the current biome's own constant on top (ZoneBiomeTags, e.g. blightlands
        // is tier3_biome so its mobs are always at least tier 3 regardless of how close to spawn
        // the biome patch happens to generate). Only life/damage/armor/XP scale with this - the rest
        // of a mob's stats (speed, resistances, poison, etc.) stay exactly what its own
        // MobDefinition says, same as before.
        BlockPos pos = this.blockPosition();
        int worldTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
        Holder<Biome> biome = level.getBiome(pos);
        int effectiveTier = Math.min(worldTier + ZoneBiomeTags.constantFor(biome), WorldTier.MAX);
        WorldTierScaling scaling = WorldTierScalingReloadListener.current();
        double multiplier = scaling.multiplierFor(effectiveTier);

        this.tier = effectiveTier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(definition.maxHealth() * multiplier);
        this.setHealth(this.getMaxHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(definition.attackDamage() * multiplier);
        this.getAttribute(Attributes.ARMOR).setBaseValue(definition.armor() * multiplier);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(definition.movementSpeed());
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(definition.followRange());
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(definition.knockbackResistance());
        this.getAttribute(Attributes.SCALE).setBaseValue(definition.scale());
        this.xpReward = (int) Math.round(definition.xpReward() * multiplier);
        this.fireResistance = definition.fireResistance();
        this.coldResistance = definition.coldResistance();
        this.lightningResistance = definition.lightningResistance();
        this.poisonChanceOnHit = definition.poisonChanceOnHit();
        this.poisonDamagePerSecond = definition.poisonDamagePerSecond();
        this.poisonDurationTicks = definition.poisonDurationTicks();
    }
}
