package com.horses.camera.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.horses.camera.business.PhotoItem;
import com.horses.camera.ui.holder.GalleryHolder;

import java.util.List;

/**
 * @author Brian Salvattore
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {

    private List<PhotoItem> photos;
    private GalleryHolder.CallbackGallery callback;

    public GalleryAdapter(List<PhotoItem> photos, GalleryHolder.CallbackGallery callback) {
        this.photos = photos;
        this.callback = callback;
    }

    @Override
    public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return GalleryHolder.init(parent);
    }

    @Override
    public void onBindViewHolder(GalleryHolder holder, int position) {

        holder.setPhoto(photos.get(position), callback);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
}
