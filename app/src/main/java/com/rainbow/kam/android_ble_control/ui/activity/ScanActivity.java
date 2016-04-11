package com.rainbow.kam.android_ble_control.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.pwittchen.reactivebeacons.library.ReactiveBeacons;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.data.adapter.DeviceAdapter;
import com.rainbow.kam.android_ble_control.data.adapter.DeviceAdapter.OnDeviceSelectListener;
import com.rainbow.kam.android_ble_control.data.adapter.DeviceItem;
import com.rainbow.kam.ble_gatt_manager.BluetoothHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-04-08.
 */
@EActivity(R.layout.a_scan)
public class ScanActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        OnDeviceSelectListener {


    public static final String KEY_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    @StringRes(R.string.device_name_def) String defaultDeviceName;

    @ViewById(R.id.scan_root) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.device_list) RecyclerView deviceList;

    private DeviceAdapter deviceAdapter;

    private ReactiveBeacons reactiveBeacons;

    private Subscription beaconSubscription;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reactiveBeacons = new ReactiveBeacons(this);
    }


    @Override protected void onResume() {
        super.onResume();
        if (canObserveBeacons()) {
            startScan();
        }
    }


    @Override protected void onPause() {
        super.onPause();
        stopScan();
    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @Override public void onRefresh() {
        if (!beaconSubscription.isUnsubscribed()) {
            beaconSubscription.unsubscribe();
        }
        startScan();
        deviceAdapter.clear();
        refreshLayout.setRefreshing(false);
    }


    @Override public void onDeviceSelect(DeviceItem device) {
        startCommandActivity(device);
    }


    @Override public void onDeviceUnSelected() {
        finish();
    }


    @AfterViews void setViews() {
        refreshLayout.setOnRefreshListener(this);

        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setHasFixedSize(true);
        deviceAdapter = new DeviceAdapter(this);
        deviceList.setAdapter(deviceAdapter);
    }


    private void startScan() {
        beaconSubscription = reactiveBeacons.observe()
                .take(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(beacon1 -> {
                    String name = beacon1.device.getName();
                    if (name == null) {
                        name = defaultDeviceName;
                    }
                    return new DeviceItem(beacon1.device, beacon1.rssi);
                })
                .subscribe(device -> {
                    deviceAdapter.addDevice(device);
                });
    }


    private void stopScan() {
        if (beaconSubscription != null && !beaconSubscription.isUnsubscribed()) {
            beaconSubscription.unsubscribe();
        }
        deviceAdapter.clear();
    }


    private boolean canObserveBeacons() {
        if (!reactiveBeacons.isBleSupported()) {
            Snackbar.make(refreshLayout, R.string.bluetooth_fail, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (!reactiveBeacons.isBluetoothEnabled()) {
            reactiveBeacons.requestBluetoothAccess(this);
            return false;
        }
        BluetoothHelper.requestBluetoothPermission(this);
        return true;
    }


    private void startCommandActivity(DeviceItem device) {
        Observable.just(this)
                .map(activity -> {
                    Intent commandIntent = new Intent(activity, DeviceProfileActivity_.class);
                    commandIntent.putExtra(KEY_DEVICE_NAME, device.getExtraName());
                    commandIntent.putExtra(KEY_DEVICE_ADDRESS, device.getExtraAddress());
                    return commandIntent;
                })
                .subscribe(this::startActivity).unsubscribe();
    }

}
