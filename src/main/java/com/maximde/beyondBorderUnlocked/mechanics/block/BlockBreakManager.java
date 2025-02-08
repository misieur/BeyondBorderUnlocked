package com.maximde.beyondBorderUnlocked.mechanics.block;

import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.maximde.beyondBorderUnlocked.BeyondBorderUnlocked;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class BlockBreakManager {
    private static final double BREAK_RANGE = 64.0;
    private static final int PARTICLE_COUNT = 100;
    private static final double PARTICLE_OFFSET = 0.5;

    private final BeyondBorderUnlocked plugin;
    private final Set<Location> breakingBlocks = new HashSet<>();


    public void startBreaking(Player player, Block block, ItemStack tool) {
        Location blockLocation = block.getLocation();
        if (breakingBlocks.contains(blockLocation)) {
            return;
        }

        if(player.getGameMode() == GameMode.CREATIVE) {
            handleInstantBreak(player, block);
            return;
        }

        boolean isUnbreakable = isUnbreakableBlock(block, player);
        if (!isUnbreakable) {
            breakingBlocks.add(blockLocation);
        }

        float breakSpeed = BreakSpeedCalculator.calculateBreakSpeed(tool, block);
        int ticksBetweenStages = Math.max(0, Math.round(20 / breakSpeed));

        if (ticksBetweenStages < 1) {
            handleInstantBreak(player, block);
            return;
        }

        startBreakingAnimation(player, block, isUnbreakable, ticksBetweenStages);
    }

    private void handleInstantBreak(Player player, Block block) {
        spawnBreakEffects(block);
        block.breakNaturally();
        cleanupBlockBreak(player, block);
    }

    private void startBreakingAnimation(Player player, Block block, boolean isUnbreakable, int ticksBetweenStages) {
        Location blockLocation = block.getLocation();
        BukkitTask armSwingTask = createArmSwingTask(player, block);

        new BukkitRunnable() {
            int stage = 0;

            @Override
            public void run() {
                if (!isPlayerStillBreaking(player, block)) {
                    handleBreakingStop(blockLocation, block, isUnbreakable, armSwingTask);
                    cancel();
                    return;
                }

                if (stage > 9 && !isUnbreakable) {
                    completeBlockBreak(player, block, armSwingTask);
                    cancel();
                    return;
                }

                if (!isUnbreakable) {
                    broadcastBreakStage(blockLocation, block, stage++);
                }
            }
        }.runTaskTimer(plugin, 0L, ticksBetweenStages);
    }

    private BukkitTask createArmSwingTask(Player player, Block block) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                player.swingMainHand();
                block.getWorld().playSound(
                        block.getLocation(),
                        block.getBlockData().getSoundGroup().getHitSound(),
                        1.0F,
                        1.0F
                );
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private boolean isPlayerStillBreaking(Player player, Block block) {
        Block lookingAt = player.getTargetBlockExact(5);
        return lookingAt != null && lookingAt.equals(block);
    }

    private void handleBreakingStop(Location location, Block block, boolean isUnbreakable, BukkitTask armSwingTask) {
        if (!isUnbreakable) {
            broadcastBreakStage(location, block, -1);
            breakingBlocks.remove(location);
        }
        armSwingTask.cancel();
    }

    private void completeBlockBreak(Player player, Block block, BukkitTask armSwingTask) {
        spawnBreakEffects(block);
        block.breakNaturally(player.getInventory().getItemInMainHand());
        cleanupBlockBreak(player, block);
        armSwingTask.cancel();
    }

    private void spawnBreakEffects(Block block) {
        Location particleLocation = block.getLocation().clone().add(PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET);
        block.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                particleLocation,
                PARTICLE_COUNT,
                block.getBlockData()
        );
        block.getWorld().playSound(
                block.getLocation(),
                block.getBlockData().getSoundGroup().getBreakSound(),
                1.0f,
                1.0f
        );
    }

    private void cleanupBlockBreak(Player player, Block block) {
        breakingBlocks.remove(block.getLocation());
        plugin.removeOutline(player.getUniqueId());
        plugin.getLastOutlinedBlock().remove(player.getUniqueId());
    }

    /*
     * 0-9: Progressive breaking states
     * -1: Reset breaking state
     */
    private void broadcastBreakStage(Location location, Block block, int stage) {
        WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation(
                BlockUtils.calculateBlockEntityId(block),
                new Vector3i((int)location.getX(), (int)location.getY(), (int)location.getZ()),
                (byte) stage
        );

        location.getWorld().getPlayers().stream()
                .filter(p -> BlockUtils.isWithinRange(p.getLocation(), location, BREAK_RANGE))
                .forEach(p -> plugin.getPlayerManager().sendPacket(p, packet));
    }

    private boolean isUnbreakableBlock(Block block, Player player) {
        return block.getType().getHardness() < 0 && player.getGameMode() == GameMode.SURVIVAL;
    }
}