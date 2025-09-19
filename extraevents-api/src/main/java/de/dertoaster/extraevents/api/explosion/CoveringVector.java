package de.dertoaster.extraevents.api.explosion;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: Hide access to the covered objects and use a function to remove them from the queue instead
public record CoveringVector(
        double x,
        double y,
        double z,
        List<WeakReference<CoveringVector>> coveredInNextLayer
) implements Iterable<WeakReference<CoveringVector>> {

    public CoveringVector(double x, double y, double z) {
        this(x, y, z, new ArrayList<>());
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

    @NotNull
    @Override
    public Iterator<WeakReference<CoveringVector>> iterator() {
        return this.coveredInNextLayer.iterator();
    }
}
