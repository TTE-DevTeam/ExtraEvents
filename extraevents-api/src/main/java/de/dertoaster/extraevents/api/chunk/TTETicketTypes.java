package de.dertoaster.extraevents.api.chunk;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;

public class TTETicketTypes {

    // Old similar flags: ENDER_PEARL, POST_TELEPORT
    public static final TicketType<Unit> ENTITY_CHUNKLOAD = register("tte_entity_chunkload", 400, TicketType.FLAG_LOADING + TicketType.FLAG_SIMULATION + TicketType.FLAG_KEEP_DIMENSION_ACTIVE);

    private static TicketType<Unit> register(String tteEntityChunkload, int timeout, int flags) {
        return (TicketType) Registry.register(BuiltInRegistries.TICKET_TYPE, tteEntityChunkload, new TicketType(timeout, flags));
    }

    public static void register() {

    }

}
