package de.dertoaster.extraevents.api;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class BresenhamUtil {

    public record IntTuple(int a, int b) {
        public IntTuple(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public IntTuple(ChunkPos chunkPos) {
            this(chunkPos.x, chunkPos.z);
        }
    }

    public static List<IntTuple> bresenham2d(IntTuple posA, IntTuple posB) {
        List<IntTuple> result = new ArrayList<>();

        int x1 = posA.a();
        int y1 = posA.a();
        int x2 = posB.a();
        int y2 = posB.b();

        int dX = Math.abs(x2 - x1);
        int sX = x1 < x2 ? 1 : -1;
        int dY = -Math.abs(y2 - y1);
        int sY = y1 < y2 ? 1 : -1;
        int err = dX + dY;
        int err2;

        // TODO: Prevent infinite loop by time constraint
        while(true) {
            result.add(new IntTuple(x1, y1));
            if (x1 == x2 && y1 == y2) {
                break;
            }

            err2 = err * 2;

            if (err2 > dY) {
                err += dY;
                x1 += sX;
            }
            if (err2 < dX) {
                err += dX;
                y1 += sY;
            }
        }

        return result;
    }

}
