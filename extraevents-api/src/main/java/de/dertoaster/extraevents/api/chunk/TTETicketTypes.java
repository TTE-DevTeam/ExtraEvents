package de.dertoaster.extraevents.api.chunk;

import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;

public class TTETicketTypes {

    public static final TicketType<Unit> ENTITY_CHUNKLOAD = TicketType.register("tte_entity_chunkload", 400, false, TicketType.TicketUse.LOADING_AND_SIMULATION);

    public static void register() {

    }

}
