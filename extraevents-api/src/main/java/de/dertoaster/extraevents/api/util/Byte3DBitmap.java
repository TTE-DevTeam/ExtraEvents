package de.dertoaster.extraevents.api.util;

import de.dertoaster.extraevents.api.explosion.CoveringVector;

public class Byte3DBitmap {

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    private final byte[] data;

    public Byte3DBitmap(int radius) {
        this(-radius, -radius, -radius, radius, radius, radius);
    }

    public Byte3DBitmap(int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);

        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        this.sizeX  = maxX - minX + 1;
        this.sizeY = maxY - minY + 1;
        this.sizeZ  = maxZ - minZ + 1;

        this.offsetX = -minX;
        this.offsetY = -minY;
        this.offsetZ = -minZ;

        int bitCount = sizeX * sizeY * sizeZ;
        this.data = new byte[(bitCount + 7) / 8];
    }

    private int toBitIndex(int x, int y, int z) {
        int ix = x + offsetX;
        int iy = y + offsetY;
        int iz = z + offsetZ;
        checkBounds(ix, iy, iz);
        return (iz * sizeY * sizeX) + (iy * sizeX) + ix;
    }

    private void checkBounds(int x, int y, int z) {
        if (x < 0 || x >= sizeX ||
                y < 0 || y >= sizeY ||
                z < 0 || z >= sizeZ) {
            throw new IndexOutOfBoundsException(
                    "Coordinate outside of range: (" + x + "," + y + "," + z + ")"
            );
        }
    }

    public void set(int x, int y, int z) {
        int bitIndex = toBitIndex(x, y, z);
        int byteIndex = bitIndex / 8;
        int bitOffset = bitIndex % 8;

        data[byteIndex] |= (1 << bitOffset);
    }

    public void unset(int x, int y, int z) {
        int bitIndex = toBitIndex(x, y, z);
        int byteIndex = bitIndex / 8;
        int bitOffset = bitIndex % 8;

        data[byteIndex] &= ~(1 << bitOffset);
    }

    public boolean get(int x, int y, int z) {
        int bitIndex = toBitIndex(x, y, z);
        int byteIndex = bitIndex / 8;
        int bitOffset = bitIndex % 8;

        return (data[byteIndex] & (1 << bitOffset)) != 0;
    }

    public void clearAll() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public int minX() { return -offsetX; }
    public int minY() { return -offsetY; }
    public int minZ() { return -offsetZ; }

    public int maxX() { return sizeX  - offsetX - 1; }
    public int maxY() { return sizeY - offsetY - 1; }
    public int maxZ() { return sizeZ  - offsetZ - 1; }

    public boolean inBounds(int x, int y, int z) {
        return (
                (minX() <= x && maxX() >= x) &&
                (minY() <= y && maxY() >= y) &&
                (minZ() <= z && maxZ() >= z)
        );
    }

    public boolean contains(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return false;
        }
        return this.get(x, y, z);
    }

    public void add(Byte3DBitmap other) {
       internalAddRemove(other, true);
    }

    private void internalAddRemove(Byte3DBitmap other, boolean add) {
        int fromX = Math.max(this.minX(), other.minX());
        int fromY = Math.max(this.minY(), other.minY());
        int fromZ = Math.max(this.minZ(), other.minZ());

        int toX = Math.min(this.maxX(), other.maxX());
        int toY = Math.min(this.maxY(), other.maxY());
        int toZ = Math.min(this.maxZ(), other.maxZ());

        if (fromX > toX || fromY > toY || fromZ > toZ) {
            return; // keine Überlappung
        }

        for (int z = fromZ; z <= toZ; z++) {
            for (int y = fromY; y <= toY; y++) {
                for (int x = fromX; x <= toX; x++) {
                    if (other.get(x, y, z)) {
                        if (add) {
                            this.set(x, y, z);
                        } else {
                            this.unset(x, y, z);
                        }
                    }
                }
            }
        }
    }

    public void remove(Byte3DBitmap other) {
        internalAddRemove(other, false);
    }

    public void set(CoveringVector toTest) {
        this.set(toTest.x(), toTest.y(), toTest.z());
    }

    public boolean inBounds(CoveringVector toTest) {
        return this.inBounds(toTest.x(), toTest.y(), toTest.z());
    }
}
