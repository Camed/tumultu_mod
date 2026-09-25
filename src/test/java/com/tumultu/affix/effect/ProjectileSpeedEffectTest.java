package com.tumultu.affix.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectileSpeedEffectTest {

    @Test
    void typeIdIsProjectileSpeedPercent() {
        assertEquals("projectile_speed_percent", new ProjectileSpeedEffect(true).typeId());
    }

    @Test
    void describeFormatsAsAPercentage() {
        assertEquals("+20% Projectile Speed", new ProjectileSpeedEffect(true).describe(0.20).getString());
    }
}
