package com.horses.camera.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.horses.camera.R;
import com.horses.camera.business.PhotoItem;
import com.horses.camera.helper.CameraHelper;
import com.horses.camera.ui.application.CameraManager;
import com.horses.camera.ui.view.CameraGrid;
import com.horses.camera.utils.ColorUtils;
import com.horses.camera.utils.Constants;
import com.horses.camera.utils.DistanceUtil;
import com.horses.camera.utils.FileUtils;
import com.horses.camera.utils.IOUtil;
import com.horses.camera.utils.ImageLoaderUtils;
import com.horses.camera.utils.ImageUtils;
import com.horses.camera.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Brian Salvattore
 */
public class TakeActivity extends BaseActivity {

    private static final String TAG = "Camera";

    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    private static final double MAX_ASPECT_DISTORTION = 0.15;

    private static int PHOTO_SIZE = 2000;
    private int currentCameraId = 0;

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private int photoNumber = Constants.NUMBER_OF_PHOTOS;
    private int photoWidth = DistanceUtil.getCameraPhotoWidth();
    private int photoMargin = CameraManager.dp2px(1);

    private float pointX, pointY;
    private static final int FOCUS = 1;
    private static final int ZOOM = 2;
    private int mode;
    private float dist;
    private int curZoomValue = 0;

    private CameraHelper cameraHelper;
    @SuppressWarnings("deprecation")
    private Camera cameraInst = null;
    @SuppressWarnings("deprecation")
    private Camera.Parameters parameters = null;
    @SuppressWarnings("deprecation")
    private Camera.Size adapterSize = null;
    @SuppressWarnings("deprecation")
    private Camera.Size previewSize = null;

    private Handler handler = new Handler();

    private CameraGrid cameraGrid;

    private LinearLayout photoArea;

    private View takePhotoPanel;
    private View focusIndex;
    private View shutter;

    private ImageView flashBtn;
    private ImageView changeBtn;
    private ImageView backBtn;
    private ImageView galleryBtn;

    private Button takePicture;

    private SurfaceView surfaceView;

    @Override
    protected int getView() {
        return R.layout.activity_take;
    }

    @Override
    protected void onCreate() {

        cameraGrid = (CameraGrid) findViewById(R.id.masking);
        photoArea       = (LinearLayout) findViewById(R.id.photo_area);
        takePhotoPanel  = findViewById(R.id.panel_take_photo);
        focusIndex      = findViewById(R.id.focus_index);
        shutter         = findViewById(R.id.shutter);
        flashBtn        = (ImageView) findViewById(R.id.flash);
        changeBtn       = (ImageView) findViewById(R.id.change);
        backBtn         = (ImageView) findViewById(R.id.back);
        galleryBtn      = (ImageView) findViewById(R.id.next);
        takePicture     = (Button) findViewById(R.id.take_picture);
        surfaceView     = (SurfaceView) findViewById(R.id.surface_view);

        cameraHelper = new CameraHelper();

        initView();
        initEvent();
        initButtons();

        changeColor();
    }

