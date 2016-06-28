package com.horses.camera.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.horses.camera.business.Album;
import com.horses.camera.business.PhotoItem;
import com.horses.camera.ui.application.CameraManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brian Salvattore
 */
public class ImageUtils {

    public static int getMiniSize(String imagePath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        return Math.min(options.outHeight, options.outWidth);
    }

    public static boolean isSquare(String imagePath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        return options.outHeight == options.outWidth;
    }

    public static boolean isSquare(Uri imageUri) {

        ContentResolver resolver = CameraManager.getApplication().getContentResolver();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {

            BitmapFactory.decodeStream(resolver.openInputStream(imageUri), null, options);
        }
        catch (Exception e) {

            e.printStackTrace();
            return false;
        }

        return options.outHeight == options.outWidth;
    }

    @SuppressLint("SimpleDateFormat")
    public static String saveToFile(String fileFolderStr, boolean isDir, Bitmap croppedImage) throws IOException {
        File jpgFile;

        if (isDir) {

            File fileFolder = new File(fileFolderStr);
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename = format.format(date) + ".jpg";
            if (!fileFolder.exists()) {
                FileUtils.getInst().mkdir(fileFolder);
            }
            jpgFile = new File(fileFolder, filename);
        }
        else {
            jpgFile = new File(fileFolderStr);
            if (!jpgFile.getParentFile().exists()) {
                FileUtils.getInst().mkdir(jpgFile.getParentFile());
            }
        }

        FileOutputStream outputStream = new FileOutputStream(jpgFile);

        croppedImage.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        IOUtil.closeStream(outputStream);

        return jpgFile.getPath();
    }

    public static Bitmap decodeBitmapWithOrientation(String pathName, int width, int height) {
        return decodeBitmapWithSize(pathName, width, height, false);
    }

    public static Bitmap decodeBitmapWithOrientationMax(String pathName, int width, int height) {
        return decodeBitmapWithSize(pathName, width, height, true);
    }

    @SuppressWarnings({"deprecation", "SuspiciousNameCombination"})
    private static Bitmap decodeBitmapWithSize(String pathName, int width, int height,
                                               boolean useBigger) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inInputShareable = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFile(pathName, options);

        int decodeWidth = width, decodeHeight = height;
        final int degrees = getImageDegrees(pathName);
        if (degrees == 90 || degrees == 270) {
            decodeWidth = height;
            decodeHeight = width;
        }

        if (useBigger) {
            options.inSampleSize = (int) Math.min(((float) options.outWidth / decodeWidth),
                    ((float) options.outHeight / decodeHeight));
        } else {
            options.inSampleSize = (int) Math.max(((float) options.outWidth / decodeWidth),
                    ((float) options.outHeight / decodeHeight));
        }

        options.inJustDecodeBounds = false;
        Bitmap sourceBm = BitmapFactory.decodeFile(pathName, options);

