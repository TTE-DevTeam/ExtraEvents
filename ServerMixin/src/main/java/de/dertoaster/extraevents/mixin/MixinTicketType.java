package de.dertoaster.extraevents.mixin;

import de.dertoaster.extraevents.api.chunk.TTETicketTypes;
import net.minecraft.server.level.TicketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TicketType.class)
public abstract class MixinTicketType {

  @Inject(
    method="<clinit>",
    at = @At("TAIL")
  )
  private static void mixinStaticBlock(CallbackInfo callbackInfo) {
    TTETicketTypes.register();
  }

}
