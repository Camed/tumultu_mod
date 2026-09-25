package com.tumultu.combat;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.AffixEffect;
import com.tumultu.affix.effect.AllDamagePercentEffect;
import com.tumultu.affix.effect.BleedDamageMultiplierEffect;
import com.tumultu.affix.effect.ChanceToBleedEffect;
import com.tumultu.affix.effect.ChanceToPoisonEffect;
import com.tumultu.affix.effect.ChanceToWitherEffect;
import com.tumultu.affix.effect.DamageOverTimeDurationEffect;
import com.tumultu.affix.effect.DamageOverTimeMultiplierEffect;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.affix.effect.ElementalDamagePercentEffect;
import com.tumultu.affix.effect.ElementalPenetrationEffect;
import com.tumultu.affix.effect.FlatElementalDamageEffect;
import com.tumultu.affix.effect.FlatPhysicalDamageEffect;
import com.tumultu.affix.effect.HungerOnKillEffect;
import com.tumultu.affix.effect.LifeOnHitEffect;
import com.tumultu.affix.effect.LifeOnKillEffect;
import com.tumultu.affix.effect.MaxHealthToPhysicalDamagePercentEffect;
import com.tumultu.affix.effect.MeleeDamagePercentEffect;
import com.tumultu.affix.effect.PhysicalDamagePercentEffect;
import com.tumultu.affix.effect.PoisonDamageMultiplierEffect;
import com.tumultu.affix.effect.RangedDamagePercentEffect;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.registry.TumultuUniqueItems;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The melee combat pipeline. {@code onIncomingDamage} (pre-mitigation) branches on what kind of
 * hit this is: mods own elemental follow-up (mitigated by the victim's resists), Thorns
 * retaliation (scaled by the thorns-wearer's multiplier), or an ordinary hit (attacker's
 * physical%/crit - now sourced from armor too, not just the weapon - then the victim's own Armor,
 * via {@link PlayerCombatStats#armorDamageMultiplier} replacing vanilla's own formula entirely,
 * followed by "additional physical damage reduction" affixes on the result). {@code onDamagePost}
 * (confirmed landed) rolls elemental damage/life-steal/bleed-poison-wither once a hit lands,
 * and {@code onDeath} handles on-kill rewards. Elemental damage is delivered as a separate
 * follow-up hit (see {@link #PENDING_ELEMENTAL} and {@link CombatTickHandler}) rather than
 * folded into the main number, since each element needs its own real DamageType
 * for resistances to mean anything - there is no way to carry two DamageTypes in a single hurt() call (afaik lol)
 */
public class CombatEventHandler {
    public static final int DOT_DURATION_TICKS = 100;
    public static final float DOT_BASE_DAMAGE_PER_SECOND = 1.5f;
    private static final int WITHER_DURATION_TICKS = 60;
    private static final double BLEED_DAMAGE_PERCENT_OF_HIT_PER_SECOND = 0.25;
    private static final int MAX_POISON_STACKS = 10;
    private static final double BASE_CRIT_DAMAGE_BONUS = 1.0;

    // fire exposure / burning
    private static final float FIRE_EXPOSURE_GAIN_PER_DAMAGE = 4f;
    private static final int BURN_BASE_DURATION_TICKS = DOT_DURATION_TICKS;
    static final float BURN_BASE_DAMAGE_PER_SECOND = 0.5f;
    static final float BURN_DAMAGE_PER_EXPOSURE_POINT = 0.05f;

    // cold freeze
    private static final float COLD_FREEZE_GAIN_PER_DAMAGE = 4f;

    // lightning exposure / shocked
    private static final float LIGHTNING_EXPOSURE_GAIN_PER_DAMAGE = 4f;
    private static final int SHOCK_BASE_DURATION_TICKS = DOT_DURATION_TICKS;

    // shocked state incoming damage multiplier
    private static final double SHOCK_DAMAGE_TAKEN_INCREASE = 0.15;

    // cinderheart conversion ratio
    private static final double CINDERHEART_OVERCAP_MULTIPLIER_PER_POINT = 0.5;

    // elemental damage types are all in data/minecraft/tags/damage_type/bypasses_cooldown.json -
    // without it, this follow-up hit (delivered next tick, still well inside the melee hit's own
    // ~20-tick invulnerability window) would be silently dropped by LivingEntity.hurtServer's own
    // "damage <= lastHurt" check whenever the elemental amount is smaller than the melee hit that
    // caused it - which is the common case, since it's normally a bonus, not the main hit.
    record PendingHit(ResourceKey<DamageType> damageType, float amount, UUID attackerId) {}
    static final Map<UUID, Queue<PendingHit>> PENDING_ELEMENTAL = new ConcurrentHashMap<>();

     // dot ticks that bypass mitigation in {@link #onIncomingDamage} and
     // land exactly as dealt
    private static boolean isDotFollowUpDamage(DamageSource source) {
        return source.is(TumultuDamageTypes.BLEED) || source.is(TumultuDamageTypes.POISON);
    }

     // Every kind of damage this pipeline generates itself as a follow-up to an original hit -
     // used by {@link #onDamagePost} to avoid re-rolling bleed/poison/burn/elemental procs off of a
     // hit that was already one of those procs (which would cascade). {@link #onIncomingDamage}
     // uses the narrower {@link #isDotFollowUpDamage} instead, since ele dmg still need
     // their own mitigation pass there.
    private static boolean isOwnFollowUpDamage(DamageSource source) {
        return isDotFollowUpDamage(source) || elementOf(source) != null;
    }

    // determines type of damage applied
    private static ElementKind elementOf(DamageSource source) {
        if (source.is(TumultuDamageTypes.FIRE) || source.is(TumultuDamageTypes.BURN)) return ElementKind.FIRE;
        if (source.is(TumultuDamageTypes.COLD) || source.is(DamageTypes.FREEZE)) return ElementKind.COLD;
        if (source.is(TumultuDamageTypes.LIGHTNING)) return ElementKind.LIGHTNING;
        return null;
    }

    // devlog shenanigans
    private static String devLogMessage(LivingEntity attacker, LivingEntity victim, DamageSource source, float healthDamage) {
        float healthAfter = victim.getHealth();
        float healthBefore = healthAfter + healthDamage;
        ItemStack weapon = source.getWeaponItem();
        String weaponName = (weapon != null && !weapon.isEmpty()) ? weapon.getHoverName().getString() : "fists";
        return String.format("%s hit %s with %s for %.2f %s damage (HP %.1f -> %.1f)",
                attacker.getName().getString(), victim.getName().getString(), weaponName,
                healthDamage, source.getMsgId(), healthBefore, healthAfter);
    }

    // apply shocked multiplayer if target is shocked
    private static float applyShockedMultiplier(LivingEntity target, float amount) {
        AttachmentType<ShockedData> attachment = TumultuAttachments.SHOCKED.get();
        if (!target.hasData(attachment)) return amount;
        if (!target.getData(attachment).isActive()) return amount;
        return (float) (amount * (1 + SHOCK_DAMAGE_TAKEN_INCREASE));
    }

    // get the crit multiplier based on crit damage percent, bonus multiplayer and victims crit dmg reduction
    static double critMultiplier(boolean crit, double critDamagePercent, double critDamageReduction, double critDamageBonusMultiplier) {
        if (!crit) return 1.0;
        double bonus = Math.max(0, (BASE_CRIT_DAMAGE_BONUS + critDamagePercent) * critDamageBonusMultiplier - critDamageReduction);
        return 1 + bonus;
    }

    // unique cinderhearts fire dmg multiplier
    private static double cinderheartFireMultiplier(LivingEntity wielder, Registry<AffixDefinition> registry) {
        double overcap = PlayerCombatStats.elementalResistanceOvercap(wielder, ElementKind.FIRE, registry);
        return 1 + CINDERHEART_OVERCAP_MULTIPLIER_PER_POINT * overcap;
    }

    // unique ROI dmg multiplier (based on attackers armour)
    private static double reflectionOfIronMultiplier(LivingEntity wielder, ItemStack weapon, Registry<AffixDefinition> registry) {
        if (weapon == null || weapon.getItem() != TumultuUniqueItems.REFLECTION_OF_IRON.get()) {
            return 1.0;
        }
        double armor = PlayerCombatStats.trueArmor(wielder, registry);
        return Math.pow(2, armor / 10.0);
    }


    // defensive thingies/resistances/immunities/converts are implemented here
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource incoming = event.getSource();

        if (incoming.is(TumultuDamageTypes.POISON) || incoming.is(NeoForgeMod.POISON_DAMAGE)) {
            LivingEntity poisonVictim = event.getEntity();
            Registry<AffixDefinition> registry = poisonVictim.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);

            //unique reversed toxins enabler
            if (PlayerCombatStats.hasReversedPoison(poisonVictim, registry)) {
                event.setCanceled(true);
                poisonVictim.heal(event.getAmount());
                return;
            }
        }

        if (isDotFollowUpDamage(incoming)) return;
        ElementKind element = elementOf(incoming);
        if (element != null) {
            LivingEntity victim = event.getEntity();
            Registry<AffixDefinition> registry = victim.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
            double resistance = PlayerCombatStats.elementalResistance(victim, element, registry);

            // penetration
            if (incoming.getEntity() instanceof LivingEntity attacker) {
                double penetration = PlayerCombatStats.elementalPenetration(attacker, registry);
                if (penetration > 0) {
                    resistance *= (1 - Math.min(penetration, 1.0));
                }
            }

            float amount = resistance > 0 ? (float) (event.getAmount() * (1 - resistance)) : event.getAmount();
            event.setAmount(applyShockedMultiplier(victim, amount));
            return;
        }

        // thorns overwritten
        if (incoming.is(DamageTypes.THORNS)) {
            float amount = event.getAmount();
            if (incoming.getEntity() instanceof LivingEntity thornsWearer) {
                Registry<AffixDefinition> registry = thornsWearer.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
                double multiplier = PlayerCombatStats.thornsMultiplier(thornsWearer, registry);
                if (multiplier > 0) {
                    amount *= (1 + multiplier);
                }
            }
            event.setAmount(applyShockedMultiplier(event.getEntity(), amount));
            return;
        }

        LivingEntity victim = event.getEntity();
        Registry<AffixDefinition> registry = victim.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        float amount = event.getAmount();

        // phys damage calculations
        if (incoming.getEntity() instanceof LivingEntity attacker) {
            // phys flats
            double physicalPercent = 0;
            double flatPhysical = 0;

            //crits
            double critChance = PlayerCombatStats.critChance(attacker, registry);
            double critDamagePercent = PlayerCombatStats.critDamagePercent(attacker, registry);

            // other damage increases
            ItemStack weapon = incoming.getWeaponItem();
            AffixData weaponData = weapon != null ? weapon.get(TumultuDataComponents.AFFIX_DATA.get()) : null;
            boolean isMeleeHit = incoming.isDirect();

            List<RolledAffix> physicalSourceAffixes = new ArrayList<>();
            if (weaponData != null) physicalSourceAffixes.addAll(weaponData.affixes());
            for (AffixData curio : PlayerCombatStats.curioAffixData(attacker)) physicalSourceAffixes.addAll(curio.affixes());

            for (RolledAffix rolled : physicalSourceAffixes) {
                AffixDefinition def = registry.getValue(rolled.affixId());
                if (def == null) continue;
                AffixEffect effect = def.effect();
                if (effect instanceof PhysicalDamagePercentEffect) physicalPercent += rolled.rolledValue();
                else if (effect instanceof FlatPhysicalDamageEffect) flatPhysical += rolled.rolledValue();
                else if (effect instanceof MeleeDamagePercentEffect && isMeleeHit) physicalPercent += rolled.rolledValue();
                else if (effect instanceof RangedDamagePercentEffect && !isMeleeHit) physicalPercent += rolled.rolledValue();
                // pillagers favour "increased Damage" - additive into whichever pool a physical
                // hit uses, same as physical_damage_percent (see onDamagePost's mirror of this for
                // the elemental follow-up's own pool).
                else if (effect instanceof AllDamagePercentEffect) physicalPercent += rolled.rolledValue();
                // devourer's power - read live off the attacker's current max health, so other
                // max-health gear worn alongside it scales this up automatically, same as any
                // other stat derived from a live attribute rather than snapshotted at roll time.
                else if (effect instanceof MaxHealthToPhysicalDamagePercentEffect) {
                    flatPhysical += attacker.getAttributeValue(Attributes.MAX_HEALTH) * rolled.rolledValue();
                }
            }

            boolean crit = critChance > 0 && victim.getRandom().nextDouble() < critChance;
            double critDamageReduction = PlayerCombatStats.critDamageReduction(victim, registry);
            double critDamageBonusMultiplier = PlayerCombatStats.critDamageMultiplier(attacker, registry);
            double critMultiplier = critMultiplier(crit, critDamagePercent, critDamageReduction, critDamageBonusMultiplier);
            double reflectionMultiplier = reflectionOfIronMultiplier(attacker, weapon, registry);
            amount = (float) ((amount + flatPhysical) * (1 + physicalPercent) * critMultiplier * reflectionMultiplier);
        }

        event.setAmount(applyShockedMultiplier(victim, amount));

        // Victim-side defense: PlayerCombatStats.finalPhysicalDamageMultiplier (Armor's own curve
        // with "additional physical damage reduction" affixes applied to its result, capped at
        // MAX_FINAL_PHYSICAL_REDUCTION overall) registered as a Reduction.ARMOR override so it
        // fully REPLACES vanilla's own armor calculation rather than stacking with it - vanilla
        // would otherwise still run its own armor math afterward, in the normal damage pipeline,
        // using the same Attributes.ARMOR value.
        if (!incoming.is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, vanillaReduction) -> {
                double before = container.getNewDamage();
                double after = before * PlayerCombatStats.finalPhysicalDamageMultiplier(victim, registry);
                return (float) (before - after);
            });
        }
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity victim = event.getEntity();

        // devlog shenanigans
        if (!isDotFollowUpDamage(source)) {
            CombatDevLog.log(attacker, devLogMessage(attacker, victim, source, event.getHealthDamage()));
        }

        if (isOwnFollowUpDamage(source)) return;

        ItemStack weapon = source.getWeaponItem();
        AffixData weaponData = (weapon != null && !weapon.isEmpty()) ? weapon.get(TumultuDataComponents.AFFIX_DATA.get()) : null;
        List<AffixData> curios = PlayerCombatStats.curioAffixData(attacker);

        // curio's elemental/life-on-hit/proc-chance affixes count the same way the weapon's
        // do - "equip a ring, punch things on fire" should work with no weapon affixes at all.
        List<RolledAffix> data = new ArrayList<>();
        if (weaponData != null) data.addAll(weaponData.affixes());
        for (AffixData curio : curios) data.addAll(curio.affixes());
        if (data.isEmpty()) return;

        Registry<AffixDefinition> registry = victim.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        RandomSource random = victim.getRandom();

        // armour + weapon combined, same source of truth as onIncomingDamage's physical crit roll.
        double critChance = PlayerCombatStats.critChance(attacker, registry);
        double critDamagePercent = PlayerCombatStats.critDamagePercent(attacker, registry);

        double elementalPercent = 0;
        double lifeOnHit = 0;
        double bleedChance = 0;
        double poisonChance = 0;
        double witherChance = 0;
        double dotMultiplier = 0;
        double dotDuration = 0;
        double bleedMultiplierBonus = 0;
        double poisonMultiplierBonus = 0;
        Map<ElementKind, Double> flatElemental = new EnumMap<>(ElementKind.class);

        for (RolledAffix rolled : data) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) continue;
            AffixEffect effect = def.effect();
            if (effect instanceof ElementalDamagePercentEffect) elementalPercent += rolled.rolledValue();
            else if (effect instanceof AllDamagePercentEffect) elementalPercent += rolled.rolledValue();
            else if (effect instanceof FlatElementalDamageEffect flat) flatElemental.merge(flat.element(), rolled.rolledValue(), Double::sum);
            else if (effect instanceof LifeOnHitEffect) lifeOnHit += rolled.rolledValue();
            else if (effect instanceof ChanceToBleedEffect) bleedChance += rolled.rolledValue();
            else if (effect instanceof ChanceToPoisonEffect) poisonChance += rolled.rolledValue();
            else if (effect instanceof ChanceToWitherEffect) witherChance += rolled.rolledValue();
            else if (effect instanceof DamageOverTimeMultiplierEffect) dotMultiplier += rolled.rolledValue();
            else if (effect instanceof DamageOverTimeDurationEffect) dotDuration += rolled.rolledValue();
            else if (effect instanceof BleedDamageMultiplierEffect) bleedMultiplierBonus += rolled.rolledValue();
            else if (effect instanceof PoisonDamageMultiplierEffect) poisonMultiplierBonus += rolled.rolledValue();
        }

        if (lifeOnHit > 0) {
            attacker.heal((float) lifeOnHit);
        }

        if (!flatElemental.isEmpty()) {
            boolean elementalCrit = critChance > 0 && random.nextDouble() < critChance;
            double critDamageReduction = PlayerCombatStats.critDamageReduction(victim, registry);
            double critDamageBonusMultiplier = PlayerCombatStats.critDamageMultiplier(attacker, registry);
            double elementalCritMultiplier = critMultiplier(elementalCrit, critDamagePercent, critDamageReduction, critDamageBonusMultiplier);
            double multiplier = (1 + elementalPercent) * elementalCritMultiplier;
            UUID attackerId = attacker.getUUID();

            for (Map.Entry<ElementKind, Double> entry : flatElemental.entrySet()) {
                float amount = (float) (entry.getValue() * multiplier);
                if (amount <= 0) continue;

                ElementKind kind = entry.getKey();
                if (kind == ElementKind.FIRE && weapon != null && weapon.getItem() == TumultuUniqueItems.CINDERHEART.get()) {
                    amount *= cinderheartFireMultiplier(attacker, registry);
                }
                ResourceKey<DamageType> damageType = switch (kind) {
                    case FIRE -> TumultuDamageTypes.FIRE;
                    case COLD -> TumultuDamageTypes.COLD;
                    case LIGHTNING -> TumultuDamageTypes.LIGHTNING;
                };
                PENDING_ELEMENTAL.computeIfAbsent(victim.getUUID(), id -> new ConcurrentLinkedQueue<>())
                        .add(new PendingHit(damageType, amount, attackerId));

                switch (kind) {
                    case FIRE -> applyFireExposure(victim, attackerId, amount, dotMultiplier, dotDuration, registry, random);
                    case COLD -> applyColdFreeze(victim, amount);
                    case LIGHTNING -> applyLightningExposure(victim, amount, dotDuration, registry, random);
                }
            }
        }

        if (bleedChance > 0 && random.nextDouble() < bleedChance && !PlayerCombatStats.isImmuneToBleed(victim, registry)) {
            double bleedBase = event.getHealthDamage() * BLEED_DAMAGE_PERCENT_OF_HIT_PER_SECOND;
            applyBleed(victim, attacker.getUUID(), bleedBase, dotMultiplier + bleedMultiplierBonus, dotDuration);
        }
        if (poisonChance > 0 && random.nextDouble() < poisonChance && !PlayerCombatStats.isImmuneToPoison(victim, registry)) {
            stackPoison(victim, attacker.getUUID(), dotMultiplier + poisonMultiplierBonus, dotDuration);
        }
        if (witherChance > 0 && random.nextDouble() < witherChance && !PlayerCombatStats.isImmuneToWither(victim, registry)) {
            int witherDuration = (int) Math.round(WITHER_DURATION_TICKS * (1 + dotMultiplier + dotDuration));
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, witherDuration, 0));
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;

        ItemStack weapon = attacker.getMainHandItem();
        AffixData weaponData = weapon.get(TumultuDataComponents.AFFIX_DATA.get());

        List<RolledAffix> data = new ArrayList<>();
        if (weaponData != null) data.addAll(weaponData.affixes());
        for (AffixData curio : PlayerCombatStats.curioAffixData(attacker)) data.addAll(curio.affixes());
        if (data.isEmpty()) return;

        Registry<AffixDefinition> registry = attacker.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        double lifeOnKill = 0;
        double hungerOnKill = 0;
        for (RolledAffix rolled : data) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) continue;
            AffixEffect effect = def.effect();
            if (effect instanceof LifeOnKillEffect) lifeOnKill += rolled.rolledValue();
            else if (effect instanceof HungerOnKillEffect) hungerOnKill += rolled.rolledValue();
        }

        if (lifeOnKill > 0) {
            attacker.heal((float) lifeOnKill);
        }
        if (hungerOnKill > 0) {
            attacker.getFoodData().eat((int) Math.round(hungerOnKill), 0f);
        }
    }

    // vanilla statuses blocker (if applicable)
    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) return;
        boolean isPoison = effectInstance.is(MobEffects.POISON);
        boolean isWither = effectInstance.is(MobEffects.WITHER);
        if (!isPoison && !isWither) return;

        LivingEntity entity = event.getEntity();
        Registry<AffixDefinition> registry = entity.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        if (isPoison && PlayerCombatStats.isImmuneToPoison(entity, registry)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        } else if (isWither && PlayerCombatStats.isImmuneToWither(entity, registry)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    // dmg based on hit
    private static void applyBleed(LivingEntity victim, UUID attackerId, double baseDamagePerSecond, double dotMultiplier, double dotDuration) {
        float damagePerSecond = (float) (baseDamagePerSecond * (1 + dotMultiplier));
        int duration = (int) Math.round(DOT_DURATION_TICKS * (1 + dotDuration));
        inflictBleed(victim, attackerId, damagePerSecond, duration);
    }

    // dmg based on amount of stacks
    private static void stackPoison(LivingEntity victim, UUID attackerId, double dotMultiplier, double dotDuration) {
        float damagePerSecond = (float) (DOT_BASE_DAMAGE_PER_SECOND * (1 + dotMultiplier));
        int duration = (int) Math.round(DOT_DURATION_TICKS * (1 + dotDuration));
        inflictPoison(victim, attackerId, damagePerSecond, duration);
    }

    public static void inflictBleed(LivingEntity victim, UUID attackerId, float damagePerSecond, int durationTicks) {
        victim.setData(TumultuAttachments.BLEED.get(), DamageOverTimeData.create(durationTicks, damagePerSecond, attackerId));

        // add cosmetic thingy, so victim can actually see that its bleeding
        victim.addEffect(new MobEffectInstance(TumultuMobEffects.BLEEDING, durationTicks, 0, false, true, true));
    }

    // append poison stack
    public static void inflictPoison(LivingEntity victim, UUID attackerId, float damagePerSecond, int durationTicks) {
        DamageOverTimeData newStack = DamageOverTimeData.create(durationTicks, damagePerSecond, attackerId);

        AttachmentType<List<DamageOverTimeData>> attachment = TumultuAttachments.POISON.get();
        List<DamageOverTimeData> current = victim.hasData(attachment) ? victim.getData(attachment) : List.of();

        List<DamageOverTimeData> updated = new ArrayList<>(current);
        updated.add(newStack);

        // remove oldest stack before applying new
        if (updated.size() > MAX_POISON_STACKS) {
            updated.remove(0);
        }

        victim.setData(attachment, List.copyOf(updated));

        // add cosmetic thingy, so victim can actually see that its poisoned
        victim.addEffect(new MobEffectInstance(TumultuMobEffects.POISONED, durationTicks, 0, false, true, true));
    }

    // apply fire exposure, which then can translate into burning damage
    // scales with victims fire resistance
    private static void applyFireExposure(LivingEntity victim, UUID attackerId, float fireDamage,
            double dotMultiplier, double dotDuration, Registry<AffixDefinition> registry, RandomSource random) {
        AttachmentType<ExposureData> attachment = TumultuAttachments.FIRE_EXPOSURE.get();
        ExposureData current = victim.hasData(attachment) ? victim.getData(attachment) : ExposureData.INACTIVE;
        ExposureData updated = current.withGain(fireDamage * FIRE_EXPOSURE_GAIN_PER_DAMAGE);
        victim.setData(attachment, updated);

        double igniteChance = updated.value() / ExposureData.MAX;
        if (random.nextDouble() >= igniteChance) return;

        double fireResistance = PlayerCombatStats.elementalResistance(victim, ElementKind.FIRE, registry);
        int baseDuration = (int) Math.round(BURN_BASE_DURATION_TICKS * (1 + dotDuration));
        int duration = ResistanceScaling.scaledDuration(baseDuration, fireResistance);
        if (duration > 0) {
            inflictBurn(victim, attackerId, dotMultiplier, duration);
        }
    }

    // apply vanilla freeze on cold damage
    private static void applyColdFreeze(LivingEntity victim, float coldDamage) {
        if (!victim.canFreeze()) return;
        int gain = Math.round(coldDamage * COLD_FREEZE_GAIN_PER_DAMAGE);
        int newValue = Math.min(victim.getTicksRequiredToFreeze(), victim.getTicksFrozen() + gain);
        victim.setTicksFrozen(newValue);
    }

    // as for fire exposure - higher damage, higher exposure, higher chance to put "shocked" on an enemy
    private static void applyLightningExposure(LivingEntity victim, float lightningDamage,
            double dotDuration, Registry<AffixDefinition> registry, RandomSource random) {
        AttachmentType<ExposureData> attachment = TumultuAttachments.LIGHTNING_EXPOSURE.get();
        ExposureData current = victim.hasData(attachment) ? victim.getData(attachment) : ExposureData.INACTIVE;
        ExposureData updated = current.withGain(lightningDamage * LIGHTNING_EXPOSURE_GAIN_PER_DAMAGE);
        victim.setData(attachment, updated);

        double shockChance = updated.value() / ExposureData.MAX;
        if (random.nextDouble() >= shockChance) return;

        double lightningResistance = PlayerCombatStats.elementalResistance(victim, ElementKind.LIGHTNING, registry);
        int baseDuration = (int) Math.round(SHOCK_BASE_DURATION_TICKS * (1 + dotDuration));
        int duration = ResistanceScaling.scaledDuration(baseDuration, lightningResistance);
        if (duration > 0) {
            inflictShock(victim, duration);
        }
    }

    // apply burn status
    public static void inflictBurn(LivingEntity victim, UUID attackerId, double dotMultiplier, int durationTicks) {
        victim.setData(TumultuAttachments.BURN.get(), BurnData.create(durationTicks, (float) dotMultiplier, attackerId));
        victim.addEffect(new MobEffectInstance(TumultuMobEffects.BURNING, durationTicks, 0, false, true, true));
    }

    // apply shocked status
    public static void inflictShock(LivingEntity victim, int durationTicks) {
        victim.setData(TumultuAttachments.SHOCKED.get(), new ShockedData(durationTicks));
        victim.addEffect(new MobEffectInstance(TumultuMobEffects.SHOCKED, durationTicks, 0, false, true, true));
    }

    // devtools shenanigans, direct infliction of ele damage
    public static void inflictElementalDamage(LivingEntity victim, UUID attackerId, ElementKind element, float amount) {
        if (amount <= 0) return;

        ResourceKey<DamageType> damageType = switch (element) {
            case FIRE -> TumultuDamageTypes.FIRE;
            case COLD -> TumultuDamageTypes.COLD;
            case LIGHTNING -> TumultuDamageTypes.LIGHTNING;
        };
        PENDING_ELEMENTAL.computeIfAbsent(victim.getUUID(), id -> new ConcurrentLinkedQueue<>())
                .add(new PendingHit(damageType, amount, attackerId));

        Registry<AffixDefinition> registry = victim.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        RandomSource random = victim.getRandom();
        switch (element) {
            case FIRE -> applyFireExposure(victim, attackerId, amount, 0, 0, registry, random);
            case COLD -> applyColdFreeze(victim, amount);
            case LIGHTNING -> applyLightningExposure(victim, amount, 0, registry, random);
        }
    }
}
