package com.rainbow.kam.android_ble_control.ui.activity;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.TextView;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.dagger.component.ActivityComponent;
import com.rainbow.kam.android_ble_control.data.DeviceItem;
import com.rainbow.kam.android_ble_control.ui.adapter.profile.OnGattItemClickListener;
import com.rainbow.kam.android_ble_control.ui.adapter.profile.ProfileAdapter;
import com.rainbow.kam.android_ble_control.ui.view.ControlView;
import com.rainbow.kam.android_ble_control.ui.view.OnControlListener;
import com.rainbow.kam.ble_gatt_manager.BluetoothHelper;
import com.rainbow.kam.ble_gatt_manager.legacy.GattCustomCallbacks;
import com.rainbow.kam.ble_gatt_manager.legacy.GattManager;
import com.rainbow.kam.ble_gatt_manager.exceptions.GattException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import javax.inject.Inject;


/**
 * Created by kam6512 on 2015-11-27.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_profile)
public class DeviceProfileActivity extends BaseActivity implements
        OnGattItemClickListener,
        OnControlListener,
        GattCustomCallbacks {

    private final String TAG = getClass().getSimpleName();

    @StringRes(R.string.profile_error) String NONE;

    private static final int GONE = View.GONE;
    private static final int VISIBLE = View.VISIBLE;

    private GattManager gattManager;
    private BluetoothGattCharacteristic controlCharacteristic;

    @StringRes(R.string.bt_rssi_unit) String RSSI_UNIT;
    @StringRes(R.string.bt_connecting) String connectingLabel;
    @StringRes(R.string.bt_connected) String connectedLabel;
    @StringRes(R.string.bt_disconnected) String disconnectedLabel;


    @Extra(DeviceItem.KEY_DEVICE_NAME) String deviceName;
    @Extra(DeviceItem.KEY_DEVICE_ADDRESS) String deviceAddress;
    private String deviceRSSI;

    @ViewById(R.id.profile_root) CoordinatorLayout rootLayout;

    @ViewById(R.id.profile_toolbar) Toolbar toolbar;
    @ViewById(R.id.profile_name) TextView deviceNameTextView;
    @ViewById(R.id.profile_rssi) TextView deviceRSSITextView;
    @ViewById(R.id.profile_address) TextView deviceAddressTextView;
    @ViewById(R.id.profile_state) TextView deviceStateTextView;

    @ViewById(R.id.profile_recyclerView) RecyclerView recyclerView;

    @ViewById(R.id.profile_control) ControlView controlView;

    @Inject ProfileAdapter profileAdapter;


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
            showMessage(e.getMessage());
            showNoneValue();
        }
    }


    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        } else {
            showDisconnected();
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


    @OptionsItem(android.R.id.home) void onBackMenuItemSelected() {
        finish();
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
            toBackStack();
        } else {
            finish();
        }
    }


    private void toBackStack() {
        if (controlView.getVisibility() == VISIBLE) {
            dismissControlView();
        } else {
            if (profileAdapter.hasBackStack()) {
                profileAdapter.showServiceList();
            } else {
                disconnectDevice();
            }
        }
    }


    private void showNoneValue() {
        deviceNameTextView.setText(NONE);
        deviceAddressTextView.setText(NONE);
        deviceRSSITextView.setText(NONE);
        deviceStateTextView.setText(NONE);
    }


    @UiThread void showDisconnected() {
        deviceStateTextView.setText(disconnectedLabel);
        finish();
    }


    private void showControlView() {
        recyclerView.setVisibility(GONE);
        controlView.setVisibility(VISIBLE);
    }


    private void dismissControlView() {
        recyclerView.setVisibility(VISIBLE);
        controlView.setVisibility(GONE);
    }


    @UiThread void showMessage(String msg) {
        Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG).show();
        Log.i(TAG, msg);
    }


    @UiThread @Override public void onDeviceConnected() {
        deviceStateTextView.setText(connectedLabel);
    }


    @UiThread @Override public void onDeviceDisconnected() {
        showDisconnected();
    }


    @UiThread @Override
    public void onServicesFound(BluetoothGatt bluetoothGatt) {
        profileAdapter.setServiceList(bluetoothGatt.getServices());
    }


    @Override public void onDeviceReady() {
        showMessage("onDeviceReady");
    }


    @UiThread @Override
    public void onReadSuccess(final BluetoothGattCharacteristic ch) {
        controlView.newValueForCharacteristic(ch);
    }


    @UiThread @Override
    public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
        controlView.newValueForCharacteristic(ch);
    }


    @Override public void onSetNotificationSuccess() {
        showMessage("onSetNotificationSuccess");
    }


    @Override public void onWriteSuccess() {
        showMessage("onWriteSuccess");
    }


    @UiThread @Override public void onRSSIUpdate(final int rssi) {
        deviceRSSI = rssi + RSSI_UNIT;
        deviceRSSITextView.setText(deviceRSSI);
    }


    @UiThread @Override public void onError(GattException e) {
        showMessage(e.getMessage());
        if (e instanceof ReadCharacteristicException) {
            controlView.setFail();
        }
    }


    @UiThread @Override
    public void onServiceClickListener(BluetoothGattService bluetoothGattService) {
        profileAdapter.setCharacteristicList(bluetoothGattService.getCharacteristics());
    }


    @UiThread @Override
    public void onCharacteristicClickListener(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        controlCharacteristic = bluetoothGattCharacteristic;
        showControlView();
    }


    @UiThread @Override public void onControlReady() {
        controlView.init(deviceName, deviceAddress, controlCharacteristic);
    }


    @Background @Override
    public void setNotification(boolean isNotificationEnable) {
        gattManager.setNotification(controlCharacteristic, isNotificationEnable);
    }


    @Background @Override public void setReadValue() {
        gattManager.readValue(controlCharacteristic);
    }


    @Background @Override public void setWriteValue(byte[] data) {
        gattManager.writeValue(controlCharacteristic, data);
    }
}
