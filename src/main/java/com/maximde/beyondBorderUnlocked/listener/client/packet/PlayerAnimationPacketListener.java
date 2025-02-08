package com.maximde.beyondBorderUnlocked.listener.client.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.maximde.beyondBorderUnlocked.BeyondBorderUnlocked;
import com.maximde.beyondBorderUnlocked.mechanics.block.BlockBreakManager;
import com.maximde.beyondBorderUnlocked.mechanics.block.BlockPlaceManager;
import com.maximde.beyondBorderUnlocked.events.BlockBreakBorderEvent;
import com.maximde.beyondBorderUnlocked.events.BlockPlaceBorderEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PlayerAnimationPacketListener implements PacketListener {
    private static final int MAX_REACH = 5;

    private final BeyondBorderUnlocked plugin;
    private final BlockBreakManager blockBreakManager;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isValidAnimationPacket(event)) {
            return;
        }

        Player player = Bukkit.getPlayer(event.getUser().getName());
        if (!isValidPlayer(player)) {
            return;
        }

        if (isInsideBorder(player)) {
            return;
        }

        Block targetBlock = player.getTargetBlockExact(MAX_REACH);
        if (targetBlock == null) {
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        boolean isBlock = handItem.getType().isBlock();

        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleBlockInteraction(player, targetBlock, handItem, isBlock));
    }

    private boolean isValidAnimationPacket(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.ANIMATION;
    }

    private boolean isValidPlayer(Player player) {
        return player != null && player.getGameMode() != GameMode.ADVENTURE;
    }

    private boolean isInsideBorder(Player player) {
        return player.getWorld().getWorldBorder().isInside(player.getLocation());
    }

    private void handleBlockInteraction(Player player, Block targetBlock, ItemStack handItem, boolean isBlock) {
        if (shouldHandleBreaking(handItem, isBlock, player)) {
            handleBlockBreaking(player, targetBlock);
        } else if (shouldHandleBuilding(isBlock)) {
            handleBlockPlacing(player, targetBlock, handItem);
        }
    }

    private boolean shouldHandleBreaking(ItemStack handItem, boolean isBlock, Player player) {
        return plugin.getPluginConfig().isBreaking()
                && (!isBlock || handItem.getType().isAir())
                && !(handItem.getType().name().contains("SWORD") && player.getGameMode() == GameMode.CREATIVE);
    }

    private boolean shouldHandleBuilding(boolean isBlock) {
        return plugin.getPluginConfig().isBuilding() && isBlock;
    }

    private void handleBlockBreaking(Player player, Block targetBlock) {
        BlockBreakBorderEvent event = new BlockBreakBorderEvent(player, targetBlock);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            blockBreakManager.startBreaking(player, targetBlock, player.getInventory().getItemInMainHand());
        }
    }

    private void handleBlockPlacing(Player player, Block targetBlock, ItemStack handItem) {
        BlockPlaceBorderEvent event = new BlockPlaceBorderEvent(player, targetBlock);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            BlockPlaceManager.handleBlockPlace(player, targetBlock, handItem);
        }
    }
}