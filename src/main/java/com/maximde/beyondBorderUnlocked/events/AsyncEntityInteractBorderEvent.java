package com.maximde.beyondBorderUnlocked.events;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncEntityInteractBorderEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    @Getter
    private final Player player;

    @Getter
    private final Entity entity;

    @Getter
    private final InteractAction action;

    public static enum InteractAction {
        INTERACT,
        ATTACK,
        INTERACT_AT;
    }

    public AsyncEntityInteractBorderEvent(Player player, Entity entity, WrapperPlayClientInteractEntity.InteractAction action) {
        super(true);
        this.player = player;
        this.entity = entity;
        this.action = InteractAction.valueOf(action.name());
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
