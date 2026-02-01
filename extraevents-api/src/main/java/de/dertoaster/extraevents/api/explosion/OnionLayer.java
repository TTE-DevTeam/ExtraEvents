package de.dertoaster.extraevents.api.explosion;

import de.dertoaster.extraevents.api.util.Byte3DBitmap;

import java.util.ArrayList;

public class OnionLayer extends ArrayList<CoveringVector> {

    private final Byte3DBitmap positionMap;

    public OnionLayer(final int radius) {
        int radToUse = radius + 1;
        this.positionMap = new Byte3DBitmap(-radToUse, -radToUse, -radToUse, radToUse, radToUse, radToUse);
    }

    @Override
    public boolean add(CoveringVector coveringVector) {
        if (super.add(coveringVector)) {
            this.positionMap.set(coveringVector);
            return true;
        }
        return false;
    }

}
