package com.horses.camera.ui.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.horses.camera.R;
import com.horses.camera.business.Album;
import com.horses.camera.ui.adapter.TabPageIndicatorAdapter;
import com.horses.camera.utils.Constants;
import com.horses.camera.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Brian Salvattore
 */
public class GalleryActivity extends BaseActivity {

    private List<String> paths = new ArrayList<>();

    protected TabLayout tabLayout;

    protected ViewPager viewPager;

    @Override
    protected int getView() {
        return R.layout.activity_gallery;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate() {

        tabLayout = (TabLayout) findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.pager);

        setSupportActionBar();
        setSupportActionBar(getResources().getString(R.string.title_gallery));

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(android.R.color.white));

        Map<String, Album> albums = ImageUtils.findGalleries(this, paths);

        viewPager.setAdapter(new TabPageIndicatorAdapter(this, albums, paths));

        tabLayout.setupWithViewPager(viewPager);
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
