package de.dertoaster.extraevents.mixin;

import de.dertoaster.extraevents.ProjectileHelper;
import de.dertoaster.extraevents.api.BresenhamUtil;
import de.dertoaster.extraevents.api.event.TNTHitEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerChunkCache;
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
import org.bukkit.entity.TNTPrimed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class MixinPrimedTNT extends Entity {

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
    ChunkPos chunkPosNext = new ChunkPos(posNext.offset(posNext.subtract(posCur)));

    if (chunkPosCur.equals(chunkPosNext)) {
      return;
    }

    // Not the same chunk => Chunkload!
    // Iterate along the path and load chunks in => chunk in front and to the left and right of the chunk
    // Run Bresenham-Line algorithm to find all chunks we cross
    for (BresenhamUtil.IntTuple chunkCoords : BresenhamUtil.bresenham2d(new BresenhamUtil.IntTuple(chunkPosCur), new BresenhamUtil.IntTuple(chunkPosNext))) {
      ChunkPos chunkPos = new ChunkPos(chunkCoords.a(), chunkCoords.b());
      if (this.level().getChunkIfLoaded(chunkCoords.a(), chunkCoords.b()) == null) {
        ServerLevel level = this.level().getMinecraftWorld();

        /*Seems to load the chunk, but entities in there arent processed...*/
        level.getChunkSource().addRegionTicket(
          TNT_CHUNKLOAD,
          chunkPos,
          1,
          Unit.INSTANCE
        );
        //System.out.println("Force loading chunk: " + chunkCoords.a() + " " + chunkCoords.b());
      }
      if (!this.level().shouldTickBlocksAt(chunkPos.toLong())) {
        //System.out.println("Adding TICKING ticket for chunk: " + chunkCoords.a() + " " + chunkCoords.b());
        ((ServerChunkCache) this.level().getChunkSource()).addTicketAtLevel(TNT_CHUNKLOAD, chunkPos, 2, Unit.INSTANCE);
      }
    }
  }

}
