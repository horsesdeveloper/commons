package com.horses.camera.helper;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("deprecation")
public class CameraHelper {

    private final CameraHelperImpl base;

    public CameraHelper() {

        base = new CameraHelperGB();
    }

    public int getNumberOfCameras() {
        return base.getNumberOfCameras();
    }

    public Camera openCamera(final int id) {
        return base.openCamera(id);
    }

    public Camera openDefaultCamera() {
        return base.openDefaultCamera();
    }

    public Camera openFrontCamera() {
        return base.openCameraFacing(CameraInfo.CAMERA_FACING_FRONT);
    }

    public Camera openBackCamera() {
        return base.openCameraFacing(CameraInfo.CAMERA_FACING_BACK);
    }

    public boolean hasFrontCamera() {
        return base.hasCamera(CameraInfo.CAMERA_FACING_FRONT);
    }

    public boolean hasBackCamera() {
        return base.hasCamera(CameraInfo.CAMERA_FACING_BACK);
    }

    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        base.getCameraInfo(cameraId, cameraInfo);
    }

    public void setCameraDisplayOrientation(final Activity activity, final int cameraId,
                                            final Camera camera) {

        int result = getCameraDisplayOrientation(activity, cameraId);
        camera.setDisplayOrientation(result);
    }

    public int getCameraDisplayOrientation(final Activity activity, final int cameraId) {

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(cameraId, info);

        if (info.getFacing() == CameraInfo.CAMERA_FACING_FRONT) {

            return (info.getOrientation() + degrees) % 360;
        }
        else {

            return(info.getOrientation() - degrees + 360) % 360;
        }
    }
}
