package com.horses.camera.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edmodo.cropper.CropImageView;
import com.horses.camera.R;
import com.horses.camera.ui.application.CameraManager;
import com.horses.camera.utils.FileUtils;
import com.horses.camera.utils.ImageUtils;

/**
 * @author Brian Salvattore
 */
public class CropperActivity extends BaseActivity {

    private MaterialDialog dialog;

    protected CropImageView cropImageView;

    @Override
    protected int getView() {
        return R.layout.activity_cropper;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate() {

        cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        setSupportActionBar();
        setSupportActionBar(getResources().getString(R.string.title_cropper));
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_close_white_24dp));

        setCropImageView();
    }

    private void setCropImageView() {

        Uri fileUri = getIntent().getData();
        Bitmap oriBitmap = ImageUtils.decodeBitmapWithOrientation(fileUri.getPath(), CameraManager.getScreenWidth(), CameraManager.getScreenHeight());

        cropImageView.setImageBitmap(oriBitmap);
        cropImageView.setFixedAspectRatio(true);
    }

    private void saveImageToCache(Bitmap croppedImage) {

        if (croppedImage != null) {

            try {

                ImageUtils.saveToFile(FileUtils.getInst().getCacheDir() + "/croppedcache", false, croppedImage);

                Intent intent = new Intent();

                intent.setData(Uri.parse("file://" + FileUtils.getInst().getCacheDir() + "/croppedcache"));

                setResult(RESULT_OK, intent);

                dialog.dismiss();

                finish();
            } catch (Exception e) {

                e.printStackTrace();
                /*TODO if not save*/
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {

            finish();
        }
        else if (id == R.id.action_check) {

            dialog = new MaterialDialog.Builder(activity)
                    .title(R.string.dialog_title)
                    .content(R.string.dialog_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();

            saveImageToCache(cropImageView.getCroppedImage());
        }
        return super.onOptionsItemSelected(item);
    }
}
