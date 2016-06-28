package com.horses.camera.ui.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.horses.camera.business.PhotoItem;
import com.horses.camera.ui.activity.CropperActivity;
import com.horses.camera.ui.activity.TakeActivity;
import com.horses.camera.utils.Constants;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.util.Stack;

/**
 * @author Brian Salvattore
 */
public class CameraManager {

    private static CameraManager manager;
    private Stack<Activity> cameras = new Stack<>();

    private static Application application;

    private static int screenWidth = 0, screenHeight = 0;
    private static float screenDensity = 0f;
    private static String cacheDir, filesDir;
    private static int colorPrimary = 0;

    private static String packAge;

    public static CameraManager getInst() {

        if (manager == null) {

            synchronized (CameraManager.class) {
                if (manager == null)
                    manager = new CameraManager();
            }
        }
        return manager;
    }

    public void close() {

        for (Activity act : cameras) {

            try {

                act.finish();
            }
            catch (Exception ignore) { }
        }
        cameras.clear();
    }

    public void addActivity(Activity activity) {
        cameras.add(activity);
    }

    public void removeActivity(Activity activity) {
        cameras.remove(activity);
    }

    public void openCameraSimple(Activity activity) {

        Intent intent = new Intent(activity, TakeActivity.class);
        activity.startActivityForResult(intent, Constants.REQUEST_PICK);
    }

    public void processCropper(Activity activity, PhotoItem photo) {

        Uri uri = photo.getImageUri().startsWith("file:") ? Uri.parse(photo.getImageUri()) : Uri.parse("file://" + photo.getImageUri());

        Intent intent = new Intent(activity, CropperActivity.class);
        intent.setData(uri);

        activity.startActivityForResult(intent, Constants.REQUEST_CROP);
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static float getScreenDensity() {
        return screenDensity;
    }

    public static int dp2px(float dps) {
        return (int) (0.5F + dps * getScreenDensity());
    }

    public static int px2dp(float pxs) {
        return (int) (pxs / getScreenDensity() + 0.5f);
    }

    public static String getFilesDirPath() {
        return filesDir;
    }

    public static String getCacheDirPath() {
        return cacheDir;
    }

    public static Application getApplication(){
        return application;

    }public static void setApplication(Application app){
        application = app ;
    }

    public static String getPackAgeName(){
        return packAge;
    }

    public static int getColorPrimary(){
        return colorPrimary;
    }

    public static class Builder {

        private DisplayMetrics displayMetrics = null;
        private Application application;

        private String packageName = "horses";
        private int primaryColor;

        public static final String APP_DIR                    = Environment.getExternalStorageDirectory() + "/";
        public static final String APP_TEMP                   = APP_DIR + "/temp";
        public static final String APP_IMAGE                  = APP_DIR + "/Media/Images";


        public Builder primaryColor(int colorPrimary) {

            this.primaryColor = colorPrimary;
            return this;
        }

        public Builder packageName(String packAge) {

            this.packageName = packAge;
            return this;
        }

        public Builder(Application application) {

            this.application = application;
            setApplication(application);
        }

        public CameraManager init() {

            screenWidth = getScreenWidth();
            screenHeight = getScreenHeight();
            screenDensity = getScreenDensity();
            cacheDir = getCacheDirPath();
            filesDir = getFilesDirPath();

            colorPrimary = primaryColor;
            packAge = packageName;

            initImageLoader(application);

            return CameraManager.getInst();
        }

        public float getScreenDensity() {
            if (this.displayMetrics == null) {
                setDisplayMetrics(application.getResources().getDisplayMetrics());
            }
            return this.displayMetrics.density;
        }

        public int getScreenHeight() {
            if (this.displayMetrics == null) {
                setDisplayMetrics(application.getResources().getDisplayMetrics());
            }
            return this.displayMetrics.heightPixels;
        }

        public int getScreenWidth() {
            if (this.displayMetrics == null) {
                setDisplayMetrics(application.getResources().getDisplayMetrics());
            }
            return this.displayMetrics.widthPixels;
        }

        public void setDisplayMetrics(DisplayMetrics DisplayMetrics) {
            this.displayMetrics = DisplayMetrics;
        }

        public String getFilesDirPath() {
            return application.getFilesDir().getAbsolutePath();
        }

        public String getCacheDirPath() {
            return application.getCacheDir().getAbsolutePath();
        }

        public String fullDir(){

            return APP_DIR + packageName + APP_TEMP + APP_IMAGE;
        }

        private void initImageLoader(Application application) {

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheOnDisk(true)
                    .build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(application)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .defaultDisplayImageOptions(defaultOptions)
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                    .diskCache(new UnlimitedDiskCache(StorageUtils.getOwnCacheDirectory(application, fullDir())))
                    .diskCacheSize(100 * 1024 * 1024)
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .memoryCache(new LruMemoryCache(2 * 1024 * 1024)).memoryCacheSize(2 * 1024 * 1024)
                    .threadPoolSize(3)
                    .build();

            ImageLoader.getInstance().init(config);
        }
    }
}
