package com.tumultu.combat;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.AffixType;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.AffixEffect;
import com.tumultu.affix.effect.ArmorAppliesToElementalEffect;
import com.tumultu.affix.effect.CannotBePoisonedEffect;
import com.tumultu.affix.effect.CritDamageReductionEffect;
import com.tumultu.affix.effect.ElementKind;
import com.tumultu.affix.effect.ElementalPenetrationEffect;
import com.tumultu.affix.effect.ElementalResistanceEffect;
import com.tumultu.affix.effect.PhysicalDamageReductionEffect;
import com.tumultu.affix.effect.ThornsMultiplierEffect;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerCombatStatsTest {

    @Mock
    private Registry<AffixDefinition> registry;

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("tumultu", path);
    }

    private static AffixDefinition def(AffixEffect effect) {
        return new AffixDefinition(AffixType.PREFIX, "Test", effect, List.of(), 100, "g", List.of());
    }

    private static AffixData singleAffix(Identifier affixId, double value) {
        return new AffixData(ItemRarity.MAGIC, List.of(new RolledAffix(affixId, 0, value)), false);
    }

    @Test
    void sumElementalResistanceSumsAcrossAllArmorPieces() {
        Identifier helmetAffix = id("fireproof_helmet");
        Identifier chestAffix = id("fireproof_chest");
        when(registry.getValue(helmetAffix)).thenReturn(def(new ElementalResistanceEffect(ElementKind.FIRE, true)));
        when(registry.getValue(chestAffix)).thenReturn(def(new ElementalResistanceEffect(ElementKind.FIRE, true)));

        double total = PlayerCombatStats.sumElementalResistance(
                List.of(singleAffix(helmetAffix, 0.10), singleAffix(chestAffix, 0.15), AffixData.EMPTY, AffixData.EMPTY),
                ElementKind.FIRE, registry);

        assertEquals(0.25, total, 0.0001);
    }

    @Test
    void sumElementalResistanceIgnoresADifferentElement() {
        Identifier coldAffix = id("insulated_helmet");
        when(registry.getValue(coldAffix)).thenReturn(def(new ElementalResistanceEffect(ElementKind.COLD, true)));

        double fireTotal = PlayerCombatStats.sumElementalResistance(
                List.of(singleAffix(coldAffix, 0.20), AffixData.EMPTY, AffixData.EMPTY, AffixData.EMPTY),
                ElementKind.FIRE, registry);

        assertEquals(0.0, fireTotal, 0.0001);
    }

    @Test
    void sumElementalResistanceIsUncappedAtThisLayer() {
        Identifier a = id("a");
        Identifier b = id("b");
        Identifier c = id("c");
        lenient().when(registry.getValue(a)).thenReturn(def(new ElementalResistanceEffect(ElementKind.FIRE, true)));
        lenient().when(registry.getValue(b)).thenReturn(def(new ElementalResistanceEffect(ElementKind.FIRE, true)));
        lenient().when(registry.getValue(c)).thenReturn(def(new ElementalResistanceEffect(ElementKind.FIRE, true)));

        double total = PlayerCombatStats.sumElementalResistance(
                List.of(singleAffix(a, 0.30), singleAffix(b, 0.30), singleAffix(c, 0.30), AffixData.EMPTY),
                ElementKind.FIRE, registry);

        assertEquals(0.90, total, 0.0001, "capping to MAX_ELEMENTAL_RESISTANCE happens in the wrapper");
    }

    @Test
    void sumArmorAppliesToElementalSumsAcrossPieces() {
        Identifier a = id("a");
        when(registry.getValue(a)).thenReturn(def(new ArmorAppliesToElementalEffect(true)));

        double total = PlayerCombatStats.sumArmorAppliesToElemental(
                List.of(singleAffix(a, 0.25), AffixData.EMPTY, AffixData.EMPTY, AffixData.EMPTY), registry);

        assertEquals(0.25, total, 0.0001);
    }

    @Test
    void sumPhysicalDamageReductionSumsAcrossPieces() {
        Identifier a = id("a");
        when(registry.getValue(a)).thenReturn(def(new PhysicalDamageReductionEffect(true)));

        double total = PlayerCombatStats.sumPhysicalDamageReduction(
                List.of(AffixData.EMPTY, singleAffix(a, 0.08), AffixData.EMPTY, AffixData.EMPTY), registry);

        assertEquals(0.08, total, 0.0001);
    }

    @Test
    void sumThornsMultiplierSumsAcrossPieces() {
        Identifier a = id("a");
        Identifier b = id("b");
        when(registry.getValue(a)).thenReturn(def(new ThornsMultiplierEffect(true)));
        when(registry.getValue(b)).thenReturn(def(new ThornsMultiplierEffect(true)));

        double total = PlayerCombatStats.sumThornsMultiplier(
                List.of(singleAffix(a, 0.20), AffixData.EMPTY, singleAffix(b, 0.15), AffixData.EMPTY), registry);

        assertEquals(0.35, total, 0.0001);
    }

    @Test
    void sumElementalPenetrationSumsAcrossEquippedGear() {
        Identifier a = id("a");
        Identifier b = id("b");
        when(registry.getValue(a)).thenReturn(def(new ElementalPenetrationEffect(true)));
        when(registry.getValue(b)).thenReturn(def(new ElementalPenetrationEffect(true)));

        double total = PlayerCombatStats.sumElementalPenetration(
                List.of(singleAffix(a, 0.20), AffixData.EMPTY, AffixData.EMPTY, AffixData.EMPTY, singleAffix(b, 0.15)), registry);

        assertEquals(0.35, total, 0.0001, "penetration sums across every equipped slot passed in, not just one");
    }

    @Test
    void sumCritDamageReductionSumsAcrossArmorPieces() {
        Identifier chest = id("chest");
        Identifier legs = id("legs");
        when(registry.getValue(chest)).thenReturn(def(new CritDamageReductionEffect(true)));
        when(registry.getValue(legs)).thenReturn(def(new CritDamageReductionEffect(true)));

        double total = PlayerCombatStats.sumCritDamageReduction(
                List.of(AffixData.EMPTY, singleAffix(chest, 0.30), singleAffix(legs, 0.20), AffixData.EMPTY), registry);

        assertEquals(0.50, total, 0.0001);
    }

    @Test
    void hasCannotBePoisonedIsTrueWhenAnyPieceHasIt() {
        Identifier legAffix = id("immunity");
        when(registry.getValue(legAffix)).thenReturn(def(new CannotBePoisonedEffect(false)));

        boolean immune = PlayerCombatStats.hasCannotBePoisoned(
                List.of(AffixData.EMPTY, AffixData.EMPTY, singleAffix(legAffix, 1.0), AffixData.EMPTY), registry);

        assertTrue(immune);
    }

    @Test
    void hasCannotBePoisonedIsFalseWithoutIt() {
        boolean immune = PlayerCombatStats.hasCannotBePoisoned(
                List.of(AffixData.EMPTY, AffixData.EMPTY, AffixData.EMPTY, AffixData.EMPTY), registry);

        assertFalse(immune);
    }
}
