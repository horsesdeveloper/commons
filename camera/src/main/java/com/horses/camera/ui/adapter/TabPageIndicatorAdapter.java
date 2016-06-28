package com.horses.camera.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.horses.camera.R;
import com.horses.camera.business.Album;
import com.horses.camera.ui.fragment.GalleryFragment;
import com.horses.camera.utils.FileUtils;
import com.horses.camera.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Brian Salvattore
 */
public class TabPageIndicatorAdapter extends FragmentPagerAdapter {

    private Map<String, Album> albums;
    private List<String> paths;

    private AppCompatActivity activity;

    public TabPageIndicatorAdapter(AppCompatActivity activity, Map<String, Album> albums, List<String> paths) {
        super(activity.getSupportFragmentManager());

        this.activity = activity;
        this.albums = albums;
        this.paths = paths;
    }

    @Override
    public Fragment getItem(int position) {
        return GalleryFragment.init(albums.get(paths.get(position)).getPhotos());
    }

    @Override
    public CharSequence getPageTitle(int position) {

        Album album = albums.get(paths.get(position % paths.size()));

        if (StringUtils.equalsIgnoreCase(FileUtils.getInst().getSystemPhotoPath(), album.getAlbumUri())) {

            return activity.getResources().getString(R.string.title_album);
        }
        else if (album.getTitle().length() > 13) {

            return album.getTitle().substring(0, 11) + "...";
        }

        return album.getTitle();
    }

    @Override
    public int getCount() {
        return paths.size();
    }
}
