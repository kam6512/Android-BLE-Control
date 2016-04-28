package com.rainbow.kam.android_ble_control;

import android.app.Application;

import com.rainbow.kam.android_ble_control.dagger.component.AppComponent;
import com.rainbow.kam.android_ble_control.dagger.component.DaggerAppComponent;
import com.rainbow.kam.android_ble_control.dagger.module.AppModule;

/**
 * Created by kam6512 on 2016-04-27.
 */
public final class App extends Application {

    private AppComponent appComponent;


    @Override public void onCreate() {
        super.onCreate();
        initializeInjector();
    }


    private void initializeInjector() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }


    public AppComponent getAppComponent() {

        if (appComponent == null) {
            initializeInjector();
        }
        return appComponent;
    }
}
