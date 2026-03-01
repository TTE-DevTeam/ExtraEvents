package de.dertoaster.extraevents.api.explosion;

import de.dertoaster.extraevents.api.util.math.geometry.ConvexPolyhedron;
import de.dertoaster.extraevents.api.util.math.geometry.Plane3D;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private static final double EPS = 1e-8;
    ConvexPolyhedron createCoveringPolyhedron() {
        // Walk over all vertices of a cube
        // Discard the vertices that would cut into the cube in either direction towards zero
        // That way we can sort out the vertices we don't need for the convex hull
        // Then, create the polyhedron from the remaining vertices

        // Technically, we are using the visible vertices from the center to create a convex open hull
        // Instead of the vertices, we should use the visible edges of the "cube"

        // Step 1) Collect all valid vertices of the cube
        // TODO: Add check if the position is "visible" => Addition and subtraction of a small "step vector" must not result in a position inside the cube!
        List<Vector> vertices = new ArrayList<>(8);
        for (double iX = x - .5; iX <= x + .5; iX++) {
            for (double iY = y - .5; iY <= y + .5; iY++) {
                for (double iZ = z - .5; iZ <= z + .5; iZ++) {
                    vertices.add(new Vector(iX, iY, iZ));
                }
            }
        }
        final Vector self = new Vector(x, y, z);
        // Step 2) Calculate the convex hull of those points => The visible "outline" of the cube from the origin
        //   Step 2.1) Iterate through the visible positions and per position over all following positions
        //   Step 2.2) Create the normal from both positions (cross product). If length < 0 => Colinear, no edge
        //   Step 2.3) Test with the normal and all other vertices that aren't "our" vertices, if dotproduct < 0
        //   Step 2.4) If dot > 0, set "pos" marker
        //   Step 2.5) Otherwise, set "neg" marker
        //   Step 2.6) If "pos" and "neg" marker are both set, quit the loop. Only if both are not set, this is a visible edge
        final Set<Triple<Vector, Vector, Vector>> planeList = new HashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            final Vector currentVertex = vertices.get(i);
            for (int j = i + 1; j < vertices.size(); j++) {
                final Vector otherVertex = vertices.get(j);
                final Vector normal = currentVertex.crossProduct(otherVertex);

                if (normal.length() < EPS) {
                    continue;
                }

                boolean pos = false;
                boolean neg = false;

                for (int k = 0; k < vertices.size(); k++) {
                    if (k == i || k == j) continue;

                    final Vector kVec = vertices.get(k);

                    final double dot = normal.dot(kVec);
                    if (Math.abs(dot) < EPS) continue;

                    if (dot > 0 ) {
                        pos = true;
                    } else {
                        neg = true;
                    }

                    if (pos && neg) break;
                }

                if (!(pos && neg)) {
                    planeList.add(Triple.of(currentVertex, otherVertex, self));
                }
            }
        }
        // Step 3) Per edge, create a plane using the 2 endpoints of the edge on the cube and the center
        // Step 4) Orient the plane, use the center of the cube is inner reference point
        final Vector pointInPolyhedron = self.clone().add(self.clone().normalize());
        final Plane3D preCheckPlane = new Plane3D(self, self, pointInPolyhedron);

        return new ConvexPolyhedron(planeList, pointInPolyhedron, preCheckPlane);
    }

}
