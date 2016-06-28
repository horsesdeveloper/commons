package com.horses.camera.ui.holder;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.horses.camera.R;
import com.horses.camera.business.PhotoItem;
import com.horses.camera.utils.DisplayListener;
import com.horses.camera.utils.DistanceUtil;
import com.horses.camera.utils.ImageLoaderUtils;

/**
 * @author Brian Salvattore
 */
public class GalleryHolder extends RecyclerView.ViewHolder {

    protected ImageView image;

    public GalleryHolder(View itemView) {
        super(itemView);

        image = (ImageView) itemView.findViewById(R.id.image);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static GalleryHolder init(ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);

        int width = DistanceUtil.getCameraAlbumWidth();

        ViewPager.LayoutParams params = (ViewPager.LayoutParams) parent.getLayoutParams();
        params.width = width;
        params.height = width;

        view.setLayoutParams(params);

        return new GalleryHolder(view);
    }

    public void setPhoto(final PhotoItem photo, final CallbackGallery callback) {

        ImageLoaderUtils.displayLocalImage(photo.getImageUri(), image, DisplayListener.optionsImage);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickPhoto(photo);
            }
        });
    }

    public interface CallbackGallery {
        void onClickPhoto(PhotoItem item);
    }

}
