package com.rainbow.kam.android_ble_control.dagger;

/**
 * Created by kam6512 on 2016-04-27.
 */

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Scope
@Retention(RUNTIME)
public @interface PerActivity {
}