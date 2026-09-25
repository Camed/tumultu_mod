package com.tumultu.combat;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.ChanceToFireAdditionalArrowEffect;
import com.tumultu.affix.effect.ProjectileSpeedEffect;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// ranged interactions modifiers
public class RangedEventHandler {
    private static final Set<UUID> OWN_DUPLICATES = ConcurrentHashMap.newKeySet();
    private static final float EXTRA_ARROW_SPREAD_DEGREES = 5.0f;

    @SubscribeEvent
    public static void onProjectileSpawned(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (OWN_DUPLICATES.remove(arrow.getUUID())) return;

        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) return;

        AffixData data = weapon.get(TumultuDataComponents.AFFIX_DATA.get());
        if (data == null || data.affixes().isEmpty()) return;

        Registry<AffixDefinition> registry = arrow.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY);

        double speedPercent = 0;
        double extraArrowChance = 0;
        for (RolledAffix rolled : data.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def == null) continue;
            if (def.effect() instanceof ProjectileSpeedEffect) speedPercent += rolled.rolledValue();
            else if (def.effect() instanceof ChanceToFireAdditionalArrowEffect) extraArrowChance += rolled.rolledValue();
        }

        if (speedPercent > 0) {
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(1 + speedPercent));
        }

        if (extraArrowChance > 0
                && arrow.level() instanceof ServerLevel serverLevel
                && arrow.getOwner() instanceof LivingEntity shooter
                && arrow.getRandom().nextDouble() < extraArrowChance) {
            spawnExtraArrow(serverLevel, arrow, shooter, weapon);
        }
    }

    private static void spawnExtraArrow(ServerLevel level, AbstractArrow original, LivingEntity shooter, ItemStack weapon) {
        Arrow duplicate = new Arrow(level, shooter, Items.ARROW.getDefaultInstance(), weapon);
        duplicate.setPos(original.getX(), original.getY(), original.getZ());
        duplicate.setBaseDamageFromMob(1.0F);
        duplicate.setCritArrow(original.isCritArrow());

        float spreadRadians = (float) Math.toRadians(EXTRA_ARROW_SPREAD_DEGREES);
        duplicate.setDeltaMovement(original.getDeltaMovement().yRot(spreadRadians));

        OWN_DUPLICATES.add(duplicate.getUUID());
        level.addFreshEntity(duplicate);
    }
}
