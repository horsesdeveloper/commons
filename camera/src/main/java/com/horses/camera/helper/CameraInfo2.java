package com.horses.camera.helper;

import java.io.Serializable;

/**
 * @author Brian Salvattore
 */
public class CameraInfo2 implements Serializable {

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    private int facing;
    private int orientation;
}
