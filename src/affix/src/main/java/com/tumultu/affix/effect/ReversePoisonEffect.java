package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** A binary flag, same shape as {@link CannotBePoisonedEffect} - present or not, the rolled value
 * itself is unused - but the opposite reaction: instead of blocking Poison damage entirely, it
 * redirects that damage into healing of the same amount. See {@code
 * CombatEventHandler#onIncomingDamage}'s own interception at the very top of the method (before
 * poison would otherwise hit {@code isDotFollowUpDamage}'s early return and land unmodified).
 * Covers both this mod's own {@code TumultuDamageTypes.POISON} AND vanilla's Poison effect - the
 * latter is reliably identifiable because NeoForge itself patches {@code PoisonMobEffect} to deal
 * its damage as {@code NeoForgeMod.POISON_DAMAGE} specifically so mods can differentiate it from
 * generic magic damage, so no "current status effect" guesswork is needed. */
public record ReversePoisonEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<ReversePoisonEffect> MAP_CODEC = MapCodec.unit(() -> new ReversePoisonEffect(false));

    @Override
    public String typeId() {
        return "reverse_poison";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal("Effects of Poison on you are now reversed");
    }
}
