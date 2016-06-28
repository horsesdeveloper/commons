package com.horses.app.commons;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.horses.camera.ui.application.CameraManager;
import com.horses.camera.utils.Constants;
import com.horses.camera.utils.ImageUtils;

public class MainActivity extends AppCompatActivity {

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.image);

        CameraManager.getInst().openCameraSimple(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_PICK && resultCode == RESULT_OK) {

            Uri uri = data.getData();

            ImageUtils.asyncLoadImage(this, uri, new ImageUtils.LoadImageCallback() {
                @Override
                public void callback(Bitmap result) {
                    image.setImageBitmap(result);
                }
            });
        }
    }
}
