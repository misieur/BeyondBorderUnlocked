package com.maximde.beyondBorderUnlocked;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.util.TimeStampMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.maximde.beyondBorderUnlocked.mechanics.block.BlockBreakManager;
import com.maximde.beyondBorderUnlocked.mechanics.border.BorderManager;
import com.maximde.beyondBorderUnlocked.mechanics.outline.BlockOutlineManager;
import com.maximde.beyondBorderUnlocked.utils.Metrics;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

import com.maximde.beyondBorderUnlocked.listener.client.packet.InteractEntityPacketListener;
import com.maximde.beyondBorderUnlocked.utils.Command;
import com.maximde.beyondBorderUnlocked.utils.Config;
import com.maximde.beyondBorderUnlocked.listener.PlayerMoveListener;
import com.maximde.beyondBorderUnlocked.listener.client.packet.PlayerAnimationPacketListener;

@Getter
public final class BeyondBorderUnlocked extends JavaPlugin {

    private PlayerManager playerManager;
    private ProtocolManager protocolManager;
    private Config pluginConfig;

    @Getter
    private static BeyondBorderUnlocked instance;

    @Getter
    private final Map<UUID, List<Integer>> playerOutlines = new HashMap<>();

    @Getter
    private final Map<UUID, Location> lastOutlinedBlock = new HashMap<>();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        pluginConfig = new Config(this);
        getCommand("beyondborder").setExecutor(new Command(this));

        new Metrics(this, 24714);

        PacketEvents.getAPI().getEventManager().registerListener(new InteractEntityPacketListener(this),
                PacketListenerPriority.LOW);
        PacketEvents.getAPI().getSettings().debug(false).checkForUpdates(false).timeStampMode(TimeStampMode.MILLIS).reEncodeByDefault(true);
        PacketEvents.getAPI().init();
        playerManager = PacketEvents.getAPI().getPlayerManager();
        protocolManager = PacketEvents.getAPI().getProtocolManager();
        
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this, new BlockOutlineManager(this), new BorderManager()), this);

        PacketEvents.getAPI().getEventManager().registerListener(new PlayerAnimationPacketListener(this, new BlockBreakManager(this)),
                PacketListenerPriority.LOW);

        if(this.getPluginConfig().isDamageEnabled()) {
            for (World world : Bukkit.getWorlds()) {
                world.getWorldBorder().setDamageBuffer(this.getPluginConfig().getDamageBuffer());
                world.getWorldBorder().setDamageAmount(this.getPluginConfig().getDamageAmount());
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            PacketEvents.getAPI().terminate();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "Failed to terminate packet events", exception);
        }
    }

    public void removeOutline(UUID playerId) {
        if (this.getPlayerOutlines().containsKey(playerId)) {
            for (int entityId : this.getPlayerOutlines().get(playerId)) {
                this.getPlayerManager().sendPacket(Objects.requireNonNull(Bukkit.getPlayer(playerId)), new WrapperPlayServerDestroyEntities(entityId));
            }
            this.getPlayerOutlines().remove(playerId);
            this.getLastOutlinedBlock().remove(playerId);
        }
    }
}
