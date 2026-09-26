package com.tumultu.affix;

import com.tumultu.affix.effect.AttributeEffect;
import com.tumultu.affix.effect.HybridArmorLifeEffect;
import com.tumultu.registry.TumultuRegistries;
import com.tumultu.registry.TumultuTags;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.Nullable;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosDataComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * Projects a rolled {@link AffixData} onto the actual gameplay-visible parts of an ItemStack:
 * the vanilla attribute modifiers that make numbers real, and the item's displayed name/color.
 */
public class AffixEffectApplier {

    public static void apply(ItemStack stack, AffixData data, RegistryAccess registryAccess, RandomSource random) {
        Registry<AffixDefinition> registry = registryAccess.lookupOrThrow(TumultuRegistries.AFFIX_KEY);
        applyAttributeModifiers(stack, data, registry);
        applyDisplayName(stack, data, registry, random);
    }

    private static void applyAttributeModifiers(ItemStack stack, AffixData data, Registry<AffixDefinition> registry) {
        // apply only actually equipped item stats
        Equippable equippable = stack.getItem().components().get(DataComponents.EQUIPPABLE);
        EquipmentSlotGroup slotGroup = equippable != null
                ? EquipmentSlotGroup.bySlot(equippable.slot())
                : EquipmentSlotGroup.MAINHAND;

        ItemAttributeModifiers base = stack.getItem().components()
                .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        base.modifiers().forEach(entry ->
                builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display()));

        // Here we go with dependecies: Curios API
        // Rings/amulets don't occupy any real EquipmentSlotGroup - vanilla's own
        // component above never applies to them regardless of what slotGroup is picked, so their
        // affixes need Curios' own attribute-modifiers component, which Curios checks
        // whenever the item is actually worn in one of its slots instead.
        boolean isCurio = stack.is(TumultuTags.CURIOS);
        CurioAttributeModifiers.Builder curioBuilder = isCurio ? CurioAttributeModifiers.builder() : null;

        for (RolledAffix rolled : data.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) {
                continue;
            }
            // Hidden below since AffixTooltipAppender already shows every affix (name + value +
            // roll range) - letting vanilla also list the raw number would just duplicate it.
            if (def.effect() instanceof AttributeEffect attributeEffect) {
                AttributeModifier modifier = new AttributeModifier(rolled.affixId(), rolled.rolledValue(), attributeEffect.operation());
                builder.add(attributeEffect.attribute(), modifier, slotGroup, ItemAttributeModifiers.Display.hidden());
                if (curioBuilder != null) {
                    curioBuilder.addModifier(attributeEffect.attribute(), modifier);
                }
            } else if (def.effect() instanceof HybridArmorLifeEffect hybrid) {
                AttributeModifier armorModifier = new AttributeModifier(rolled.affixId(), rolled.rolledValue(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                builder.add(Attributes.ARMOR, armorModifier, slotGroup, ItemAttributeModifiers.Display.hidden());

                AttributeModifier lifeModifier = new AttributeModifier(rolled.affixId(), hybrid.lifeFor(rolled.rolledValue()), AttributeModifier.Operation.ADD_VALUE);
                builder.add(Attributes.MAX_HEALTH, lifeModifier, slotGroup, ItemAttributeModifiers.Display.hidden());
                if (curioBuilder != null) {
                    curioBuilder.addModifier(Attributes.ARMOR, armorModifier);
                    curioBuilder.addModifier(Attributes.MAX_HEALTH, lifeModifier);
                }
            }
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        if (curioBuilder != null) {
            stack.set(CuriosDataComponents.ATTRIBUTE_MODIFIERS, curioBuilder.build());
        }
    }

    private static void applyDisplayName(ItemStack stack, AffixData data, Registry<AffixDefinition> registry, RandomSource random) {
        if (data.rarity() == ItemRarity.NORMAL) {
            stack.remove(DataComponents.CUSTOM_NAME);
            return;
        }

        Component baseName = Component.translatable(stack.getItem().getDescriptionId());

        AffixDefinition bestPrefix = pickBest(data.affixes(), registry, AffixType.PREFIX, random);
        AffixDefinition bestSuffix = pickBest(data.affixes(), registry, AffixType.SUFFIX, random);

        MutableComponent name = Component.empty();
        if (bestPrefix != null) {
            name.append(bestPrefix.displayName()).append(" ");
        }
        name.append(baseName);
        if (bestSuffix != null) {
            name.append(" ").append(bestSuffix.displayName());
        }

        stack.set(DataComponents.CUSTOM_NAME, name.withStyle(data.rarity().color()));
    }

    private static @Nullable AffixDefinition pickBest(List<RolledAffix> affixes, Registry<AffixDefinition> registry, AffixType type, RandomSource random) {
        record Candidate(AffixDefinition def, int tier) {}

        List<Candidate> matching = new ArrayList<>();
        for (RolledAffix rolled : affixes) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null && def.type() == type) {
                matching.add(new Candidate(def, rolled.tierIndex()));
            }
        }
        if (matching.isEmpty()) {
            return null;
        }

        int highest = matching.stream().mapToInt(Candidate::tier).max().orElseThrow();
        List<AffixDefinition> topTier = matching.stream()
                .filter(c -> c.tier() == highest)
                .map(Candidate::def)
                .toList();
        return topTier.get(random.nextInt(topTier.size()));
    }
}