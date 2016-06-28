package com.horses.camera.helper;

import android.hardware.Camera;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("deprecation")
public class CameraHelperGB implements CameraHelperImpl {

    @Override
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public Camera openCamera(final int id) {
        return Camera.open(id);
    }

    @Override
    public Camera openDefaultCamera() {
        return Camera.open(0);
    }

    @Override
    public boolean hasCamera(final int facing) {
        return getCameraId(facing) != -1;
    }

    @Override
    public Camera openCameraFacing(final int facing) {
        return Camera.open(getCameraId(facing));
    }

    @Override
    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        cameraInfo.setFacing(info.facing);
        cameraInfo.setOrientation(info.orientation);
    }

    private int getCameraId(final int facing) {

        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();

        for (int id = 0; id < numberOfCameras; id++) {

            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }

        return -1;
    }
}
