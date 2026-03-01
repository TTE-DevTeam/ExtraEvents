package de.dertoaster.extraevents.api.explosion;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class Onion {

    // TODO: Runtime object will be a single bitmaphitbox resembling the yet to visit objects
    // TODO: Runtime stores 2 ints: currentIndex and node count
    // TODO: Runtime uses a "next()" method that returns a coveringVector
    // TODO: Runtime: First, grab the next pos, if that pos is empty in our bitmap, set the covered positions as empty too

    protected static final int PRECOMPUTE_RADIUS = 32;
    protected static OnionLayer[] LAYERS = new OnionLayer[PRECOMPUTE_RADIUS + 1];

    public static void init() {
        // Do nothing
    }

    static {
        // Create layers
        System.out.println("Creating <" + PRECOMPUTE_RADIUS + "> layer instances...");
        for (int i = 0; i <= PRECOMPUTE_RADIUS; i++) {
            LAYERS[i] = new OnionLayer(i);
        }

        System.out.println("Creating position node objects...");
        long createdNodes =0;
        // Create vectors
        for (byte iX = -PRECOMPUTE_RADIUS; iX <= PRECOMPUTE_RADIUS; iX++) {
            for (byte iY = -PRECOMPUTE_RADIUS; iY <= PRECOMPUTE_RADIUS; iY++) {
                for (byte iZ = -PRECOMPUTE_RADIUS; iZ <= PRECOMPUTE_RADIUS; iZ++) {
                    // Enforce ball shape!
                    final double dist = Math.sqrt(iX * iX + iY * iY + iZ * iZ);
                    if (Math.round(dist) > PRECOMPUTE_RADIUS) {
                        continue;
                    }
                    CoveringVector vector = new CoveringVector(iX, iY, iZ);
                    final int layer = (int) Math.round(dist);
                    if (layer < 0 || layer >= LAYERS.length) {
                        System.err.println("FAILURE: Position " + vector.toString() + " is located in layer " + layer + " which is outside our computed area!");
                    } else {
                        if (LAYERS[layer].add(vector)) {
                            createdNodes++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i <= PRECOMPUTE_RADIUS; i++) {
            OnionLayer layer = LAYERS[i];
            System.out.println("Nodes in layer <" + i + ">: " + layer.size());
        }

        System.out.println("Computing occlusion...");
        // Now, iterate over each layer except the last one and compute the covered positions...
        final AtomicLong totallyOccludedPositions = new AtomicLong(0);
        // TODO: Use multithreading for this, otherwise it will take forever!
        for (int i = 0; i < LAYERS.length; i++) {
            final int layerIndex = i;
            LAYERS[i].forEach(cv -> computeCoveredPositions(cv, layerIndex, totallyOccludedPositions));
        }
        final long occlusions = totallyOccludedPositions.get();
        System.out.println("Created nodes: " + createdNodes);
        System.out.println("Created occlusions: " + occlusions);
        System.out.println("Average occlusions: " + Math.round(((double) occlusions) / ((double) (createdNodes - LAYERS[LAYERS.length - 1].size()))));
        System.out.println("Done!");
    }

    // TODO: This does not seem to be correct!
    protected static void computeCoveredPositions(CoveringVector position, int layerIndex, AtomicLong totallyOccludedPositions) {
        for (int i = layerIndex; i < LAYERS.length; i++) {
            OnionLayer nextLayer = LAYERS[i];
            for (CoveringVector toTest : nextLayer) {
                // Danger: This will maybe cut off data!
                byte deltaX = (byte) (toTest.x() - position.x());
                byte deltaY = (byte) (toTest.y() - position.y());
                byte deltaZ = (byte) (toTest.z() - position.z());
                if (position.occludes(deltaX, deltaY, deltaZ)) {
//                if (position.coveredPositions().inBounds(deltaX, deltaY, deltaZ)) {
//                    position.coveredPositions().set(deltaX, deltaY, deltaZ);
//                }
                    position.coveredPositions().add(toTest);
                    totallyOccludedPositions.getAndIncrement();
                }
            }
        }
    }

    public static Optional<OnionLayer> getLayer(int index) {
        if (index < 0 || index >= LAYERS.length) {
            return Optional.empty();
        }
        return Optional.ofNullable(LAYERS[index]);
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
