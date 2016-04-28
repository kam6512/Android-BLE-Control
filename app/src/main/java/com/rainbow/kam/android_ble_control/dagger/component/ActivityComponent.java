package com.rainbow.kam.android_ble_control.dagger.component;

import android.app.Activity;

import com.rainbow.kam.android_ble_control.dagger.PerActivity;
import com.rainbow.kam.android_ble_control.dagger.module.ActivityModule;
import com.rainbow.kam.android_ble_control.ui.activity.DeviceProfileActivity;
import com.rainbow.kam.android_ble_control.ui.activity.ScanActivity;

import dagger.Component;

/**
 * Created by Kang Young Won on 2016-04-27.
 */
@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {ActivityModule.class})
public interface ActivityComponent {
    Activity activity();

    void inject(ScanActivity activity);

    void inject(DeviceProfileActivity activity);
}
