package com.rainbow.kam.android_ble_control.dagger.module;

import android.app.Activity;
import android.content.Context;

import com.rainbow.kam.android_ble_control.dagger.scope.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by kam6512 on 2016-04-27.
 */
@Module
public class ActivityModule {
    private final Context context;
    private final Activity activity;


    public ActivityModule(Activity activity) {
        this.context = activity;
        this.activity = activity;
    }


    @Provides @PerActivity Activity activity() {
        return activity;
    }


    @Provides @PerActivity Context context() {
        return context;
    }
}
