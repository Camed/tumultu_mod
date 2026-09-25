package com.tumultu.combat;

import com.tumultu.affix.effect.ElementKind;

/**
 * What {@link PlayerCombatStats} needs from a Tumultu-authored mob (currently just
 * {@code AbstractTumultuMonster}, in the separate blightlands domain) without depending on that
 * concrete class directly - combat and blightlands each need something from the other
 * ({@code CombatEventHandler.inflictPoison}, this interface's own resistance value), and a plain
 * class dependency both ways would be a circular module dependency. Implementing this interface is
 * enough for a mob to plug into {@code trueArmor}/{@code rawElementalResistance}'s
 * {@code instanceof} checks.
 */
public interface TieredCombatant {
    double elementalResistance(ElementKind kind);
}
