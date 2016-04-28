package com.rainbow.kam.android_ble_control.ui.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.dagger.component.ActivityComponent;
import com.rainbow.kam.android_ble_control.ui.adapter.ProfileAdapter;
import com.rainbow.kam.android_ble_control.ui.view.ControlView;
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

import javax.inject.Inject;


/**
 * Created by kam6512 on 2015-11-27.
 */
@EActivity(R.layout.a_profile)
public class DeviceProfileActivity extends BaseActivity implements
        ProfileAdapter.OnGattItemClickListener,
        ControlView.OnControlListener,
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

    @ViewById(R.id.profile_recyclerView) RecyclerView recyclerView;

    @ViewById(R.id.profile_control) ControlView controlView;

    @Inject ProfileAdapter profileAdapter;

    private GattManager gattManager;

    private List<BluetoothGattService> bluetoothGattServices;
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

    private BluetoothGattCharacteristic controlCharacteristic;


    @Override protected void injectComponent(ActivityComponent component) {
        component.inject(this);
    }


    @AfterViews void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(deviceName);
        }
        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        deviceRSSI = RSSI_UNIT;
        deviceRSSITextView.setText(deviceRSSI);

    }


    @AfterViews void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(profileAdapter);
    }


    private void connectDevice() {
        try {
            gattManager.connect(deviceAddress);
            deviceStateTextView.setText(connectingLabel);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            deviceNameTextView.setText(e.getMessage());
            deviceAddressTextView.setText(e.getMessage());
            deviceRSSITextView.setText(e.getMessage());
            deviceStateTextView.setText(e.getMessage());
        }
    }


    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        } else {
            deviceStateTextView.setText(disconnectedLabel);
            finish();
        }
    }


    @Override protected void onResume() {
        super.onResume();
        gattManager = new GattManager(getApplication(), this);
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            BluetoothHelper.requestBluetoothEnable(this);
        }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @Override public void onBackPressed() {
        if (gattManager.isConnected()) {
            if (controlView.getVisibility() == View.VISIBLE) {
                controlView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                if (profileAdapter.isBackPressedAvailable()) {
                    disconnectDevice();
                } else {
                    showServiceList();
                }
            }
        } else {
            finish();
        }
    }


    @UiThread void showExceptionMessage(String msg) {
        Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG).show();
    }


    @UiThread @Override public void onDeviceConnected() {
        deviceStateTextView.setText(connectedLabel);
    }


    @Override public void onDeviceConnectFail(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @UiThread @Override public void onDeviceDisconnected() {
        deviceStateTextView.setText(disconnectedLabel);
        finish();
    }


    @Override public void onDeviceDisconnectFail(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @Override public void onServicesFound(BluetoothGatt bluetoothGatt) {
        bluetoothGattServices = bluetoothGatt.getServices();
        showServiceList();
    }


    @Override public void onServicesNotFound(Exception e) {
        showExceptionMessage(e.getMessage());
    }


    @Override public void onDeviceReady() {
        Snackbar.make(rootLayout, "onDeviceReady", Snackbar.LENGTH_LONG).show();
    }


    @Override public void onReadSuccess(final BluetoothGattCharacteristic ch) {
        controlView.newValueForCharacteristic(ch);
    }


    @Override public void onReadFail(Exception e) {
        controlView.setFail();
        showExceptionMessage(e.getMessage());
    }


    @Override public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
        controlView.newValueForCharacteristic(ch);
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


    @Override public void onRSSIMiss() {
        Snackbar.make(rootLayout, "onRSSIMiss", Snackbar.LENGTH_LONG).show();
    }


    @Override public void onServiceClickListener(int position) {
        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        showCharacteristicList();
    }


    @Override public void onCharacteristicClickListener(int position) {
        controlCharacteristic = bluetoothGattCharacteristics.get(position);
        controlView.setVisibility(View.VISIBLE);
    }


    @UiThread void showServiceList() {
        profileAdapter.setServiceList(bluetoothGattServices);
    }


    @UiThread void showCharacteristicList() {
        profileAdapter.setCharacteristicList(bluetoothGattCharacteristics);
    }


    @Override public void onControlReady() {
        recyclerView.setVisibility(View.GONE);
        controlView.init(deviceName, deviceAddress, controlCharacteristic);
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
