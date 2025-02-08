package com.maximde.beyondBorderUnlocked.listener;

import com.maximde.beyondBorderUnlocked.BeyondBorderUnlocked;
import com.maximde.beyondBorderUnlocked.mechanics.border.BorderManager;
import com.maximde.beyondBorderUnlocked.mechanics.outline.BlockOutlineManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public class PlayerMoveListener implements Listener {
    private final BeyondBorderUnlocked plugin;
    private final BlockOutlineManager outlineManager;
    private final BorderManager borderManager;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        outlineManager.updateOutline(player);

        if(plugin.getPluginConfig().isWalkthrough()) {
            borderManager.handleBorderCrossing(player, event.getFrom(), event.getTo());
        }
    }
}