        return imageWithFixedRotation(sourceBm, degrees);
    }

    public static int getImageDegrees(String pathName) {

        int degrees = 0;

        try {

            ExifInterface exifInterface = new ExifInterface(pathName);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degrees = 270;
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return degrees;
    }

    public static Bitmap imageWithFixedRotation(Bitmap bm, int degrees) {

        if (bm == null || bm.isRecycled())
            return null;

        if (degrees == 0)
            return bm;

        final Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        if (result != bm)
            bm.recycle();

        return result;
    }


    public static float getImageRadio(ContentResolver resolver, Uri fileUri) {

        InputStream inputStream = null;

        try {

            inputStream = resolver.openInputStream(fileUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            int initWidth = options.outWidth;
            int initHeight = options.outHeight;

            return initHeight > initWidth ? (float) initHeight / (float) initWidth
                    : (float) initWidth / (float) initHeight;
        }
        catch (Exception e) {

            e.printStackTrace();
            return 1;
        }
        finally {

            IOUtil.closeStream(inputStream);
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "UnusedAssignment"})
    public static Bitmap byteToBitmap(byte[] imgByte) {

        InputStream input;
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        input = new ByteArrayInputStream(imgByte);

        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));

        bitmap = (Bitmap) softRef.get();

        if (imgByte != null)
            imgByte = null;

        try {

            if (input != null) {
                input.close();
            }
        }
        catch (IOException e) {

            e.printStackTrace();
        }

        return bitmap;
    }

    @SuppressLint("Recycle")
    public static Map<String, Album> findGalleries(Context context, List<String> paths) {

        paths.clear();
        paths.add(FileUtils.getInst().getSystemPhotoPath());

        String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.SIZE + ">?",
                new String[] { "100000" },
                MediaStore.Images.Media.DATE_ADDED + " desc");

        assert cursor != null;
        cursor.moveToFirst();

        Map<String, Album> galleries = new HashMap<>();
        while (cursor.moveToNext()) {
            String data = cursor.getString(1);
            if (data.lastIndexOf("/") < 1) {
                continue;
            }
            String sub = data.substring(0, data.lastIndexOf("/"));
            if (!galleries.keySet().contains(sub)) {
                String name = sub.substring(sub.lastIndexOf("/") + 1, sub.length());
                if (!paths.contains(sub)) {
                    paths.add(sub);
                }
                galleries.put(sub, new Album(name, sub, new ArrayList<PhotoItem>()));
            }

            galleries.get(sub).getPhotos().add(new PhotoItem(data, (long) (cursor.getInt(2)) * 1000));
        }

        ArrayList<PhotoItem> sysPhotos = FileUtils.getInst().findPicsInDir(FileUtils.getInst().getSystemPhotoPath());

        if (!sysPhotos.isEmpty()) {

            galleries.put(FileUtils.getInst().getSystemPhotoPath(), new Album("Gallery", FileUtils.getInst().getSystemPhotoPath(), sysPhotos));
        }
        else {

            galleries.remove(FileUtils.getInst().getSystemPhotoPath());
            paths.remove(FileUtils.getInst().getSystemPhotoPath());
        }

        return galleries;
    }

    public interface LoadImageCallback {
        void callback(Bitmap result);
    }

    public static void asyncLoadImage(Context context, Uri imageUri, LoadImageCallback callback) {
        new LoadImageUriTask(context, imageUri, callback).execute();
    }

    private static class LoadImageUriTask extends AsyncTask<Void, Void, Bitmap> {

        private final Uri imageUri;
        private final Context context;
        private LoadImageCallback callback;

        public LoadImageUriTask(Context context, Uri imageUri, LoadImageCallback callback) {
            this.imageUri = imageUri;
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {

                InputStream inputStream;

                if (imageUri.getScheme().startsWith("http") || imageUri.getScheme().startsWith("https")) {

                    inputStream = new URL(imageUri.toString()).openStream();
                }
                else {

                    inputStream = context.getContentResolver().openInputStream(imageUri);
                }

                return BitmapFactory.decodeStream(inputStream);
            }
            catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            callback.callback(result);
        }
    }

    public static void asyncLoadSmallImage(Context context, Uri imageUri, LoadImageCallback callback) {
        new LoadSmallPicTask(context, imageUri, callback).execute();
    }

    private static class LoadSmallPicTask extends AsyncTask<Void, Void, Bitmap> {

        private final Uri imageUri;
        private final Context context;
        private LoadImageCallback callback;

        public LoadSmallPicTask(Context context, Uri imageUri, LoadImageCallback callback) {
            this.imageUri = imageUri;
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return getResizedBitmap(context, imageUri, 300, 300);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            callback.callback(result);
        }

    }

    public static Bitmap getResizedBitmap(Context context, Uri imageUri, int width, int height) {
        InputStream inputStream = null;

        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);

            options.outWidth = width;
            options.outHeight = height;
            options.inJustDecodeBounds = false;
            IOUtil.closeStream(inputStream);
            inputStream = context.getContentResolver().openInputStream(imageUri);

            return BitmapFactory.decodeStream(inputStream, null, options);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        finally {

            IOUtil.closeStream(inputStream);
        }

        return null;
    }


}
