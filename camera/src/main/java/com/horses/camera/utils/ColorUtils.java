package com.horses.camera.utils;

import com.horses.camera.ui.application.CameraManager;

/**
 * @author Brian Salvattore
 */
public class ColorUtils {

    private static final int DEFAULT_COLOR = android.R.color.black;
    private static final int RED_COLOR = android.R.color.black;

    @SuppressWarnings("ResourceType")
    public static int getPrimaryColor() {

        return CameraManager
                .getApplication()
                .getResources()
                .getColor(CameraManager.getColorPrimary() == 0 ? DEFAULT_COLOR : CameraManager.getColorPrimary());
    }

    public static int getRedColor() {

        return CameraManager
                .getApplication()
                .getResources()
                .getColor(RED_COLOR);
    }
}
