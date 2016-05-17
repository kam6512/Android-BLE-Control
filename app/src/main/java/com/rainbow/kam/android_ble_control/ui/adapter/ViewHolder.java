package com.rainbow.kam.android_ble_control.ui.adapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rainbow.kam.android_ble_control.data.DeviceItem;

/**
 * Created by Kang Young Won on 2016-05-17.
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
        super(itemView);
    }


    public void bindViews(@NonNull DeviceItem deviceItem) {

    }


    public void bindViews(final BluetoothGattService bluetoothGattService) {

    }


    public void bindViews(final BluetoothGattCharacteristic characteristicItem) {

    }
}
