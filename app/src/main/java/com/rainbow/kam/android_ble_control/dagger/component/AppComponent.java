package com.rainbow.kam.android_ble_control.dagger.component;

import android.app.Application;

import com.rainbow.kam.android_ble_control.dagger.module.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by kam6512 on 2016-04-27.
 */

@Singleton
@Component(modules = {
        AppModule.class
})

public interface AppComponent {
    Application application();
}
