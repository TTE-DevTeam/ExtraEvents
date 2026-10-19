package de.dertoaster.extraevents.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.dertoaster.extraevents.api.event.DispenserDispenseEntityEvent;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.SpawnEggItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SpawnEggItemBehavior.class)
public class MixinSpawnEggItemBehavior extends DefaultDispenseItemBehavior {

  @Inject(
    method = "execute(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;",
      shift = At.Shift.AFTER
    ),
    locals = LocalCapture.CAPTURE_FAILEXCEPTION
  )
  private void mixinExecute(BlockSource source, ItemStack dispensed, CallbackInfoReturnable<ItemStack> callbackInfo, @Local(name = "spawned", type = Entity.class) Entity spawned) {
    Block block = CraftBlock.at(source.level(), source.pos());
    DispenserDispenseEntityEvent ddee = new DispenserDispenseEntityEvent(block, spawned.getBukkitEntity());
    //if (!DispenserBlock.eventFired) {
    ddee.callEvent();
    //}
  }

}
