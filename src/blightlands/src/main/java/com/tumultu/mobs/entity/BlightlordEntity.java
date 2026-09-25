package com.tumultu.mobs.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

// blightlands biome boss
public class BlightlordEntity extends TumultuBruteMonster {
    private static final int MIN_RANGED_ATTACK_COOLDOWN_TICKS = 100;
    private static final int RANGED_ATTACK_COOLDOWN_JITTER_TICKS = 20;

    private int rangedAttackCooldown;

    public BlightlordEntity(EntityType<? extends TumultuBruteMonster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new BlightlordRangedAttackGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.rangedAttackCooldown > 0) {
            this.rangedAttackCooldown--;
        }
    }

    boolean isRangedAttackReady() {
        return this.rangedAttackCooldown <= 0;
    }

    void performRangedAttack(LivingEntity target) {
        this.swing(InteractionHand.MAIN_HAND, true);
        this.shootArrowAt(target);
        this.rangedAttackCooldown = MIN_RANGED_ATTACK_COOLDOWN_TICKS + this.random.nextInt(RANGED_ATTACK_COOLDOWN_JITTER_TICKS + 1);
    }


    private void shootArrowAt(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack ammo = new ItemStack(Items.ARROW);
        ItemStack weapon = new ItemStack(Items.BOW);
        Holder<Enchantment> punch = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PUNCH);
        weapon.enchant(punch, 2); // we hate three block stacking!

        Arrow arrow = new Arrow(serverLevel, this, ammo, weapon);
        arrow.setBaseDamage(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        Projectile.spawnProjectileUsingShoot(arrow, serverLevel, ammo, dx, dy + horizontalDistance * 0.2, dz, 1.6F, 6.0F);

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
