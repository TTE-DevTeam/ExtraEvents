package de.dertoaster.extraevents.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplosionPropellProjectileEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final Entity exploder;
    private boolean cancelled = false;
    private final Vector pushDirection;

    public ExplosionPropellProjectileEvent(@NotNull Projectile theEntity, @Nullable Entity exploder, @NotNull Vector pushDirection) {
        super(theEntity);
        this.exploder = exploder;
        this.pushDirection = pushDirection;
    }

    @Nullable
    public Entity getPusher() {
        return this.exploder;
    }

    public Projectile getProjectile() {
        return (Projectile) this.entity;
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
