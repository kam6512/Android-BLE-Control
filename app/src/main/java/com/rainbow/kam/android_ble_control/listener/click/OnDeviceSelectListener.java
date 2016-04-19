package com.rainbow.kam.android_ble_control.listener.click;

import com.rainbow.kam.android_ble_control.data.DeviceItem;

/**
 * Created by kam6512 on 2016-04-19.
 */
public interface OnDeviceSelectListener {
    void onDeviceSelect(DeviceItem device);

    void onDeviceUnSelected();
}
