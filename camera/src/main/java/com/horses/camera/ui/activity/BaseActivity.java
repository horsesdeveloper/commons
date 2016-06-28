package com.horses.camera.ui.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.horses.camera.R;
import com.horses.camera.ui.application.CameraManager;

/**
 * @author Brian Salvattore
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    protected Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        CameraManager.getInst().addActivity(this);

        setContentView(getView());

        activity = this;

        onCreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraManager.getInst().removeActivity(this);
    }

    protected abstract int getView();

    protected abstract void onCreate();

    protected void setSupportActionBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    @SuppressWarnings("ConstantConditions")
    protected void setSupportActionBar(String title){

        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(Color.WHITE);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
    }
}
