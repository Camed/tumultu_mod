package com.tumultu.combat;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.AffixEffect;
import com.tumultu.affix.effect.AllElementalResistanceEffect;
import com.tumultu.affix.effect.ArmorAppliesToElementalEffect;
import com.tumultu.affix.effect.AttributeEffect;
import com.tumultu.affix.effect.CannotBeBledEffect;
import com.tumultu.affix.effect.CannotBePoisonedEffect;
import com.tumultu.affix.effect.CannotBeWitheredEffect;
import com.tumultu.affix.effect.CritChanceEffect;
import com.tumultu.affix.effect.CritChanceMultiplierEffect;
import com.tumultu.affix.effect.CritDamageMultiplierEffect;
import com.tumultu.affix.effect.CritDamagePercentEffect;
import com.tumultu.affix.effect.CritDamageReductionEffect;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.affix.effect.ElementalPenetrationEffect;
import com.tumultu.affix.effect.ElementalResistanceEffect;
import com.tumultu.affix.effect.HybridArmorLifeEffect;
import com.tumultu.affix.effect.LifeRegenerationEffect;
import com.tumultu.affix.effect.PhysicalDamageReductionEffect;
import com.tumultu.affix.effect.ReversePoisonEffect;
import com.tumultu.affix.effect.ThornsMultiplierEffect;
import com.tumultu.registry.TumultuDataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;

// generally dynamic in-memory db used for various calculations within the mod
public class PlayerCombatStats {
    public static final double MAX_ELEMENTAL_RESISTANCE = 0.75;
    public static final double MAX_PHYSICAL_REDUCTION = 0.90;
    private static final double ARMOR_MITIGATION_PER_POINT = 0.04;
    private static final double MAX_ARMOR_MITIGATION_EQUIVALENT = 0.80;

     // Base curve steepness for {@link #armorDamageMultiplier}'s {@code x} - at 4.0 with 0
     // Toughness, 20 Armor gives ~44% reduction, 50 gives ~67%, 100 gives 80%. Raise to make Armor
     // mitigate harder per point everywhere; lower to flatten the curve everywhere.
    private static final double ARMOR_FORMULA_BASE_MULTIPLIER = 4.0;
    // How much each point of {@link Attributes#ARMOR_TOUGHNESS} adds to {@code x} - a full
    // unenchanted netherite set has 12 Toughness, adding 3.6 to x (4.0 -> 7.6), which at 20 Armor
    // raises reduction from ~44% to ~60%. Toughness has no cap of its own in vanilla, so this
    // keeps scaling right along with it.
    private static final double ARMOR_FORMULA_TOUGHNESS_MULTIPLIER = 0.3;
    // Hard ceiling on the FINAL physical damage multiplier (Armor curve * additional physical
    // reduction affixes combined) - see {@link #finalPhysicalDamageMultiplier}. Damage can never
    // be reduced by more than 95% overall, no matter how extreme the inputs. */
    public static final double MAX_FINAL_PHYSICAL_REDUCTION = 0.95;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static double elementalResistance(LivingEntity entity, ElementKind element, Registry<AffixDefinition> registry) {
        return Math.min(rawElementalResistance(entity, element, registry), MAX_ELEMENTAL_RESISTANCE);
    }

    // res is capped at 75%
    // todo: maybe player should be able to modify max res through future uniques/systems - keep an eye on that
    private static double rawElementalResistance(LivingEntity entity, ElementKind element, Registry<AffixDefinition> registry) {
        List<AffixData> armor = armorAffixData(entity);
        double direct = sumElementalResistance(allEquippedAffixData(entity), element, registry)
                + sumElementalResistance(curioAffixData(entity), element, registry);
        if (entity instanceof TieredCombatant mob) {
            direct += mob.elementalResistance(element);
        }

        double armorConversionPercent = sumArmorAppliesToElemental(armor, registry);
        double armorMitigationEquivalent = Math.min(entity.getAttributeValue(Attributes.ARMOR) * ARMOR_MITIGATION_PER_POINT, MAX_ARMOR_MITIGATION_EQUIVALENT);
        double converted = armorMitigationEquivalent * armorConversionPercent;

        return direct + converted;
    }

    public static double elementalResistanceOvercap(LivingEntity entity, ElementKind element, Registry<AffixDefinition> registry) {
        return Math.max(0, rawElementalResistance(entity, element, registry) - MAX_ELEMENTAL_RESISTANCE);
    }

