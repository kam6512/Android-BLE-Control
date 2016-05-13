package com.rainbow.kam.android_ble_control.ui.adapter.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

/**
 * Created by Kang Young Won on 2016-05-13.
 */
public interface OnGattItemClickListener {
    void onServiceClickListener(BluetoothGattService bluetoothGattService);

    void onCharacteristicClickListener(BluetoothGattCharacteristic bluetoothGattCharacteristic);
}