    private void initButtons(){

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(activity, GalleryActivity.class), Constants.REQUEST_CROP);
            }
        });

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchCamera();
            }
        });

        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                turnLight(cameraInst);
            }
        });

        takePicture.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {

                try {

                    animateShutter();
                    /*cameraInst.takePicture(null, null, (data, camera) -> {

                        Bundle bundle = new Bundle();

                        bundle.putByteArray("bytes", data);

                        new SavePicTask(data).execute();
                        camera.startPreview();
                    });*/

                    cameraInst.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                            Bundle bundle = new Bundle();

                            bundle.putByteArray("bytes", data);

                            new SavePicTask(data).execute();
                            camera.startPreview();
                        }
                    });
                }
                catch (Throwable t) {

                    t.printStackTrace();

                    new MaterialDialog.Builder(activity)
                            .title(R.string.dialog_title_fail)
                            .content(R.string.dialog_content_fail)
                            .positiveText(android.R.string.ok)
                            .show();

                    try {

                        cameraInst.startPreview();
                    }
                    catch (Throwable ignore) {

                    }
                }
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void initEvent(){

        boolean canSwitch = false;

        try {

            canSwitch = cameraHelper.hasFrontCamera() && cameraHelper.hasBackCamera();
        }
        catch (Exception ignored) { }

        if (!canSwitch) {

            changeBtn.setVisibility(View.GONE);
        }

        /*surfaceView.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:

                    pointX = event.getX();
                    pointY = event.getY();
                    mode = FOCUS;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:

                    dist = spacing(event);

                    if (spacing(event) > 10f) {

                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:

                    mode = FOCUS;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == FOCUS) {

                        //pointFocus((int) event.getRawX(), (int) event.getRawY());
                    } else if (mode == ZOOM) {

                        float newDist = spacing(event);

                        if (newDist > 10f) {

                            float tScale = (newDist - dist) / dist;

                            if (tScale < 0) {

                                tScale = tScale * 10;
                            }

                            addZoomIn((int) tScale);
                        }
                    }
                    break;
            }
            return false;
        });*/

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:

                        pointX = event.getX();
                        pointY = event.getY();
                        mode = FOCUS;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:

                        dist = spacing(event);

                        if (spacing(event) > 10f) {

                            mode = ZOOM;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:

                        mode = FOCUS;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == FOCUS) {

                            //pointFocus((int) event.getRawX(), (int) event.getRawY());
                        } else if (mode == ZOOM) {

                            float newDist = spacing(event);

                            if (newDist > 10f) {

                                float tScale = (newDist - dist) / dist;

                                if (tScale < 0) {

                                    tScale = tScale * 10;
                                }

                                addZoomIn((int) tScale);
                            }
                        }
                        break;
                }
                return false;
            }
        });

        /*surfaceView.setOnClickListener(v -> {

            try {

                pointFocus((int) pointX, (int) pointY);
            } catch (Exception e) {

                e.printStackTrace();
            }

            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(focusIndex.getLayoutParams());
            layout.setMargins((int) pointX - 60, (int) pointY - 60, 0, 0);
            focusIndex.setLayoutParams(layout);
            focusIndex.setVisibility(View.VISIBLE);
            ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(800);
            focusIndex.startAnimation(sa);

            handler.postDelayed(() -> focusIndex.setVisibility(View.INVISIBLE), 800);
        });*/

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    pointFocus((int) pointX, (int) pointY);
                } catch (Exception e) {

                    e.printStackTrace();
                }

                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(focusIndex.getLayoutParams());
                layout.setMargins((int) pointX - 60, (int) pointY - 60, 0, 0);
                focusIndex.setLayoutParams(layout);
                focusIndex.setVisibility(View.VISIBLE);
                ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(800);
                focusIndex.startAnimation(sa);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusIndex.setVisibility(View.INVISIBLE);
                    }
                }, 800);
            }
        });


    }

    private float spacing(MotionEvent event) {

        if (event == null)
            return 0;

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    @SuppressWarnings("deprecation")
    private void addZoomIn(int delta) {

        try {

            Camera.Parameters params = cameraInst.getParameters();

            if (!params.isZoomSupported())
                return;

            curZoomValue += delta;

            if (curZoomValue < 0) {

                curZoomValue = 0;
            }
            else if (curZoomValue > params.getMaxZoom()) {

                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {

                params.setZoom(curZoomValue);
                cameraInst.setParameters(params);
            }
            else {

                cameraInst.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void pointFocus(int x, int y) {

        cameraInst.cancelAutoFocus();
        parameters = cameraInst.getParameters();

        showPoint(x, y);

        cameraInst.setParameters(parameters);
        autoFocus();
    }

    @SuppressWarnings("deprecation")
    private void showPoint(int x, int y) {

        if (parameters.getMaxNumMeteringAreas() > 0) {

            List<Camera.Area> areas = new ArrayList<>();

            int rectY = -x * 2000 / CameraManager.getScreenWidth() + 1000;
            int rectX = y * 2000 / CameraManager.getScreenHeight() - 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;

            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));

            parameters.setMeteringAreas(areas);
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }


    private void switchCamera() {

        currentCameraId = (currentCameraId + 1) % cameraHelper.getNumberOfCameras();
        releaseCamera();

        setUpCamera(currentCameraId);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void setUpCamera(int mCurrentCameraId2) {

        cameraInst = getCameraInstance(mCurrentCameraId2);

        if (cameraInst != null) {
            try {
                cameraInst.setPreviewDisplay(surfaceView.getHolder());
                initCamera();
                cameraInst.startPreview();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {

            /*TODO again*/
        }
    }

    private void releaseCamera() {

        if (cameraInst != null) {

            cameraInst.setPreviewCallback(null);
            cameraInst.release();
            cameraInst = null;
        }

        adapterSize = null;
        previewSize = null;
    }

    @SuppressWarnings("deprecation")
    private void turnLight(Camera camera) {

        if (camera == null || camera.getParameters() == null || camera.getParameters().getSupportedFlashModes() == null)
            return;

        Camera.Parameters parameters = camera.getParameters();
        String flashMode = camera.getParameters().getFlashMode();
        List<String> supportedModes = camera.getParameters().getSupportedFlashModes();

        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode) && supportedModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {

            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            flashBtn.setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
        else if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {

                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                flashBtn.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                camera.setParameters(parameters);
            }
            else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {

                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                flashBtn.setImageResource(R.drawable.ic_flash_off_white_24dp);
                camera.setParameters(parameters);
            }
        }
        else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode) && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {

            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            flashBtn.setImageResource(R.drawable.ic_flash_off_white_24dp);
        }
    }

    private class SavePicTask extends AsyncTask<Void, Void, String> {

        private byte[] data;
        private MaterialDialog dialog;

        protected void onPreExecute() {

            dialog = new MaterialDialog.Builder(activity)
                    .title(R.string.dialog_title)
                    .content(R.string.dialog_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        private SavePicTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                return saveToSDCard(data);
            }
            catch (IOException e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            dialog.dismiss();

            if (StringUtils.isNotEmpty(result)) {

                CameraManager.getInst().processCropper(activity, new PhotoItem(result, System.currentTimeMillis()));
            }
            else {

                new MaterialDialog.Builder(activity)
                        .title(R.string.dialog_title_fail)
                        .content(R.string.dialog_content_fail)
                        .positiveText(android.R.string.ok)
                        .show();
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public String saveToSDCard(byte[] data) throws IOException {

        Bitmap croppedImage;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        PHOTO_SIZE = options.outHeight > options.outWidth ? options.outWidth : options.outHeight;
        int height = options.outHeight > options.outWidth ? options.outHeight : options.outWidth;

        options.inJustDecodeBounds = false;
        Rect r;

        if (currentCameraId == 1) {

            r = new Rect(height - PHOTO_SIZE, 0, height, PHOTO_SIZE);
        }
        else {

            r = new Rect(0, 0, PHOTO_SIZE, PHOTO_SIZE);
        }

        try {

            croppedImage = decodeRegionCrop(data, r);
        }
        catch (Exception e) {

            return null;
        }

        String imagePath = ImageUtils.saveToFile(FileUtils.getInst().getSystemPhotoPath(), true,
                croppedImage);

        croppedImage.recycle();

        return imagePath;
    }

    private Bitmap decodeRegionCrop(byte[] data, Rect rect) {

        InputStream is = null;
        System.gc();
        Bitmap croppedImage = null;

        try {

            is = new ByteArrayInputStream(data);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);

            try {

                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
            }
            catch (IllegalArgumentException ignore) { }
        }
        catch (Throwable e) {

            e.printStackTrace();
        }
        finally {

            IOUtil.closeStream(is);
        }

        Matrix m = new Matrix();
        m.setRotate(90, PHOTO_SIZE / 2, PHOTO_SIZE / 2);

        if (currentCameraId == 1)
            m.postScale(1, -1);


        Bitmap rotatedImage = Bitmap.createBitmap(croppedImage, 0, 0, PHOTO_SIZE, PHOTO_SIZE, m, true);

        if (rotatedImage != croppedImage) {

            assert croppedImage != null;
            croppedImage.recycle();
        }

        return rotatedImage;
    }

    private void animateShutter() {

        shutter.setVisibility(View.VISIBLE);
        shutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(shutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(shutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {

                shutter.setVisibility(View.GONE);
            }
        });

        animatorSet.start();
    }

    private void changeColor(){

        backBtn.getDrawable().setColorFilter(ColorUtils.getPrimaryColor(), PorterDuff.Mode.MULTIPLY);
        galleryBtn.getDrawable().setColorFilter(ColorUtils.getPrimaryColor(), PorterDuff.Mode.MULTIPLY);
    }

    @SuppressWarnings("deprecation")
    private void initView(){

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        surfaceView.getHolder().addCallback(new SurfaceCallback());

        ViewGroup.LayoutParams layout = cameraGrid.getLayoutParams();
        layout.height = CameraManager.getScreenWidth();
        layout = photoArea.getLayoutParams();
        layout.height = DistanceUtil.getCameraPhotoAreaHeight();
        layout = takePhotoPanel.getLayoutParams();
        layout.height = CameraManager.getScreenHeight()
                - CameraManager.getScreenWidth()
                - DistanceUtil.getCameraPhotoAreaHeight();

        ArrayList<PhotoItem> sysPhotos = FileUtils.getInst().findPicsInDir(FileUtils.getInst().getSystemPhotoPath());

        int showNumber = sysPhotos.size() > photoNumber ? photoNumber : sysPhotos.size();

        for (int i = 0; i < showNumber; i++) {
            addPhoto(sysPhotos.get(showNumber - 1 - i));
        }

    }

    @SuppressWarnings({"SuspiciousNameCombination", "StatementWithEmptyBody"})
    private void addPhoto(PhotoItem photoItem) {
        ImageView photo = new ImageView(this);

        if (StringUtils.isNotBlank(photoItem.getImageUri())) {

            ImageLoaderUtils.displayLocalImage(photoItem.getImageUri(), photo);
        }
        else {

            /*TODO photo.setImageResource(R.drawable.default_img);*/
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(photoWidth, photoWidth);
        params.leftMargin = photoMargin;
        params.rightMargin = photoMargin;
        params.gravity = Gravity.CENTER;
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setTag(photoItem.getImageUri());

        if (photoArea.getChildCount() >= photoNumber) {
            photoArea.removeViewAt(photoArea.getChildCount() - 1);
            photoArea.addView(photo, 0, params);
        }
        else {
            photoArea.addView(photo, 0, params);
        }

        /*photo.setOnClickListener(v -> {

            if (v instanceof ImageView && v.getTag() instanceof String) {

                CameraManager.getInst().processCropper(activity, new PhotoItem((String) v.getTag(), System.currentTimeMillis()));
            }
        });*/

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v instanceof ImageView && v.getTag() instanceof String) {

                    CameraManager.getInst().processCropper(activity, new PhotoItem((String) v.getTag(), System.currentTimeMillis()));
                }
            }
        });
    }

    private final class SurfaceCallback implements SurfaceHolder.Callback {

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {

                if (cameraInst != null) {

                    cameraInst.stopPreview();
                    cameraInst.release();
                    cameraInst = null;
                }
            }
            catch (Exception ignored) { }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            if (null == cameraInst) {

                try {

                    cameraInst = Camera.open();
                    cameraInst.setPreviewDisplay(holder);
                    initCamera();
                    cameraInst.startPreview();
                }
                catch (Throwable e) {

                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            autoFocus();
        }
    }

    private void autoFocus() {
        new Thread() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                try {

                    sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cameraInst == null) {

                    return;
                }
                /*cameraInst.autoFocus((success, camera) -> {

                    if (success) {

                        initCamera();
                    }
                });*/

                cameraInst.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                        if (success) {

                            initCamera();
                        }
                    }
                });
            }
        };
    }

    @SuppressWarnings("deprecation")
    private void initCamera() {

        parameters = cameraInst.getParameters();

        parameters.setPictureFormat(PixelFormat.JPEG);

        setUpPicSize();
        setUpPreviewSize();

        if (adapterSize != null)
            parameters.setPictureSize(adapterSize.width, adapterSize.height);


        if (previewSize != null)
            parameters.setPreviewSize(previewSize.width, previewSize.height);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        else {

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        setDisplay(parameters, cameraInst);

        try {

            cameraInst.setParameters(parameters);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        cameraInst.startPreview();
        cameraInst.cancelAutoFocus();
    }

    @SuppressWarnings("deprecation")
    private void setDisplay(Camera.Parameters parameters, Camera camera) {

        if (Build.VERSION.SDK_INT >= 8) {

            setDisplayOrientation(camera, 90);
        }
        else {

            parameters.setRotation(90);
        }
    }

    @SuppressWarnings("deprecation")
    private void setDisplayOrientation(Camera camera, int i) {

        Method downPolymorphic;

        try {

            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);

            if (downPolymorphic != null) {

                downPolymorphic.invoke(camera, i);
            }
        }
        catch (Exception ignore) { }
    }

    private void setUpPicSize() {

        if(adapterSize == null)
            adapterSize = findBestPictureResolution();
    }

    private void setUpPreviewSize() {

        if(previewSize == null)
            previewSize = findBestPreviewResolution();
    }

    @SuppressWarnings("deprecation")
    private Camera.Size findBestPictureResolution() {

        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes();

        StringBuilder picResolutionSb = new StringBuilder();

        for (Camera.Size supportedPicResolution : supportedPicResolutions) {

            picResolutionSb.append(supportedPicResolution.width).append('x').append(supportedPicResolution.height).append(" ");
        }

        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();

        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<>(supportedPicResolutions);

        /*Collections.sort(sortedSupportedPicResolutions, (a, b) -> {

            int aPixels = a.height * a.width;
            int bPixels = b.height * b.width;
            if (bPixels < aPixels) {
                return -1;
            }
            if (bPixels > aPixels) {
                return 1;
            }
            return 0;
        });*/

        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {

                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        double screenAspectRatio = (double) CameraManager.getScreenWidth() / (double) CameraManager.getScreenHeight();
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();

        while (it.hasNext()) {

            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);

            if (distortion > MAX_ASPECT_DISTORTION) {

                it.remove();
            }
        }

        if (!sortedSupportedPicResolutions.isEmpty()) {

            return sortedSupportedPicResolutions.get(0);
        }

        return defaultPictureResolution;
    }

    @SuppressWarnings("deprecation")
    private Camera.Size findBestPreviewResolution() {

        Camera.Parameters cameraParameters = cameraInst.getParameters();
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();

        if (rawSupportedSizes == null)
            return defaultPreviewResolution;

        List<Camera.Size> supportedPreviewResolutions = new ArrayList<>(rawSupportedSizes);

        /*Collections.sort(supportedPreviewResolutions, (a, b) -> {

            int aPixels = a.height * a.width;
            int bPixels = b.height * b.width;

            if (bPixels < aPixels) {

                return -1;
            }

            if (bPixels > aPixels) {

                return 1;
            }

            return 0;
        });*/

        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {

                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;

                if (bPixels < aPixels) {

                    return -1;
                }

                if (bPixels > aPixels) {

                    return 1;
                }

                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();

        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {

            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height).append(' ');
        }

        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);

        double screenAspectRatio = (double) CameraManager.getScreenWidth() / (double) CameraManager.getScreenHeight();

        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();

        while (it.hasNext()) {

            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            if (width * height < MIN_PREVIEW_PIXELS) {

                it.remove();
                continue;
            }

            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;

            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {

                it.remove();
                continue;
            }

            if (maybeFlippedWidth == CameraManager.getScreenWidth() && maybeFlippedHeight == CameraManager.getScreenHeight()) {

                return supportedPreviewResolution;
            }
        }

        if (!supportedPreviewResolutions.isEmpty()) {

            return supportedPreviewResolutions.get(0);
        }

        return defaultPreviewResolution;
    }

    @SuppressWarnings("deprecation")
    private Camera getCameraInstance(final int id) {

        Camera camera = null;

        try {

            camera = cameraHelper.openCamera(id);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        return camera;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent result) {

        if (requestCode == Constants.REQUEST_CROP && resultCode == RESULT_OK) {

            Intent intent = new Intent();
            intent.setData(result.getData());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
