package de.dertoaster.extraevents;

import net.minecraft.world.entity.Entity;

public class ProjectileHelper {

  // Copy of AbstractHurtingProjectile
  public static boolean canHitEntity(Entity target) {
    return internalCanHitEntity(target) && !target.noPhysics;
  }

  // Copy of Projectile
  public static boolean internalCanHitEntity(Entity target) {
    if (!target.canBeHitByProjectile()) {
      return false;
    }
    // Normally there would be projectile specific code here (has to do with the shooter). But we dont have one here, so we ignore it!
    return true;
  }

}
