package de.dertoaster.extraevents.api.entity;

import ca.spottedleaf.moonrise.common.util.CoordinateUtils;
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder;
import de.dertoaster.extraevents.api.BresenhamUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IChunkLoadingEntity {

    public boolean canLoadChunks();

    public void setCanLoadChunks(boolean value);

    static final TicketType<Unit> ENTITY_CHUNKLOAD = TicketType.register("tte_entity_chunkload", 400, false, TicketType.TicketUse.LOADING_AND_SIMULATION);

    public default void callOnReadAdditionalSaveData(final CompoundTag compound) {
        if (!compound.contains("tte") || compound.getCompound("tte") == null) {
            return;
        }
        Optional<CompoundTag> tteTag = compound.getCompound("tte");
        if (tteTag.isPresent() && tteTag.get().contains("chunkLoading")) {
            Optional<Boolean> optBool = tteTag.get().getBoolean("chunkLoading");
            if (optBool.isPresent() && optBool.get()) {
                this.setCanLoadChunks(true);
            } else {
                this.setCanLoadChunks(false);
            }
        }
    }

    public default void callOnAddAdditionalSaveData(final CompoundTag compound) {
        Optional<CompoundTag> tteTag = compound.getCompound("tte");
        tteTag.ifPresent(tag -> {
            tag.putBoolean("chunkLoading", this.canLoadChunks());
        });
    }

    public default void loadChunks(final Vec3 movementDelta, final BlockPos posCur, final ServerLevel level) {
        if (!this.canLoadChunks()) {
            return;
        }

        Vec3 velocityRaw = movementDelta;
        Vec3i velocity = new Vec3i((int) velocityRaw.x(), (int) velocityRaw.y(), (int) velocityRaw.z());
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

            final boolean loadedChunk = level.getChunkIfLoaded(chunkCoords.a(), chunkCoords.b()) != null;

            final @Nullable NewChunkHolder chunkHolder = level.moonrise$getChunkTaskScheduler().chunkHolderManager.getChunkHolder(CoordinateUtils.getChunkKey(chunkPos));

            final boolean tickingEntity = chunkHolder != null && chunkHolder.isEntityTickingReady();
            //System.out.println("Adding TICKING ticket for chunk: " + chunkCoords.a() + " " + chunkCoords.b());
            // Force load chunk and mark it for ticking!
            if (!loadedChunk) {
                level.getChunkSource().addTicketAtLevel(ENTITY_CHUNKLOAD, chunkPos, ChunkLevel.BLOCK_TICKING_LEVEL);
            }
            if (!loadedChunk || !tickingEntity) {
                level.getChunkSource().addTicketAtLevel(ENTITY_CHUNKLOAD, chunkPos, ChunkLevel.ENTITY_TICKING_LEVEL);
            }
        }
    }

}
