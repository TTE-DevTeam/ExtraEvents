package de.dertoaster.extraevents.api.explosion;

import java.util.LinkedList;
import java.util.Queue;

public class Onion {

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
        for (int iX = -PRECOMPUTE_RADIUS; iX <= PRECOMPUTE_RADIUS; iX++) {
            for (int iY = -PRECOMPUTE_RADIUS; iY <= PRECOMPUTE_RADIUS; iY++) {
                for (int iZ = -PRECOMPUTE_RADIUS; iZ <= PRECOMPUTE_RADIUS; iZ++) {
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

        // Now, iterate over each layer and compute the covered positions...
        for (int i = 0; i < LAYERS.length - 1; i++) {
            final OnionLayer nextLayer = LAYERS[i+1];
            LAYERS[i].forEach(cv -> computeCoveredPositions(cv, nextLayer));
        }
    }

    protected static void computeCoveredPositions(CoveringVector position, OnionLayer outerLayer) {
        // TODO: Implement
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
