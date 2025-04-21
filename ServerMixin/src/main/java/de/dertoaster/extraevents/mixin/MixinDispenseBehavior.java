package de.dertoaster.extraevents.mixin;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DispenseItemBehavior.class)
public abstract class MixinDispenseBehavior {

  // RE-register some of the dispense behaviors AFTER bootstrap

}
