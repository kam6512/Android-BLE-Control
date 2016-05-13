package com.rainbow.kam.android_ble_control.ui.view;

/**
 * Created by Kang Young Won on 2016-05-13.
 */
public interface OnControlListener {
    void onControlReady();

    void setNotification(boolean isNotificationEnable);

    void setReadValue();

    void setWriteValue(byte[] data);
}