    public static double physicalDamageReduction(LivingEntity entity, Registry<AffixDefinition> registry) {
        return Math.min(sumPhysicalDamageReduction(armorAffixData(entity), registry), MAX_PHYSICAL_REDUCTION);
    }

    public static double rawPhysicalDamageReduction(LivingEntity entity, Registry<AffixDefinition> registry) {
        return sumPhysicalDamageReduction(armorAffixData(entity), registry);
    }


    // armor reworked!
    public static double armorDamageMultiplier(LivingEntity entity, Registry<AffixDefinition> registry) {
        double armor = trueArmor(entity, registry);
        double toughness = entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double x = ARMOR_FORMULA_BASE_MULTIPLIER + ARMOR_FORMULA_TOUGHNESS_MULTIPLIER * toughness;
        return 100.0 / (100.0 + x * armor);
    }

    public static double trueArmor(LivingEntity entity, Registry<AffixDefinition> registry) {
        // tumultu mobs wear no armor items, so sumBaseArmorFromEquipment sees nothing for them -
        // their MobDefinition-driven Attributes.ARMOR base value (set once at spawn, never touched
        // by equipment) is their only source. Safe to add unconditionally so far since no
        // mob equips armor items to double-count against; todo: revisit if that ever changes.
        double baseArmor = entity instanceof TieredCombatant
                ? entity.getAttributeValue(Attributes.ARMOR)
                : sumBaseArmorFromEquipment(entity);
        double percentBonus = 0;
        for (RolledAffix rolled : allAffixes(armorAffixData(entity))) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) continue;
            AffixEffect effect = def.effect();
            if (effect instanceof AttributeEffect attributeEffect
                    && attributeEffect.attribute().equals(Attributes.ARMOR)
                    && attributeEffect.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                percentBonus += rolled.rolledValue();
            } else if (effect instanceof HybridArmorLifeEffect) {
                percentBonus += rolled.rolledValue();
            }
        }
        return baseArmor * (1 + percentBonus);
    }

    private static double sumBaseArmorFromEquipment(LivingEntity entity) {
        double total = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute().equals(Attributes.ARMOR) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                    total += entry.modifier().amount();
                }
            }
        }
        return total;
    }

    public static double finalPhysicalDamageMultiplier(LivingEntity entity, Registry<AffixDefinition> registry) {
        double multiplier = armorDamageMultiplier(entity, registry) * (1 - physicalDamageReduction(entity, registry));
        return Math.max(multiplier, 1 - MAX_FINAL_PHYSICAL_REDUCTION);
    }

    public static double rawFinalPhysicalDamageReduction(LivingEntity entity, Registry<AffixDefinition> registry) {
        return 1 - armorDamageMultiplier(entity, registry) * (1 - physicalDamageReduction(entity, registry));
    }

    public static boolean isImmuneToPoison(LivingEntity entity, Registry<AffixDefinition> registry) {
        return hasCannotBePoisoned(armorAffixData(entity), registry);
    }

    public static boolean isImmuneToBleed(LivingEntity entity, Registry<AffixDefinition> registry) {
        return hasEffect(armorAffixData(entity), registry, CannotBeBledEffect.class);
    }
    public static boolean isImmuneToWither(LivingEntity entity, Registry<AffixDefinition> registry) {
        return hasEffect(armorAffixData(entity), registry, CannotBeWitheredEffect.class);
    }
    public static boolean hasReversedPoison(LivingEntity entity, Registry<AffixDefinition> registry) {
        return hasEffect(armorAffixData(entity), registry, ReversePoisonEffect.class);
    }
    public static double thornsMultiplier(LivingEntity entity, Registry<AffixDefinition> registry) {
        return sumThornsMultiplier(armorAffixData(entity), registry);
    }
    public static double critChance(LivingEntity entity, Registry<AffixDefinition> registry) {
        List<AffixData> equipped = allEquippedAffixData(entity);
        double base = sumWhere(equipped, registry, CritChanceEffect.class);
        double multiplier = 1 + sumWhere(equipped, registry, CritChanceMultiplierEffect.class);
        return base * multiplier;
    }
    public static double critDamagePercent(LivingEntity entity, Registry<AffixDefinition> registry) {
        return sumWhere(allEquippedAffixData(entity), registry, CritDamagePercentEffect.class);
    }
    public static double critDamageMultiplier(LivingEntity entity, Registry<AffixDefinition> registry) {
        return 1 + sumWhere(allEquippedAffixData(entity), registry, CritDamageMultiplierEffect.class);
    }
    public static double critDamageReduction(LivingEntity entity, Registry<AffixDefinition> registry) {
        return sumCritDamageReduction(armorAffixData(entity), registry);
    }
    public static double lifeRegenerationPerSecond(LivingEntity entity, Registry<AffixDefinition> registry) {
        return sumWhere(armorAffixData(entity), registry, LifeRegenerationEffect.class)
                + sumWhere(curioAffixData(entity), registry, LifeRegenerationEffect.class);
    }
    public static double elementalPenetration(LivingEntity entity, Registry<AffixDefinition> registry) {
        return sumElementalPenetration(allEquippedAffixData(entity), registry);
    }

    // todo: update as IItemHandlerModifiable is marked as deprecated
    public static List<AffixData> curioAffixData(LivingEntity entity) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(entity);
        if (handler == null) {
            return List.of();
        }
        IItemHandlerModifiable equipped = handler.getEquippedCurios();
        List<AffixData> result = new ArrayList<>();
        for (int i = 0; i < equipped.getSlots(); i++) {
            AffixData data = affixDataOf(equipped.getStackInSlot(i));
            if (!data.affixes().isEmpty()) {
                result.add(data);
            }
        }
        return result;
    }

    private static List<AffixData> armorAffixData(LivingEntity entity) {
        return List.of(
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[0])),
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[1])),
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[2])),
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[3]))
        );
    }

    private static List<AffixData> allEquippedAffixData(LivingEntity entity) {
        return List.of(
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[0])),
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[1])),
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[2])),
                affixDataOf(entity.getItemBySlot(ARMOR_SLOTS[3])),
                affixDataOf(entity.getMainHandItem())
        );
    }

    private static AffixData affixDataOf(ItemStack stack) {
        AffixData data = stack.get(TumultuDataComponents.AFFIX_DATA.get());
        return data != null ? data : AffixData.EMPTY;
    }

    static double sumElementalResistance(List<AffixData> armor, ElementKind element, Registry<AffixDefinition> registry) {
        double total = 0;
        for (RolledAffix rolled : allAffixes(armor)) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) continue;
            AffixEffect effect = def.effect();
            if (effect instanceof ElementalResistanceEffect resistance && resistance.element() == element) {
                total += rolled.rolledValue();
            } else if (effect instanceof AllElementalResistanceEffect) {
                total += rolled.rolledValue();
            }
        }
        return total;
    }

    static double sumArmorAppliesToElemental(List<AffixData> armor, Registry<AffixDefinition> registry) {
        return sumWhere(armor, registry, ArmorAppliesToElementalEffect.class);
    }

    static double sumPhysicalDamageReduction(List<AffixData> armor, Registry<AffixDefinition> registry) {
        return sumWhere(armor, registry, PhysicalDamageReductionEffect.class);
    }

    static double sumThornsMultiplier(List<AffixData> armor, Registry<AffixDefinition> registry) {
        return sumWhere(armor, registry, ThornsMultiplierEffect.class);
    }

    static double sumElementalPenetration(List<AffixData> equipped, Registry<AffixDefinition> registry) {
        return sumWhere(equipped, registry, ElementalPenetrationEffect.class);
    }

    static double sumCritDamageReduction(List<AffixData> armor, Registry<AffixDefinition> registry) {
        return sumWhere(armor, registry, CritDamageReductionEffect.class);
    }

    static boolean hasCannotBePoisoned(List<AffixData> armor, Registry<AffixDefinition> registry) {
        return hasEffect(armor, registry, CannotBePoisonedEffect.class);
    }

    private static boolean hasEffect(List<AffixData> armor, Registry<AffixDefinition> registry, Class<? extends AffixEffect> effectType) {
        for (RolledAffix rolled : allAffixes(armor)) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null && effectType.isInstance(def.effect())) {
                return true;
            }
        }
        return false;
    }

    private static double sumWhere(List<AffixData> armor, Registry<AffixDefinition> registry, Class<? extends AffixEffect> effectType) {
        double total = 0;
        for (RolledAffix rolled : allAffixes(armor)) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null && effectType.isInstance(def.effect())) {
                total += rolled.rolledValue();
            }
        }
        return total;
    }

    private static List<RolledAffix> allAffixes(List<AffixData> armor) {
        return armor.stream().flatMap(data -> data.affixes().stream()).toList();
    }
}
