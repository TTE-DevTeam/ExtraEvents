package de.dertoaster.extraevents.api.util.math.geometry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.util.Vector;

import java.util.*;

public class NonConvexPolyhedron {

    protected final List<Plane3D> planes = new ArrayList<>();

    public NonConvexPolyhedron(final Set<Triple<Vector, Vector, Vector>> planeVectors, final Vector pointInPolyhedron) {
        for (Triple<Vector, Vector, Vector> entry : planeVectors) {
            Plane3D plane3D = new Plane3D(entry.getLeft(), entry.getMiddle(), entry.getRight(), pointInPolyhedron);
            this.planes.add(plane3D);
        }
        if (this.planes.isEmpty()) {
            throw new IllegalArgumentException("No planes provided! At least one plane is required");
        }
    }

    // TODO: Depending on our need add options to check for outside too

    // We are in the polyhedron, if we are "behind" every plane in the list
    public boolean isOnOrIn(final double x, final double y, final double z) {
        for (Plane3D plane3D : this.planes) {
            if (!plane3D.isOnOrBehindPlane(x, y, z)) {
                return false;
            }
        }
        return true;
    }

}
