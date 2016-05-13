package com.rainbow.kam.android_ble_control.data.scan;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;
import com.rainbow.kam.ble_gatt_manager.legacy.GattAttributes;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceItem implements Comparable<DeviceItem> { //카드 뷰 틀
    private String name;
    private final String address;
    private final int bondState;
    private final int type;
    private final int rssi;


    private static final String UNKNOWN = GattAttributes.UNKNOWN;


    public DeviceItem(BluetoothDevice bluetoothDevice, int rssi) {
        String name = bluetoothDevice.getName();
        if (Strings.isNullOrEmpty(name)) {
            this.name = UNKNOWN;
        }
        this.address = bluetoothDevice.getAddress();
        this.bondState = bluetoothDevice.getBondState();
        this.type = bluetoothDevice.getType();
        this.rssi = rssi;
    }


    public final String getName() {
        return this.name;
    }


    public final String getAddress() {
        return this.address;
    }


    public final String getBondState() {
        return GattAttributes.getBond(bondState);
    }


    public final String getType() {
        return GattAttributes.getType(type);
    }


    public final int getRssi() {
        return this.rssi;
    }


    @Override public boolean equals(@NonNull final Object o) {

        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        DeviceItem device = (DeviceItem) o;
        return !(address != null ? !address.equals(device.getAddress()) : device.getAddress() != null);
    }


    @Override public String toString() {
        return "DeviceItem{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", bondState=" + bondState +
                ", type=" + type +
                ", rssi=" + rssi +
                '}';
    }


    @Override public int hashCode() {
        return this.address.hashCode();
    }


    @Override public int compareTo(@NonNull final DeviceItem anotherDevice) {
        return this.address.compareTo(anotherDevice.getAddress());
    }
}
