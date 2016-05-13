package com.rainbow.kam.android_ble_control.data;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;
import com.rainbow.kam.ble_gatt_manager.legacy.GattAttributes;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceItem implements Comparable<DeviceItem> { //카드 뷰 틀
    private final String extraName;
    private final String extraAddress;
    private final int extraBondState;
    private final int extraType;
    private final int extraRssi;


    private static final String UNKNOWN = GattAttributes.UNKNOWN;


    public DeviceItem(BluetoothDevice bluetoothDevice, int rssi) {
        String name = bluetoothDevice.getName();
        if (Strings.isNullOrEmpty(name)) {
            this.extraName = UNKNOWN;
        } else {
            this.extraName = bluetoothDevice.getName();
        }
        this.extraAddress = bluetoothDevice.getAddress();
        this.extraBondState = bluetoothDevice.getBondState();
        this.extraType = bluetoothDevice.getType();
        this.extraRssi = rssi;
    }


    public final String getName() {
        return this.extraName;
    }


    public final String getAddress() {
        return this.extraAddress;
    }


    public final String getBondState() {
        return GattAttributes.getBond(extraBondState);
    }


    public final String getType() {
        return GattAttributes.getType(extraType);
    }


    public final int getRssi() {
        return this.extraRssi;
    }


    @Override public boolean equals(@NonNull final Object o) {

        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        DeviceItem device = (DeviceItem) o;
        return !(extraAddress != null ? !extraAddress.equals(device.getAddress()) : device.getAddress() != null);
    }


    @Override public String toString() {
        return "DeviceItem{" +
                "extraName='" + extraName + '\'' +
                ", extraAddress='" + extraAddress + '\'' +
                ", extraBondState=" + extraBondState +
                ", extraType=" + extraType +
                ", extraRssi=" + extraRssi +
                '}';
    }


    @Override public int hashCode() {
        return this.extraAddress.hashCode();
    }


    @Override public int compareTo(@NonNull final DeviceItem anotherDevice) {
        return this.extraAddress.compareTo(anotherDevice.getAddress());
    }
}
