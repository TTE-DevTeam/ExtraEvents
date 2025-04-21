package de.dertoaster.extraevents.api.event;

import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplosionPropellTNTEvent extends EntityEvent {

    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final TNTPrimed exploder;

    public ExplosionPropellTNTEvent(@NotNull TNTPrimed theEntity, @Nullable TNTPrimed exploder) {
        super(theEntity);
        this.exploder = exploder;
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


}
