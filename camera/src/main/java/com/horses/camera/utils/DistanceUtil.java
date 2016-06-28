package com.horses.camera.utils;

import com.horses.camera.ui.application.CameraManager;

/**
 * @author Brian Salvattore
 */
public class DistanceUtil {

    public static int getCameraAlbumWidth() {
        return (CameraManager.getScreenWidth() - CameraManager.dp2px(10)) / 4;
    }

    public static int getCameraPhotoAreaHeight() {
        return getCameraPhotoWidth() + CameraManager.dp2px(4);
    }

    public static int getCameraPhotoWidth() {
        return CameraManager.getScreenWidth() / 4;
    }

    public static int getActivityHeight() {
        return (CameraManager.getScreenWidth() - CameraManager.dp2px(24)) / 3;
    }
}
