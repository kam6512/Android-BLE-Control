package com.rainbow.kam.android_ble_control.data;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.rainbow.kam.ble_gatt_manager.GattAttributes;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceItem implements Comparable<DeviceItem> { //카드 뷰 틀
    private final String extraName;
    private final String extraAddress;
    private final int extraBondState;
    private final int extraType;
    private int extraRssi;

    private SparseArray<String> bondList = GattAttributes.BOND_LIST;
    private SparseArray<String> typeList = GattAttributes.TYPE_LIST;


    public DeviceItem(BluetoothDevice bluetoothDevice, int rssi) {
        this.extraName = bluetoothDevice.getName();
        this.extraAddress = bluetoothDevice.getAddress();
        this.extraBondState = bluetoothDevice.getBondState();
        this.extraType = bluetoothDevice.getType();
        this.extraRssi = rssi;
    }


    public String getExtraName() {
        return this.extraName;
    }


    public String getExtraAddress() {
        return this.extraAddress;
    }


    public String getExtraBondState() {
        return bondList.get(extraBondState, bondList.get(bondList.keyAt(0)));
    }


    public String getExtraType() {
        return typeList.get(extraType, typeList.get(typeList.keyAt(0)));
    }


    public int getExtraRssi() {
        return this.extraRssi;
    }


    public void setExtraRssi(int rssi) {
        this.extraRssi = rssi;
    }


    @Override public boolean equals(@NonNull final Object o) {

        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        DeviceItem device = (DeviceItem) o;
        return !(extraAddress != null ? !extraAddress.equals(device.getExtraAddress()) : device.getExtraAddress() != null);
    }


    @Override public String toString() {
        return "Device{" +
                "extraName='" + this.extraName + '\'' +
                ", extraAddress='" + this.extraAddress + '\'' +
                '}';
    }


    @Override public int hashCode() {
        return this.extraAddress.hashCode();
    }


    @Override public int compareTo(@NonNull final DeviceItem anotherDevice) {
        return this.extraAddress.compareTo(anotherDevice.getExtraAddress());
    }
}
