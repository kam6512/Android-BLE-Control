package com.rainbow.kam.android_ble_control.ui.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.data.adapter.CharacteristicAdapter;
import com.rainbow.kam.android_ble_control.data.adapter.ServiceAdapter;
import com.rainbow.kam.android_ble_control.ui.fragment.CharacteristicListFragment;
import com.rainbow.kam.android_ble_control.ui.fragment.CharacteristicListFragment_;
import com.rainbow.kam.android_ble_control.ui.fragment.ControlFragment;
import com.rainbow.kam.android_ble_control.ui.fragment.ControlFragment_;
import com.rainbow.kam.android_ble_control.ui.fragment.ServiceListFragment;
import com.rainbow.kam.android_ble_control.ui.fragment.ServiceListFragment_;
import com.rainbow.kam.ble_gatt_manager.BluetoothHelper;
import com.rainbow.kam.ble_gatt_manager.GattCustomCallbacks;
import com.rainbow.kam.ble_gatt_manager.GattManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;


/**
 * Created by kam6512 on 2015-11-27.
 */
@EActivity(R.layout.a_profile)
public class DeviceProfileActivity extends AppCompatActivity implements
        ServiceListFragment.OnServiceReadyListener,
        CharacteristicListFragment.OnCharacteristicReadyListener,
        ServiceAdapter.OnServiceItemClickListener,
        CharacteristicAdapter.OnCharacteristicItemClickListener,
        ControlFragment.OnControlListener,
        GattCustomCallbacks {

    private final String TAG = getClass().getSimpleName();

    @StringRes(R.string.bt_rssi_unit) String RSSI_UNIT;
    @StringRes(R.string.bt_connecting) String connectingLabel;
    @StringRes(R.string.bt_connected) String connectedLabel;
    @StringRes(R.string.bt_disconnected) String disconnectedLabel;


    @Extra(ScanActivity.KEY_DEVICE_NAME) String deviceName;
    @Extra(ScanActivity.KEY_DEVICE_ADDRESS) String deviceAddress;
    private String deviceRSSI;


    @ViewById(R.id.profile_toolbar) Toolbar toolbar;
    @ViewById(R.id.profile_name) TextView deviceNameTextView;
    @ViewById(R.id.profile_address) TextView deviceAddressTextView;
    @ViewById(R.id.profile_rssi) TextView deviceRSSITextView;
    @ViewById(R.id.profile_root) CoordinatorLayout rootLayout;

    @ViewById(R.id.profile_state) TextView deviceStateTextView;

    private FragmentManager fragmentManager;

    private ServiceListFragment_ serviceListFragment;
    private CharacteristicListFragment_ characteristicListFragment;
    private ControlFragment_ controlFragment;

    private static final String TAG_SERVICE_FRAGMENT = "SERVICE";
    private static final String TAG_CHARACTERISTIC_FRAGMENT = "CHARACTERISTIC";
    private static final String TAG_CONTROL_FRAGMENT = "CONTROL";

    private GattManager gattManager;

    private List<BluetoothGattService> bluetoothGattServices;
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

    private BluetoothGattCharacteristic controlCharacteristic;

    private final Runnable deviceDisconnect = () -> {
        deviceStateTextView.setText(disconnectedLabel);
        new Handler().postDelayed(DeviceProfileActivity.this::finish, 500);
    };


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragments();
    }


    @AfterViews void setToolbar() {
        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        deviceRSSI = "-" + RSSI_UNIT;
        deviceRSSITextView.setText(deviceRSSI);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(deviceName);
        }
    }


    private void setFragments() {
        fragmentManager = getSupportFragmentManager();

        serviceListFragment = new ServiceListFragment_();
        characteristicListFragment = new CharacteristicListFragment_();
        controlFragment = new ControlFragment_();
        fragmentManager.beginTransaction()
                .replace(R.id.profile_fragment_view, serviceListFragment).commit();
    }


    private void registerBluetooth() {
        gattManager = new GattManager(getApplication(), this);
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            BluetoothHelper.requestBluetoothEnable(this);
        }
    }


    private synchronized void connectDevice() {
        deviceStateTextView.setText(connectingLabel);
        try {
            gattManager.connect(deviceAddress);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            deviceNameTextView.setText(e.getMessage());
            deviceAddressTextView.setText(e.getMessage());
            deviceRSSITextView.setText(e.getMessage());
            deviceStateTextView.setText(e.getMessage());
        }
    }


    private synchronized void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        } else {
            runOnUiThread(deviceDisconnect);
        }
    }


    @Override protected void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @Override protected void onPause() {
        super.onPause();
        if (gattManager.isConnected()) {
            disconnectDevice();
        } else {
            finish();
        }
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
    }


    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @Override public void onBackPressed() {
        if (gattManager.isConnected()) {
            if (serviceListFragment.isVisible()) {
                disconnectDevice();
            } else {
                super.onBackPressed();
            }
        } else {
            finish();
        }
    }


    @UiThread @Override public void onDeviceConnected() {
        deviceStateTextView.setText(connectedLabel);
    }


    @Override public void onDeviceConnectFail(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @UiThread @Override public void onDeviceDisconnected() {
        deviceDisconnect.run();
    }


    @Override public void onDeviceDisconnectFail(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @UiThread @Override public void onServicesFound(BluetoothGatt bluetoothGatt) {
        bluetoothGattServices = bluetoothGatt.getServices();
        onServiceReady();
    }


    @Override public void onServicesNotFound(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @Override public void onDeviceReady() {
        Snackbar.make(rootLayout, "onDeviceReady", Snackbar.LENGTH_LONG).show();
    }


    @UiThread @Override public void onReadSuccess(final BluetoothGattCharacteristic ch) {
        controlFragment.newValueForCharacteristic(ch);
    }


    @UiThread @Override public void onReadFail(Exception e) {
        controlFragment.setFail();
        showExceptionMessage(e.getMessage());
    }


    @UiThread @Override public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
        controlFragment.newValueForCharacteristic(ch);
    }


    @Override public void onSetNotificationSuccess() {
        Snackbar.make(rootLayout, "onSetNotificationSuccess", Snackbar.LENGTH_LONG).show();
    }


    @Override public void onSetNotificationFail(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @Override public void onWriteSuccess() {
        Snackbar.make(rootLayout, "onWriteSuccess", Snackbar.LENGTH_LONG).show();
    }


    @Override public void onWriteFail(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @UiThread @Override public void onRSSIUpdate(final int rssi) {
        deviceRSSI = rssi + RSSI_UNIT;
        deviceRSSITextView.setText(deviceRSSI);
    }


    @UiThread void showExceptionMessage(String msg) {
        Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG).show();
    }


    @Override public void onRSSIMiss() {
        Snackbar.make(rootLayout, "onRSSIMiss", Snackbar.LENGTH_LONG).show();
    }


    @Override public void onServiceItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack(TAG_CHARACTERISTIC_FRAGMENT).replace(R.id.profile_fragment_view, characteristicListFragment).commit();
        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        onCharacteristicReady();

    }


    @Override public void onCharacteristicItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack(TAG_CONTROL_FRAGMENT).replace(R.id.profile_fragment_view, controlFragment).commit();
        if (!bluetoothGattCharacteristics.get(position).equals(controlCharacteristic)) {
            controlCharacteristic = bluetoothGattCharacteristics.get(position);
        }
    }


    @Override public void onServiceReady() {
        if (serviceListFragment.isVisible() && bluetoothGattServices != null) {
            runOnUiThread(() -> serviceListFragment.setServiceList(bluetoothGattServices));
        }
    }


    @Override public void onCharacteristicReady() {
        if (characteristicListFragment.isVisible() && bluetoothGattServices != null) {
            runOnUiThread(() -> characteristicListFragment.setCharacteristicList(bluetoothGattCharacteristics));
        }
    }


    @Override public void onControlReady() {
        controlFragment.init(deviceName, deviceAddress, controlCharacteristic);
    }


    @Override public void setNotification(boolean isNotificationEnable) {
        gattManager.setNotification(controlCharacteristic, isNotificationEnable);
    }


    @Override public void setReadValue() {
        gattManager.readValue(controlCharacteristic);
    }


    @Override public void setWriteValue(byte[] data) {
        gattManager.writeValue(controlCharacteristic, data);
    }
}
