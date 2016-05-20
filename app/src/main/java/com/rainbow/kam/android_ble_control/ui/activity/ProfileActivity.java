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

import com.google.common.collect.Lists;
import com.rainbow.kam.android_ble_control.BuildConfig;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.dagger.component.ActivityComponent;
import com.rainbow.kam.android_ble_control.ui.adapter.profile.OnGattItemClickListener;
import com.rainbow.kam.android_ble_control.ui.adapter.profile.ProfileAdapter;
import com.rainbow.kam.android_ble_control.ui.view.ControlView;
import com.rainbow.kam.android_ble_control.ui.view.OnControlListener;
import com.rainbow.kam.android_ble_control.ui.view.PropertyDialog;
import com.rainbow.kam.ble_gatt_manager.GattAttributes;
import com.rainbow.kam.ble_gatt_manager.exceptions.GattException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ConnectedFailException;
import com.rainbow.kam.ble_gatt_manager.exceptions.details.ReadCharacteristicException;
import com.rainbow.kam.ble_gatt_manager.legacy.GattCustomCallbacks;
import com.rainbow.kam.ble_gatt_manager.legacy.GattManager;
import com.rainbow.kam.ble_gatt_manager.util.BluetoothHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

import javax.inject.Inject;


/**
 * Created by kam6512 on 2015-11-27.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_profile)
public class ProfileActivity extends BaseActivity implements
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


    @Extra(BuildConfig.KEY_DEVICE_NAME) String deviceName;
    @Extra(BuildConfig.KEY_DEVICE_ADDRESS) String deviceAddress;
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
        gattManager.connect(deviceAddress);
        deviceStateTextView.setText(connectingLabel);
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


    @Click(R.id.characteristic_filter) void filtering() {
        List<BluetoothGattCharacteristic> filteredCharacteristicList = Lists.newArrayList();
        PropertyDialog.showPropertyDialog(this, "showPropertyDialog").subscribe(integer -> {
            for (BluetoothGattService service : gattManager.getGatt().getServices()) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (GattAttributes.isPropsAvailable(characteristic.getProperties(), integer)) {
                        filteredCharacteristicList.add(characteristic);
                    }
                }
            }
        }, throwable -> {
        }, () -> profileAdapter.setCharacteristicList(filteredCharacteristicList));
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
        controlView.showNewValue(ch);
    }


    @UiThread @Override
    public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
        controlView.showNewValue(ch);
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
        try {
            throw e;
        } catch (ReadCharacteristicException readCharacteristicException) {
            controlView.showFail();
        } catch (ConnectedFailException connectedFailException) {
            showNoneValue();
        } finally {
            showMessage(e != null ? e.getMessage() : "Blank Message");
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
        controlView.initValue(deviceName, deviceAddress, controlCharacteristic);
    }


    @Background @Override
    public void setNotification(boolean isNotificationEnable) {
        gattManager.setNotification(controlCharacteristic, isNotificationEnable);
    }


    @Background @Override public void setReadValue() {
        gattManager.readValue(controlCharacteristic);
    }


    @Override public void setWriteValue(byte[] data) {
        gattManager.writeValue(controlCharacteristic, data);
    }
}
