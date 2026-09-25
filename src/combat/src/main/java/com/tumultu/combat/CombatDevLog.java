package com.tumultu.combat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// debug func -> available for cheats enabled - so clearly creative or development purposes
public final class CombatDevLog {

    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

    private CombatDevLog() {
    }

    public static boolean toggle(ServerPlayer player) {
        UUID id = player.getUUID();
        if (ENABLED.remove(id)) {
            return false;
        }
        ENABLED.add(id);
        return true;
    }

    public static boolean isEnabled(ServerPlayer player) {
        return ENABLED.contains(player.getUUID());
    }

    public static void log(LivingEntity attacker, String message) {
        if (!(attacker instanceof ServerPlayer player) || !isEnabled(player)) {
            return;
        }
        player.sendSystemMessage(Component.literal("[DevLog] " + message));
    }
}
