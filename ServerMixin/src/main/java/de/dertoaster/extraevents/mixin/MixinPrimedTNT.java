package de.dertoaster.extraevents.mixin;

import de.dertoaster.extraevents.ProjectileHelper;
import de.dertoaster.extraevents.api.event.TNTHitEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.TNTPrimed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class MixinPrimedTNT extends Entity {

  public MixinPrimedTNT(EntityType<?> entityType, Level level) {
    super(entityType, level);
  }

  @Inject(
    method = "tick()V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/entity/item/PrimedTnt;applyGravity()V",
      shift = At.Shift.AFTER
    )
  )
  private void mixinTick(CallbackInfo callbackInfo) {
    // 1) Make HitResult
    HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, ProjectileHelper::canHitEntity, ClipContext.Block.COLLIDER);

    // 2) Hit result returned anything other than MISS?
    if (hitResult.getType() == HitResult.Type.MISS) {
      return;
    }

    // 3) Throw event
    org.bukkit.entity.Entity hitEntity = null;
    org.bukkit.block.Block hitBlock = null;
    org.bukkit.block.BlockFace hitFace = null;
    if (hitResult instanceof BlockHitResult blockHitResult) {
      hitBlock = CraftBlock.at(this.level(), blockHitResult.getBlockPos());
      hitFace = CraftBlock.notchToBlockFace(blockHitResult.getDirection());
    }
    if (hitResult instanceof EntityHitResult entityHitResult){
      hitEntity = entityHitResult.getEntity().getBukkitEntity();
    }
    TNTHitEvent event = new TNTHitEvent((TNTPrimed) this.getBukkitEntity(), hitEntity, hitBlock, hitFace);

    // 4) If the event isnt cancelled, Remove the X and Z component of the velocity
    /*if (event.callEvent()) {
      this.setDeltaMovement(0, this.getDeltaMovement().y(), 0);
    }*/
  }

}
