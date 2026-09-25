package com.tumultu.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatEventHandlerCritMultiplierTest {

    @Test
    void nonCritIsAlwaysOne() {
        assertEquals(1.0, CombatEventHandler.critMultiplier(false, 0.50, 0.0, 1.0), 0.0001);
        assertEquals(1.0, CombatEventHandler.critMultiplier(false, 0.0, 0.0, 1.0), 0.0001);
    }

    @Test
    void baseCritWithNoModifiersIsDouble() {
        assertEquals(2.0, CombatEventHandler.critMultiplier(true, 0.0, 0.0, 1.0), 0.0001);
    }

    @Test
    void reductionSubtractsDirectlyFromTheBonus() {
        // 200% crit multiplier (base 1.0 bonus) reduced by a T1 0.50 roll becomes 150%.
        assertEquals(1.5, CombatEventHandler.critMultiplier(true, 0.0, 0.50, 1.0), 0.0001);
    }

    @Test
    void reductionAppliesAfterCritDamagePercentIsAdded() {
        // 1.0 base + 0.5 crit_damage_percent = 1.5 bonus, minus 0.3 reduction = 1.2 bonus -> 2.2x.
        assertEquals(2.2, CombatEventHandler.critMultiplier(true, 0.5, 0.3, 1.0), 0.0001);
    }

    @Test
    void reductionCannotPushTheMultiplierBelowOne() {
        // A reduction far larger than the bonus should floor at 1.0x, never go negative.
        assertEquals(1.0, CombatEventHandler.critMultiplier(true, 0.0, 5.0, 1.0), 0.0001);
    }

    @Test
    void bonusMultiplierScalesTheCombinedBaseAndPercentBeforeReduction() {
        // Crown of Stability: -50% more (multiplier 0.5) halves (1.0 base + 0.5 percent) = 0.75
        // bonus -> 1.75x, applied BEFORE the (still-zero here) victim reduction subtracts.
        assertEquals(1.75, CombatEventHandler.critMultiplier(true, 0.5, 0.0, 0.5), 0.0001);
    }

    @Test
    void bonusMultiplierAppliesBeforeVictimReductionSubtracts() {
        // (1.0 base) * 0.5 multiplier = 0.5, minus 0.3 reduction = 0.2 bonus -> 1.2x - the
        // reduction is subtracted from the ALREADY-scaled bonus, not scaled itself.
        assertEquals(1.2, CombatEventHandler.critMultiplier(true, 0.0, 0.3, 0.5), 0.0001);
    }

    @Test
    void bonusMultiplierCanAlsoIncreaseTheBonus() {
        // A "more" multiplier above 1.0 scales the bonus up, not just down.
        assertEquals(3.0, CombatEventHandler.critMultiplier(true, 0.0, 0.0, 2.0), 0.0001);
    }
}
