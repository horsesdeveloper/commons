package com.horses.camera.utils;

import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * @author Brian Salvattore
 */
public class ImageLoaderUtils {

    public static void displayLocalImage(String uri, ImageView imageView, DisplayImageOptions options) {

        ImageLoader
                .getInstance()
                .displayImage("file://" + uri, new ImageViewAware(imageView), options, new DisplayListener());
    }

    public static void displayLocalImage(String uri, ImageView imageView) {

        displayLocalImage(uri, imageView, DisplayListener.optionsImage);
    }
}
