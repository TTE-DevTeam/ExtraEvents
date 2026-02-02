package de.dertoaster.extraevents.api.util.math.geometry;


import org.bukkit.util.Vector;

public class Plane3D {

    private final double a;
    private final double b;
    private final double c;
    private final double d;

    private static final double EPS = 1e-8;

    private boolean lessThanZeroIsbehindPlane;

    public Plane3D(final Vector v1, final Vector v2, final Vector supportVector, final Vector pointBehindPlane) {
        this(v1.normalize().crossProduct(v2.normalize()), supportVector, pointBehindPlane);
    }
    public Plane3D(final Vector planeNormal, final Vector supportVector, final Vector pointBehindPlane) {
        planeNormal.normalize();
        this.a = planeNormal.getX();
        this.b = planeNormal.getY();
        this.c = planeNormal.getZ();

        if (Math.abs(a) + Math.abs(b) + Math.abs(c) == 0) {
            throw new IllegalArgumentException("At least one element of the plane normal vector must be non-zero!");
        }

        this.d = supportVector.dot(planeNormal);

        this.lessThanZeroIsbehindPlane = this.distance(pointBehindPlane.getX(), pointBehindPlane.getY(), pointBehindPlane.getZ()) <= 0.0D;
    }

    public double distance(final double x, final double y, final double z) {
        // Subtract D here
        // Normally, the equation is this:
        // a*x + b*y + c*z := d
        return (this.a * x) + (this.b * y) + (this.c * z) - this.d;
    }

    // If the original ax+by+cz == d is met, the point is on the plane
    // Our result function will return zero, since we subtract d
    public boolean isOn(final double x, final double y, final double z) {
        final double result = this.distance(x, y, z);
        return Math.abs(result) == EPS;
    }

    // Generally, two points are on the same "side" of a plane, if they share the same algebraic sign
    // Since we have our reference point, we can actually determine if a point is behind or in front of our plane
    public boolean isOnOrBehindPlane(final double x, final double y, final double z) {
        final double result = this.distance(x, y, z);
        if (Math.abs(result) == EPS) {
            return true;
        }
        if (this.lessThanZeroIsbehindPlane && result < 0.0D) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isBehindPlane(final double x, final double y, final double z) {
        final double result = this.distance(x, y, z);
        if (Math.abs(result) == EPS) {
            return false;
        }
        if (this.lessThanZeroIsbehindPlane && result < 0.0D) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnOrInFrontOfPlane(final double x, final double y, final double z) {
        final double result = this.distance(x, y, z);
        if (Math.abs(result) == EPS) {
            return true;
        }
        if (this.lessThanZeroIsbehindPlane && result < 0.0D) {
            return false;
        } else {
            return true;
        }
    }
    public boolean isInFrontOfPlane(final double x, final double y, final double z) {
        final double result = this.distance(x, y, z);
        if (Math.abs(result) == EPS) {
            return false;
        }
        if (this.lessThanZeroIsbehindPlane && result < 0.0D) {
            return false;
        } else {
            return true;
        }
    }

}
