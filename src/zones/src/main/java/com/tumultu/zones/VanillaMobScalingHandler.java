package com.tumultu.zones;

import com.tumultu.TumultuMod;
import com.tumultu.mobs.entity.AbstractTumultuMonster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class VanillaMobScalingHandler {
    private static final Identifier HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "world_tier_scaling_health");
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "world_tier_scaling_damage");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath(TumultuMod.MOD_ID, "world_tier_scaling_armor");

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) return;
        if (!(event.getEntity() instanceof Monster monster) || monster instanceof AbstractTumultuMonster) return;

        BlockPos pos = monster.blockPosition();
        int worldTier = WorldTier.fromPosition(pos.getX(), pos.getZ());
        Holder<Biome> biome = monster.level().getBiome(pos);
        int effectiveTier = Math.min(worldTier + ZoneBiomeTags.constantFor(biome), WorldTier.MAX);
        double multiplier = WorldTierScalingReloadListener.current().multiplierFor(effectiveTier);
        double bonus = multiplier - 1.0;
        if (bonus == 0.0) return;

        applyModifier(monster, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, bonus);
        applyModifier(monster, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, bonus);
        applyModifier(monster, Attributes.ARMOR, ARMOR_MODIFIER_ID, bonus);
        monster.setHealth(monster.getMaxHealth());
    }

    private static void applyModifier(LivingEntity entity, Holder<Attribute> attribute, Identifier id, double bonus) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        instance.addOrUpdateTransientModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
