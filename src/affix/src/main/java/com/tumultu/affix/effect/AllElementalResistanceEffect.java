package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** "X% to all Elemental Resistances" as ONE stat/ONE roll, rather than three separate {@link
 * ElementalResistanceEffect} entries (fire/cold/lightning) that would drift apart from each other
 * the first time something rerolls their values independently. {@link
 * com.tumultu.combat.PlayerCombatStats#sumElementalResistance} adds this to whichever
 * element is being queried, regardless of which one. Introduced for Pillager's Favour (mimicking
 * PoE's "Le Heup of All"'s "% to all Elemental Resistances" line), but a genuinely reusable stat. */
public record AllElementalResistanceEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<AllElementalResistanceEffect> MAP_CODEC = MapCodec.unit(() -> new AllElementalResistanceEffect(true));

    @Override
    public String typeId() {
        return "all_elemental_resistance";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("%+.0f%% to all Elemental Resistances", rolledValue * 100.0));
    }
}
