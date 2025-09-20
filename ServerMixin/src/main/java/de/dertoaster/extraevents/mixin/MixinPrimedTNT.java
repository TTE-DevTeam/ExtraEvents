package de.dertoaster.extraevents.mixin;

import ca.spottedleaf.moonrise.common.util.CoordinateUtils;
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder;
import de.dertoaster.extraevents.ProjectileHelper;
import de.dertoaster.extraevents.api.BresenhamUtil;
import de.dertoaster.extraevents.api.event.TNTHitEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class MixinPrimedTNT extends Entity {

  @Shadow
  public abstract int getFuse();

  // TODO: Implement for new explosion algorithm
  private Object damageCache = null;

  private static final TicketType<Unit> TNT_CHUNKLOAD = TicketType.create("tte_tnt", (unit1, unit2) -> {return 0;}, 400);

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
  private void mixinTickImpactExplosion(CallbackInfo callbackInfo) {
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
    if (hitResult instanceof EntityHitResult entityHitResult) {
      hitEntity = entityHitResult.getEntity().getBukkitEntity();
    }
    TNTHitEvent event = new TNTHitEvent((TNTPrimed) this.getBukkitEntity(), hitEntity, hitBlock, hitFace);

    // 4) If the event isnt cancelled, Remove the X and Z component of the velocity
    if (event.callEvent()) {
      //this.setDeltaMovement(0, this.getDeltaMovement().y(), 0);
    }
  }

  @Inject(
    method = "tick()V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/entity/item/PrimedTnt;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
      shift = At.Shift.BEFORE
    )
  )
  private void mixinTickChunkLoading(CallbackInfo callbackInfo) {
    Vec3 velocityRaw = this.getDeltaMovement();
    Vec3i velocity = new Vec3i((int) velocityRaw.x(), (int) velocityRaw.y(), (int) velocityRaw.z());
    BlockPos posCur = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
    BlockPos posNext = posCur.offset(velocity);
    ChunkPos chunkPosCur = new ChunkPos(posCur);
    // Offset the next position by 2x the distance travelled to smoothen the process
    ChunkPos chunkPosNext = new ChunkPos(posNext);

    if (chunkPosCur.equals(chunkPosNext)) {
      return;
    }

    // Not the same chunk => Chunkload!
    // Iterate along the path and load chunks in => chunk in front and to the left and right of the chunk
    // Run Bresenham-Line algorithm to find all chunks we cross
    for (BresenhamUtil.IntTuple chunkCoords : BresenhamUtil.bresenham2d(new BresenhamUtil.IntTuple(chunkPosCur), new BresenhamUtil.IntTuple(chunkPosNext))) {
      ChunkPos chunkPos = new ChunkPos(chunkCoords.a(), chunkCoords.b());

      final boolean loadedChunk = this.level().getChunkIfLoaded(chunkCoords.a(), chunkCoords.b()) != null;

      final ServerLevel level = (ServerLevel) this.level();
      final @Nullable NewChunkHolder chunkHolder = level.moonrise$getChunkTaskScheduler().chunkHolderManager.getChunkHolder(CoordinateUtils.getChunkKey(chunkPos));

      final boolean tickingEntity = chunkHolder != null && chunkHolder.isEntityTickingReady();
        //System.out.println("Adding TICKING ticket for chunk: " + chunkCoords.a() + " " + chunkCoords.b());
        // Force load chunk and mark it for ticking!
      if (!loadedChunk) {
        level.getChunkSource().addTicketAtLevel(TNT_CHUNKLOAD, chunkPos, ChunkLevel.BLOCK_TICKING_LEVEL, Unit.INSTANCE);
      }
      if (!loadedChunk || !tickingEntity) {
        level.getChunkSource().addTicketAtLevel(TNT_CHUNKLOAD, chunkPos, ChunkLevel.ENTITY_TICKING_LEVEL, Unit.INSTANCE);
      }
    }
  }

  @Inject(
    method = "tick()V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/entity/item/PrimedTnt;explode()V",
      shift = At.Shift.BEFORE
    ),
    cancellable = true
  )
  private void mixinBeforeExplode(CallbackInfo callbackInfo) {
    float explosionPower = ((PrimedTnt)(Object)this).explosionPower;
    if (this.getFuse() < 0 && this.damageCache == null) {
      return;
    }
    // Only if the value is greater than that we will use our new algorithm!
    if (explosionPower < 16) {
      return;
    }
    ExplosionPrimeEvent event = CraftEventFactory.callExplosionPrimeEvent((Explosive)this.getBukkitEntity());
    if (event.isCancelled()) {
      return;
    }

    explosionPower = event.getRadius();

//    1) Calculate block blob => the possibly affected blocks=> take from CQR, but use a 3d boolean array
//    2) Calculate layers (lists) using that blob and put them in a queue and make sure there is no gap
//    3) Repeat for each layer and for each entry in the layer
//      3.1) If the blast resistance is equal or above the limit (blast power remaining = maxRadius - layer-index), break the block
//        3.1.5) To find the relevant corners of the cube, take the center/corner vector, set it's length to a fourth of the blocks side length and then add it and remove it to the corners
//               If the resulting location is inside the cube, it is not a corner we need
//      3.2) Otherwise, calculate the block cone of that block (vectors = center-to-block-center; center-to-other-corners of the block !!depends on orientation!!) and build a pyramid (pyramid "height" = maxRadius)
//           Calculate all normals for the 5 planes of the pyramid
//      3.3) For this layer and all following layers, remove all positions that are in that blast cone
//      3.4) If a layer has no more positions, remove it
//      3.5) Repeat until no more layer is left
    // If we ran our custom explosion algorithm, cancel the CallbackInfo!
  }

  /**
   * @author DerToaster98
   * @reason New Explosion algorithm can create negative fuse tnt
   */
  @Overwrite
  public boolean isPushedByFluid() {
    if (this.getFuse() <= 0) {
      return false;
    }
    return !this.level().paperConfig().fixes.preventTntFromMovingInWater && super.isPushedByFluid();
  }

}
