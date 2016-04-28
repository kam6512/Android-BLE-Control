package com.rainbow.kam.android_ble_control.dagger.module;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by kam6512 on 2016-04-27.
 */
@Module
public class AppModule {
    private final Application application;


    public AppModule(Application application) {
        this.application = application;
    }


    @Provides @Singleton Application application() {
        return application;
    }
}
