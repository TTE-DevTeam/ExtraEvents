package de.dertoaster.extraevents.api.explosion;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class Onion {

    // TODO: Runtime object will be a bitmaphitbox resembling the yet to visit objects
    // TODO: Runtime stores 2 ints: currentIndex and node count
    // TODO: Runtime uses a "next()" method that returns a coveringVector

    // 1) Create a Map<Int, Int, Int, Vector> that holds ALL the vectors
    // 2) While creating, sort the Vectors into lists, these resemble the individual layers
    //    Distance: ROUND DOWN
    // 3) Go over each vector in each layer and add the vectors in the next layer to it's "covered" list that it covers
    // 4) Provide method to generate a queue that is sorted by distance for a given radius

    protected static final int PRECOMPUTE_RADIUS = 64;
    protected static OnionLayer[] LAYERS = new OnionLayer[PRECOMPUTE_RADIUS + 1];

    static {
        // Create layers
        for (int i = 0; i <= PRECOMPUTE_RADIUS; i++) {
            LAYERS[i] = new OnionLayer();
        }

        // Create vectors
        for (byte iX = -PRECOMPUTE_RADIUS; iX <= PRECOMPUTE_RADIUS; iX++) {
            for (byte iY = -PRECOMPUTE_RADIUS; iY <= PRECOMPUTE_RADIUS; iY++) {
                for (byte iZ = -PRECOMPUTE_RADIUS; iZ <= PRECOMPUTE_RADIUS; iZ++) {
                    // Enforce ball shape!
                    final double dist = Math.sqrt(iX * iX + iY * iY + iZ * iZ);
                    if (dist > PRECOMPUTE_RADIUS) {
                        continue;
                    }
                    CoveringVector vector = new CoveringVector(iX, iY, iZ);
                    final int layer = (int) Math.round(dist);
                    if (layer < 0 || layer >= LAYERS.length) {
                        System.err.println("FAILURE: Position " + vector.toString() + " is located in layer " + layer + " which is outside our computed area!");
                    } else {
                        LAYERS[layer].add(vector);
                    }
                }
            }
        }

        // Now, iterate over each layer except the last one and compute the covered positions...
        for (int i = 0; i < LAYERS.length - 1; i++) {
            final int layerIndex = i;
            LAYERS[i].forEach(cv -> computeCoveredPositions(cv, layerIndex));
        }
    }

    protected static void computeCoveredPositions(CoveringVector position, int layerIndex) {
        for (int i = layerIndex + 1; i < LAYERS.length; i++) {
            OnionLayer layer = LAYERS[i];
            for (CoveringVector toTest : layer) {
                if (position.occludes(toTest)) {
                    if (position.coveredPositions().inBounds(toTest)) {
                        position.coveredPositions().set(toTest);
                    }
                }
            }
        }
    }

    // TODO: Optimize!
    public static Optional<CoveringVector> next(int index) {
        if (index < 0) {
            return Optional.empty();
        }
        int layerIndex = 0;
        int offset = 0;
        while (layerIndex < LAYERS.length) {
            OnionLayer currentLayer = LAYERS[layerIndex];
            if (index - offset <= currentLayer.size()) {
                return Optional.ofNullable(currentLayer.get(index));
            } else {
                offset += currentLayer.size();
            }
        }
        return Optional.empty();
    }

    public static Queue<CoveringVector> getVectorQueue(final int radius) {
        Queue<CoveringVector> result = new LinkedList<>();

        for (int i = 0; i <= radius && i < LAYERS.length; i++) {
            for (CoveringVector coveringVector : LAYERS[i]) {
                result.add(coveringVector);
            }
        }

        return result;
    }

}
