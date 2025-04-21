package de.dertoaster.extraevents.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class DispenserDispenseEntityEvent extends BlockEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Entity entity;

    public DispenserDispenseEntityEvent(@NotNull Block theBlock, @NotNull Entity theEntity) {
        super(theBlock);
        this.entity = theEntity;
    }

    public Entity getEntity() {
        return entity;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
