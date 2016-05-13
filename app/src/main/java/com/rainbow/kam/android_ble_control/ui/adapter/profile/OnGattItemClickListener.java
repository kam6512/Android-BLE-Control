package com.rainbow.kam.android_ble_control.ui.adapter.profile;

/**
 * Created by Kang Young Won on 2016-05-13.
 */
public interface OnGattItemClickListener {
    void onServiceClickListener(int position);

    void onCharacteristicClickListener(int position);
}