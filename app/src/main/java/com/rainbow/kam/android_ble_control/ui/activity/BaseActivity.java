package com.rainbow.kam.android_ble_control.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rainbow.kam.android_ble_control.App;
import com.rainbow.kam.android_ble_control.dagger.component.ActivityComponent;
import com.rainbow.kam.android_ble_control.dagger.component.AppComponent;
import com.rainbow.kam.android_ble_control.dagger.component.DaggerActivityComponent;
import com.rainbow.kam.android_ble_control.dagger.module.ActivityModule;

/**
 * Created by kam6512 on 2016-04-27.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ActivityComponent component;


    private ActivityComponent getActivityComponent() {
        if (component == null) {
            component = DaggerActivityComponent.builder()
                    .appComponent(getApplicationComponent())
                    .activityModule(new ActivityModule(this))
                    .build();
        }
        return component;
    }


    private AppComponent getApplicationComponent() {
        return ((App) getApplication()).getAppComponent();
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectComponent(getActivityComponent());
    }


    abstract protected void injectComponent(ActivityComponent component);
}
