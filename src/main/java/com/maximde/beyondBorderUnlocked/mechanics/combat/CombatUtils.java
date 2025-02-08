package com.maximde.beyondBorderUnlocked.mechanics.combat;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CombatUtils {
    private static final double CRITICAL_ANGLE = Math.PI / 6;
    private static final double ENTITY_HEIGHT_MULTIPLIER = 0.75;

    /**
     * Critical hit detection based on:
     * - Player in air
     * - Looking angle at target
     * - Target height consideration
     */
    public static boolean isCriticalHit(Player player, LivingEntity target) {
        if (player.isOnGround() || player.getVelocity().getY() >= 0) {
            return false;
        }

        Vector playerLook = player.getLocation().getDirection();
        Vector toEntity = calculateEntityVector(player, target);

        return playerLook.angle(toEntity) < CRITICAL_ANGLE;
    }

    private static Vector calculateEntityVector(Player player, LivingEntity target) {
        return target.getLocation()
                .add(0, target.getHeight() * ENTITY_HEIGHT_MULTIPLIER, 0)
                .subtract(player.getEyeLocation())
                .toVector()
                .normalize();
    }
}