package com.tumultu.affix.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

/** Adds a percentage of the wielder's CURRENT max health (read live at hit time, not snapshotted
 * when the affix was rolled) as flat physical damage - see {@code TumultuUniqueItems.DEVOURER}. */
public record MaxHealthToPhysicalDamagePercentEffect(boolean isPercentage) implements AffixEffect {
    public static final MapCodec<MaxHealthToPhysicalDamagePercentEffect> MAP_CODEC =
            MapCodec.unit(() -> new MaxHealthToPhysicalDamagePercentEffect(true));

    @Override
    public String typeId() {
        return "max_health_to_physical_damage_percent";
    }

    @Override
    public Component describe(double rolledValue) {
        return Component.literal(String.format("Converts %.0f%% of Maximum Life to Physical Damage", rolledValue * 100.0));
    }
}
