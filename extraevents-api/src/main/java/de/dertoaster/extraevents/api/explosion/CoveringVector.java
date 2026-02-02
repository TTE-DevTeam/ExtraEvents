package de.dertoaster.extraevents.api.explosion;

import de.dertoaster.extraevents.api.util.Byte3DBitmap;
import de.dertoaster.extraevents.api.util.math.geometry.NonConvexPolyhedron;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.Set;

public record CoveringVector(
        byte x,
        byte y,
        byte z,
        // TODO Replace with normal reference list and test memory requirements!
        Set<CoveringVector> coveredPositions
) {

    private static final float[] CORNERS = {
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f
    };

    public CoveringVector(byte x, byte y, byte z) {
        // TODO: Calculate the proper box that this vector can occlude!
        // TODO: Implement the above, we need more than 32GB of memory when computing 64m radius!
//        this(x, y, z, new Byte3DBitmap((int) (1 + (Math.round(Math.sqrt((x * x) + (y * y) + (z * z)))))));
        this(x, y, z, new HashSet<>());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        CoveringVector other = (CoveringVector) obj;
        return other.x() == this.x() && other.y() == this.y() && other.z() == this.z();
    }

    // Test if THIS cube occludes the cube of OTHER
    public boolean occludes(CoveringVector other) {
        return occludes(other.x(), other.y(), other.z(), this.x(), this.y(), this.z());
    }

    public boolean occludes(byte x, byte y, byte z) {
        return occludes(x, y, z, this.x(), this.y(), this.z());
    }

    // Attention: Assumes that 0/0/0 is the VIEWER!
    // Returns if the 1x1x1 cube around B occludes the cube around A
    private static boolean occludes(
            byte ax, byte ay, byte az,
            byte bx, byte by, byte bz) {

        // --- Early Out 1: Compare distance; If B is further away than A, than B can't occlude A
        float da2 = ax * ax + ay * ay + az * az;
        float db2 = bx * bx + by * by + bz * bz;
        if (db2 >= da2) return false;

        // --- Early Out 2: Check rough direction
        float dot = ax * bx + ay * by + az * bz;
        if (dot <= 0) return false;

        // --- Bounding Box of B
        float bMinX = bx - 0.5f, bMaxX = bx + 0.5f;
        float bMinY = by - 0.5f, bMaxY = by + 0.5f;
        float bMinZ = bz - 0.5f, bMaxZ = bz + 0.5f;

        // --- Bounding Box of A (for t_hit_A)
        float aMinX = ax - 0.5f, aMaxX = ax + 0.5f;
        float aMinY = ay - 0.5f, aMaxY = ay + 0.5f;
        float aMinZ = az - 0.5f, aMaxZ = az + 0.5f;

        // --- Test for each vertex of A
        // TODO: Change implementation
        // Instead, use each vertex and add and subtract a small vector from it. If it is in "our" cube, skip to the next vertex
        // From those remaining vertices,
        for (int i = 0; i < CORNERS.length; i += 3) {

            float dx = ax + CORNERS[i];
            float dy = ay + CORNERS[i + 1];
            float dz = az + CORNERS[i + 2];

            float tB = intersectRayBox(dx, dy, dz,
                    bMinX, bMinY, bMinZ,
                    bMaxX, bMaxY, bMaxZ);

            if (tB < 0) continue;

            float tA = intersectRayBox(dx, dy, dz,
                    aMinX, aMinY, aMinZ,
                    aMaxX, aMaxY, aMaxZ);

            if (tA < 0) continue;

            // B intersects A => Occluded!
            if (tB < tA) return true;
        }

        return false;
    }

    /**
     * Ray: R(t) = t * (dx,dy,dz), t >= 0
     * @return t of the first intersection or -1 if there is no intersection
     */
    private static float intersectRayBox(
            float dx, float dy, float dz,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {

        float tMin = 0.0f;
        float tMax = Float.POSITIVE_INFINITY;

        // X
        if (dx != 0) {
            float inv = 1.0f / dx;
            float t1 = minX * inv;
            float t2 = maxX * inv;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMax < tMin) return -1;
        }

        // Y
        if (dy != 0) {
            float inv = 1.0f / dy;
            float t1 = minY * inv;
            float t2 = maxY * inv;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMax < tMin) return -1;
        }

        // Z
        if (dz != 0) {
            float inv = 1.0f / dz;
            float t1 = minZ * inv;
            float t2 = maxZ * inv;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMax < tMin) return -1;
        }

        return tMin >= 0 ? tMin : -1;
    }

    NonConvexPolyhedron createCoveringPolyhedron() {
        // Walk over all vertices of a cube
        // Discard the vertices that would cut into the cube in either direction towards zero
        // That way we can sort out the vertices we don't need for the convex hull
        // Then, create the polyhedron from the remaining vertices
        throw new NotImplementedException();
    }

}
