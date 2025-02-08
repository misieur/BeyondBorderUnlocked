package com.maximde.beyondBorderUnlocked.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncEntityDamageBorderEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    @Getter
    private final Entity damager;

    @Getter
    private final Entity entity;

    @Setter @Getter
    private double damage;

    public AsyncEntityDamageBorderEvent(Entity damager, Entity entity, double damage) {
        super(true);
        this.damager = damager;
        this.entity = entity;
        this.damage = damage;
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
