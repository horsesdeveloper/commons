package com.horses.app.commons;

import android.app.Application;

import com.horses.camera.ui.application.CameraManager;

public class Main extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new CameraManager.Builder(this)
                .packageName("demo")
                .primaryColor(R.color.colorPrimary)
                .init();
    }
}
