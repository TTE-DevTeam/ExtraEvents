package de.dertoaster.extraevents.mixin;

import de.dertoaster.extraevents.DispenseBehaviorHelper;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public abstract class MixinBootstrap {

  // RE-register some of the dispense behaviors AFTER bootstrap

  @Inject(
    method = "bootStrap()V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;bootStrap()V",
      shift = At.Shift.AFTER
    ),
    remap = false
  )
  private static void replaceDispenseBehaviors(
    CallbackInfo callbackInfo
  ) {
    DispenseBehaviorHelper.onBootstrap();
  }

}
