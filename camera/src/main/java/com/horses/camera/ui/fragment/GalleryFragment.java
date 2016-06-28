package com.horses.camera.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.horses.camera.R;
import com.horses.camera.business.PhotoItem;
import com.horses.camera.ui.adapter.GalleryAdapter;
import com.horses.camera.ui.application.CameraManager;
import com.horses.camera.ui.holder.GalleryHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Salvattore
 */
public class GalleryFragment extends Fragment implements GalleryHolder.CallbackGallery{

    private List<PhotoItem> photos = new ArrayList<>();

    private static final String PHOTO = "ARRAY";

    protected RecyclerView albums;

    public static GalleryFragment init(ArrayList<PhotoItem> photos) {

        GalleryFragment fragment = new GalleryFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PHOTO, photos);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albums = (RecyclerView) view.findViewById(R.id.albums);

        photos = setPhotos();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        albums.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        albums.setAdapter(new GalleryAdapter(photos, this));
    }

    @SuppressWarnings("unchecked")
    private ArrayList<PhotoItem> setPhotos(){

        return (ArrayList<PhotoItem>) getArguments().getSerializable(PHOTO);
    }

    @Override
    public void onClickPhoto(PhotoItem item) {

        CameraManager.getInst().processCropper(getActivity(), item);
    }
}
