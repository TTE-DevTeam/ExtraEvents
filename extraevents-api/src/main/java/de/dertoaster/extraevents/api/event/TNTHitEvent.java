package de.dertoaster.extraevents.api.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TNTHitEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity hitEntity;
    private final Block hitBlock;
    private final BlockFace hitFace;
    private boolean cancel;

    @ApiStatus.Internal
    public TNTHitEvent(@NotNull TNTPrimed projectile, @Nullable Entity hitEntity, @Nullable Block hitBlock, @Nullable BlockFace hitFace) {
        super(projectile);
        this.cancel = false;
        this.hitEntity = hitEntity;
        this.hitBlock = hitBlock;
        this.hitFace = hitFace;
    }

    public @NotNull TNTPrimed getEntity() {
        return (TNTPrimed)this.entity;
    }

    public @Nullable Block getHitBlock() {
        return this.hitBlock;
    }

    public @Nullable BlockFace getHitBlockFace() {
        return this.hitFace;
    }

    public @Nullable Entity getHitEntity() {
        return this.hitEntity;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

}
