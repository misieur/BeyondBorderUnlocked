package com.maximde.beyondBorderUnlocked.mechanics.border;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class BorderManager {

    private static final double NEAR_BORDER = 0.5;

    /** 
     * Border crossing detection:
     * NEAR_BORDER is the distance threshold for border interaction (0.5 blocks)
     * Player velocity increases detection range up to 5x when moving fast
     * 
     * Teleport (behind border) conditions:
     * - Player is facing border (dot product > 0.5)
     * - Player is moving away from center
     * - Player is looking away from center
     */
    public void handleBorderCrossing(Player player, Location from, Location to) {

        BorderData borderData = calculateBorderData(player, to);

        if (!isNearBorder(borderData, player)) {
            return;
        }

        Vector borderVec = calculateBorderVector(borderData);

        if (shouldTeleport(player, from, to, borderVec, borderData.center())) {
            teleportPlayer(player);
        }
    }

    private BorderData calculateBorderData(Player player, Location to) {
        WorldBorder border = player.getWorld().getWorldBorder();
        Location center = border.getCenter();
        double halfSize = border.getSize() / 2.0;

        double[] borders = {
                center.getX() - halfSize,
                center.getX() + halfSize,
                center.getZ() - halfSize,
                center.getZ() + halfSize
        };

        double px = to.getX(), pz = to.getZ();
        double distX = Math.min(Math.abs(px - borders[0]), Math.abs(borders[1] - px));
        double distZ = Math.min(Math.abs(pz - borders[2]), Math.abs(borders[3] - pz));

        return new BorderData(borders, center, distX, distZ, px, pz);
    }

    private boolean isNearBorder(BorderData borderData, Player player) {
        return Math.min(borderData.distX(), borderData.distZ()) <=
                NEAR_BORDER * Math.max(1.0, player.getVelocity().lengthSquared() * 10);
    }

    private Vector calculateBorderVector(BorderData data) {
        return new Vector(
                data.distX() < data.distZ() ?
                        (Math.abs(data.px() - data.borders()[0]) < Math.abs(data.borders()[1] - data.px()) ?
                                data.borders()[0] - data.px() : data.borders()[1] - data.px()) : 0,
                0,
                data.distX() >= data.distZ() ?
                        (Math.abs(data.pz() - data.borders()[2]) < Math.abs(data.borders()[3] - data.pz()) ?
                                data.borders()[2] - data.pz() : data.borders()[3] - data.pz()) : 0
        );
    }

    private boolean shouldTeleport(Player player, Location from, Location to, Vector borderVec, Location center) {
        if (borderVec.length() <= 0) {
            return false;
        }

        Vector playerDir = player.getLocation().getDirection().setY(0).normalize();
        Vector centerDir = center.toVector().subtract(to.toVector()).setY(0).normalize();
        Vector moveDir = to.toVector().subtract(from.toVector()).normalize();

        return borderVec.normalize().dot(playerDir) > 0.5
                && centerDir.dot(playerDir) < 0
                && centerDir.dot(moveDir) < 0;
    }

    private void teleportPlayer(Player player) {
        double teleportDistance = 1 + NEAR_BORDER * Math.max(1.0, player.getVelocity().lengthSquared() * 5);
        Vector direction = player.getLocation().getDirection();
        direction.multiply(teleportDistance);

        Location newLocation = player.getLocation().clone().add(direction.getX(), 0, direction.getZ());
        player.teleport(newLocation);
    }
}