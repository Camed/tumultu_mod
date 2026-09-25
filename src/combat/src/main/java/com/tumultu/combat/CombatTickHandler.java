package com.tumultu.combat;

import com.tumultu.TumultuMod;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.registry.TumultuUniqueItems;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


// dots dots dots dots
public class CombatTickHandler {

    // playerId -> the entity whose bleed/poison state they want printed to their action bar.
    public static final Map<UUID, UUID> DEBUG_TARGETS = new ConcurrentHashMap<>();

    // decay of exposure per tick
    private static final float EXPOSURE_DECAY_PER_TICK = 0.5f;

    // unfortunately must match {@code LivingEntity}'s own hardcoded per-tick freeze decay
    // todo: work around this
    private static final float VANILLA_FREEZE_DECAY_PER_TICK = 2f;

    // hud thing
    private static final int METER_INDICATOR_REFRESH_TICKS = 30;

    // identifiers for reversed forces uq
    private static final Identifier REVERSED_FORCES_GRAVITY_CANCEL_ID =
            Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "unique/reversed_forces_gravity_cancel");
    private static final Identifier REVERSED_FORCES_SPEED_BONUS_ID =
            Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "unique/reversed_forces_speed_bonus");

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // pipeline here we go
        deliverPendingElementalHits(entity);
        tickDot(entity, TumultuAttachments.BLEED, TumultuDamageTypes.BLEED);
        tickPoisonStacks(entity);
        tickBurn(entity);
        tickShocked(entity);
        tickExposureDecay(entity, TumultuAttachments.FIRE_EXPOSURE, ElementKind.FIRE, TumultuMobEffects.FIRE_EXPOSURE);
        tickExposureDecay(entity, TumultuAttachments.LIGHTNING_EXPOSURE, ElementKind.LIGHTNING, TumultuMobEffects.LIGHTNING_EXPOSURE);
        tickFreezeDecay(entity);
        tickReversedForces(entity);
        tickFreezeIndicator(entity);
        tickLifeRegeneration(entity);

        if (entity instanceof ServerPlayer player) {
            tickDebugOverlay(player);
        }
    }

    // debug/dev thingies
    private static void tickDebugOverlay(ServerPlayer player) {
        UUID targetId = DEBUG_TARGETS.get(player.getUUID());
        if (targetId == null) return;

        Level level = player.level();
        Entity target = level.getEntity(targetId);
        if (!(target instanceof LivingEntity living) || !target.isAlive()) {
            DEBUG_TARGETS.remove(player.getUUID());
            player.sendOverlayMessage(Component.literal("[Tumultu] Debug target lost - overlay disabled."));
            return;
        }

        player.sendOverlayMessage(Component.literal(
                target.getName().getString()
                        + " | Bleed: " + DebugStatus.describeBleed(living)
                        + " | Poison: " + DebugStatus.describePoison(level, living)
                        + " | Burn: " + DebugStatus.describeBurn(living)
                        + " | Freeze: " + DebugStatus.describeFreeze(living)
                        + " | Shocked: " + DebugStatus.describeShocked(living)));
    }

    // reversed forces per tick recalculation
    private static void tickReversedForces(LivingEntity entity) {
        AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (gravity == null || movementSpeed == null) return;

        gravity.removeModifier(REVERSED_FORCES_GRAVITY_CANCEL_ID);
        movementSpeed.removeModifier(REVERSED_FORCES_SPEED_BONUS_ID);

        if (entity.getItemBySlot(EquipmentSlot.LEGS).getItem() != TumultuUniqueItems.REVERSED_FORCES.get()) {
            return;
        }

        double baseGravity = gravity.getBaseValue();
        if (baseGravity <= 0) return;
        double decrease = baseGravity - gravity.getValue();
        if (decrease <= 0) return;

        gravity.addOrUpdateTransientModifier(new AttributeModifier(
                REVERSED_FORCES_GRAVITY_CANCEL_ID, decrease, AttributeModifier.Operation.ADD_VALUE));
        movementSpeed.addOrUpdateTransientModifier(new AttributeModifier(
                REVERSED_FORCES_SPEED_BONUS_ID, decrease / baseGravity, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    // health regen
    private static void tickLifeRegeneration(LivingEntity entity) {
        if (entity.tickCount % 20 != 0) return;
        if (entity.getHealth() >= entity.getMaxHealth()) return;

        Registry<AffixDefinition> registry = entity.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        double regen = PlayerCombatStats.lifeRegenerationPerSecond(entity, registry);
        if (regen > 0) {
            entity.heal((float) regen);
        }
    }

    // apply queue of ele damage
    private static void deliverPendingElementalHits(LivingEntity entity) {
        Queue<CombatEventHandler.PendingHit> queue = CombatEventHandler.PENDING_ELEMENTAL.remove(entity.getUUID());
        if (queue == null) return;

        Level level = entity.level();
        for (CombatEventHandler.PendingHit hit : queue) {
            Entity attacker = level.getEntity(hit.attackerId());
            DamageSource source = level.damageSources().source(hit.damageType(), attacker);
            entity.hurt(source, hit.amount());
        }
    }

    // poison stacking handler
    private static void tickPoisonStacks(LivingEntity entity) {
        AttachmentType<List<DamageOverTimeData>> attachment = TumultuAttachments.POISON.get();
        if (!entity.hasData(attachment)) return;

        List<DamageOverTimeData> stacks = entity.getData(attachment);
        if (stacks.isEmpty()) return;

        Level level = entity.level();
        List<DamageOverTimeData> remaining = new ArrayList<>(stacks.size());
        for (DamageOverTimeData stack : stacks) {
            if (stack.shouldDealDamageThisTick()) {
                Entity attacker = level.getEntity(stack.sourceId());
                DamageSource source = level.damageSources().source(TumultuDamageTypes.POISON, attacker);
                entity.hurt(source, stack.damagePerSecond());
            }

            DamageOverTimeData ticked = stack.tickedDown();
            if (ticked.isActive()) {
                remaining.add(ticked);
            }
        }

        if (remaining.isEmpty()) {
            entity.removeData(attachment);
        } else {
            entity.setData(attachment, remaining);
        }
    }

    private static void tickDot(LivingEntity entity, Supplier<AttachmentType<DamageOverTimeData>> attachmentSupplier, ResourceKey<DamageType> damageType) {
        AttachmentType<DamageOverTimeData> attachment = attachmentSupplier.get();
        if (!entity.hasData(attachment)) return;

        DamageOverTimeData current = entity.getData(attachment);
        if (!current.isActive()) return;

        if (current.shouldDealDamageThisTick()) {
            Level level = entity.level();
            Entity attacker = level.getEntity(current.sourceId());
            DamageSource source = level.damageSources().source(damageType, attacker);
            entity.hurt(source, current.damagePerSecond());
        }

        DamageOverTimeData next = current.tickedDown();
        if (next.isActive()) {
            entity.setData(attachment, next);
        } else {
            entity.removeData(attachment);
        }
    }

    // calc burn based on exposure
    private static void tickBurn(LivingEntity entity) {
        AttachmentType<BurnData> attachment = TumultuAttachments.BURN.get();
        if (!entity.hasData(attachment)) return;

        BurnData current = entity.getData(attachment);
        if (!current.isActive()) return;

        if (current.shouldDealDamageThisTick()) {
            float exposure = currentExposure(entity, TumultuAttachments.FIRE_EXPOSURE);
            float damagePerSecond = (float) ((CombatEventHandler.BURN_BASE_DAMAGE_PER_SECOND
                    + exposure * CombatEventHandler.BURN_DAMAGE_PER_EXPOSURE_POINT) * (1 + current.dotMultiplier()));
            Level level = entity.level();
            Entity attacker = level.getEntity(current.sourceId());
            DamageSource source = level.damageSources().source(TumultuDamageTypes.BURN, attacker);
            entity.hurt(source, damagePerSecond);
        }

        BurnData next = current.tickedDown();
        if (next.isActive()) {
            entity.setData(attachment, next);
        } else {
            entity.removeData(attachment);
        }
    }

    private static float currentExposure(LivingEntity entity, Supplier<AttachmentType<ExposureData>> attachmentSupplier) {
        AttachmentType<ExposureData> attachment = attachmentSupplier.get();
        return entity.hasData(attachment) ? entity.getData(attachment).value() : 0f;
    }

    private static void tickShocked(LivingEntity entity) {
        AttachmentType<ShockedData> attachment = TumultuAttachments.SHOCKED.get();
        if (!entity.hasData(attachment)) return;

        ShockedData current = entity.getData(attachment);
        if (!current.isActive()) return;

        ShockedData next = current.tickedDown();
        if (next.isActive()) {
            entity.setData(attachment, next);
        } else {
            entity.removeData(attachment);
        }
    }

    private static void tickExposureDecay(LivingEntity entity, Supplier<AttachmentType<ExposureData>> attachmentSupplier,
            ElementKind element, Holder<MobEffect> indicatorEffect) {
        AttachmentType<ExposureData> attachment = attachmentSupplier.get();
        if (!entity.hasData(attachment)) return;

        ExposureData current = entity.getData(attachment);
        if (!current.isActive()) return;

        Registry<AffixDefinition> registry = entity.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        double resistance = PlayerCombatStats.elementalResistance(entity, element, registry);
        double decayRate = ResistanceScaling.scaledDecayRate(EXPOSURE_DECAY_PER_TICK, resistance);

        ExposureData next = current.decayedBy(decayRate);
        if (next.isActive()) {
            entity.setData(attachment, next);
            entity.addEffect(new MobEffectInstance(indicatorEffect, METER_INDICATOR_REFRESH_TICKS, meterAmplifier(next.value()), false, true, true));
        } else {
            entity.removeData(attachment);
        }
    }

    // amplifier 0-9 represents roughly how "full" a 0-100 meter is (0-9%, 10-19%, ... 90-100%)
    private static int meterAmplifier(float percent) {
        return Math.max(0, Math.min(9, (int) (percent / 10)));
    }

    // todo: fix when reworking freezing damage to be independent of vanilla
    private static void tickFreezeDecay(LivingEntity entity) {
        if (entity.isInPowderSnow && entity.canFreeze()) return;

        int ticksFrozen = entity.getTicksFrozen();
        if (ticksFrozen <= 0) return;

        Registry<AffixDefinition> registry = entity.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        double resistance = PlayerCombatStats.elementalResistance(entity, ElementKind.COLD, registry);
        if (resistance <= 0) return;

        double totalDesiredDecay = ResistanceScaling.scaledDecayRate(VANILLA_FREEZE_DECAY_PER_TICK, resistance);
        double extraDecay = totalDesiredDecay - VANILLA_FREEZE_DECAY_PER_TICK;
        if (extraDecay <= 0) return;

        int newValue = Math.max(0, ticksFrozen - (int) Math.round(extraDecay));
        entity.setTicksFrozen(newValue);
    }
    private static void tickFreezeIndicator(LivingEntity entity) {
        if (!entity.isFreezing()) return;
        int amplifier = meterAmplifier(entity.getPercentFrozen() * 100);
        entity.addEffect(new MobEffectInstance(TumultuMobEffects.FREEZING, METER_INDICATOR_REFRESH_TICKS, amplifier, false, true, true));
    }
}
