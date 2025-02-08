package com.maximde.beyondBorderUnlocked.mechanics.outline;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.maximde.beyondBorderUnlocked.BeyondBorderUnlocked;
import com.maximde.beyondBorderUnlocked.utils.Config;
import lombok.RequiredArgsConstructor;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class BlockOutlineManager {

    private final BeyondBorderUnlocked beyondBorderUnlocked;

    /**
     * Creates 12 item display entities to form a 3D outline
     * Each segment represents an edge of the block
     * 
     * Outline updates:
     * Only updates when targeting a different block or outline is missing
     * Removes old outline before creating new one to prevent ghosting
     */
    public void updateOutline(Player player) {
        if (!shouldShowOutline(player)) {
            beyondBorderUnlocked.removeOutline(player.getUniqueId());
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (!shouldUpdateOutline(player.getUniqueId(), targetBlock)) {
            return;
        }

        beyondBorderUnlocked.removeOutline(player.getUniqueId());

        createNewOutline(player, targetBlock);
    }

    private boolean shouldShowOutline(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        return targetBlock != null
                && !targetBlock.isLiquid()
                && targetBlock.getType() != Material.AIR
                && player.getGameMode() != GameMode.ADVENTURE
                && !player.getWorld().getWorldBorder().isInside(player.getLocation())
                && beyondBorderUnlocked.getPluginConfig().isBlockOutlineEnabled();
    }

    private boolean shouldUpdateOutline(UUID playerId, Block targetBlock) {
        if (!beyondBorderUnlocked.getLastOutlinedBlock().containsKey(playerId)) {
            return true;
        }

        Location lastLoc = beyondBorderUnlocked.getLastOutlinedBlock().get(playerId);
        return !(lastLoc.getBlockX() == targetBlock.getX()
                && lastLoc.getBlockY() == targetBlock.getY()
                && lastLoc.getBlockZ() == targetBlock.getZ()
                && beyondBorderUnlocked.getPlayerOutlines().containsKey(playerId));
    }

    private void createNewOutline(Player player, Block targetBlock) {
        removeOutline(player.getUniqueId());
        beyondBorderUnlocked.getLastOutlinedBlock().put(player.getUniqueId(), targetBlock.getLocation());

        Config config = beyondBorderUnlocked.getPluginConfig();
        List<OutlineSegment> segments = config.getSegments();

        int baseEntityId = ThreadLocalRandom.current().nextInt(4000, Integer.MAX_VALUE);
        List<Integer> entityIds = new ArrayList<>();

        segments.forEach(segment -> {
            int entityId = baseEntityId + entityIds.size();
            entityIds.add(entityId);
            spawnOutlineEntity(player, targetBlock, entityId, segment, config);
        });

        beyondBorderUnlocked.getPlayerOutlines().put(player.getUniqueId(), entityIds);
    }

    private void spawnOutlineEntity(Player player, Block targetBlock, int entityId, OutlineSegment segment, Config config) {
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(UUID.randomUUID()),
                EntityTypes.ITEM_DISPLAY,
                new Vector3d(targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5),
                0f, 0f, 0f, 0,
                Optional.empty()
        );

        ItemDisplayMeta meta = (ItemDisplayMeta) EntityMeta.createMeta(entityId, EntityTypes.ITEM_DISPLAY);
        meta.setScale(segment.scale());
        meta.setTranslation(segment.translation().subtract(0.5F, 0.5F, 0.5F));
        meta.setItem(new ItemStack.Builder()
                .type(config.getBlockOutlineBlock())
                .build());

        beyondBorderUnlocked.getPlayerManager().sendPacket(player, packet);
        beyondBorderUnlocked.getPlayerManager().sendPacket(player, meta.createPacket());
    }

    public void removeOutline(UUID playerId) {
        if (beyondBorderUnlocked.getPlayerOutlines().containsKey(playerId)) {
            beyondBorderUnlocked.getPlayerOutlines().remove(playerId);
        }
    }
}