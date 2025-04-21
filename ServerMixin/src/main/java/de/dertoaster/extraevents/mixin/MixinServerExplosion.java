package de.dertoaster.extraevents.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.dertoaster.extraevents.api.event.ExplosionPropellTNTEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerExplosion.class)
public abstract class MixinServerExplosion {

  // Mixin into hurtEntities => Directly before the call of "entity.push()"
  // => Fire event, if event is cancelled, set the vector's value to zero
  @Inject(
    method = "hurtEntities()V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/phys/Vec3;)V",
      shift = At.Shift.BEFORE
    ),
    remap = false
  )
  private void callTNTPropelEvent(
    CallbackInfo callbackInfo,
    @Local(name = "entity") Entity entity,
    @Local(name = "vec3") LocalRef<Vec3> vec3
  ) {
    if (!(entity instanceof PrimedTnt)) {
      return;
    }
    Entity exploder = ((ServerExplosion)(Object)this).getDirectSourceEntity();
    TNTPrimed exploderBukkit = null;
    if (exploder != null && (exploder.getBukkitEntity() instanceof TNTPrimed)) {
      exploderBukkit = (TNTPrimed) exploder.getBukkitEntity();
    }
    Vec3 vec3Raw = vec3.get();
    ExplosionPropellTNTEvent propellTNTEvent = new ExplosionPropellTNTEvent((TNTPrimed) entity.getBukkitEntity(), exploderBukkit, new Vector(vec3Raw.x, vec3Raw.y, vec3Raw.z));
    if (!propellTNTEvent.callEvent()) {
      vec3.set(Vec3.ZERO);
    }
  }

}
