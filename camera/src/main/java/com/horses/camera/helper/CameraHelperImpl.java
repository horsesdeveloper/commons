package com.horses.camera.helper;

import android.hardware.Camera;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("deprecation")
public interface CameraHelperImpl {

    int getNumberOfCameras();

    Camera openCamera(int id);

    Camera openDefaultCamera();

    Camera openCameraFacing(int facing);

    boolean hasCamera(int cameraFacingFront);

    void getCameraInfo(int cameraId, CameraInfo2 cameraInfo);
}
