package de.dertoaster.extraevents.api.event;

import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplosionPropellTNTEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final TNTPrimed exploder;
    private boolean cancelled = false;
    private final Vector pushDirection;

    public ExplosionPropellTNTEvent(@NotNull TNTPrimed theEntity, @Nullable TNTPrimed exploder, @NotNull Vector pushDirection) {
        super(theEntity);
        this.exploder = exploder;
        this.pushDirection = pushDirection;
    }

    @Nullable
    public TNTPrimed getExploder() {
        return this.exploder;
    }

    public TNTPrimed getProjectile() {
        return (TNTPrimed) this.entity;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    public Vector getPushDirection() {
        return pushDirection;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
