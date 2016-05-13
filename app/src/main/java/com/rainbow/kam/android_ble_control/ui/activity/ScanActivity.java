package com.rainbow.kam.android_ble_control.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.pwittchen.reactivebeacons.library.ReactiveBeacons;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.dagger.component.ActivityComponent;
import com.rainbow.kam.android_ble_control.data.DeviceItem;
import com.rainbow.kam.android_ble_control.ui.adapter.DeviceAdapter;
import com.rainbow.kam.ble_gatt_manager.legacy.BluetoothHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-04-08.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_scan)
public class ScanActivity extends BaseActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        DeviceAdapter.OnDeviceSelectListener {

    public static final String KEY_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    @ViewById(R.id.scan_root) SwipeRefreshLayout refreshScanLayout;
    @ViewById(R.id.device_list) RecyclerView deviceList;

    @Inject DeviceAdapter deviceAdapter;

    private ReactiveBeacons reactiveBeacons;

    private Subscription beaconSubscription;


    @Override protected void injectComponent(ActivityComponent component) {
        component.inject(this);
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reactiveBeacons = new ReactiveBeacons(this);
    }


    @Override protected void onResume() {
        super.onResume();
        startScan();
    }


    @Override protected void onPause() {
        super.onPause();
        if (beaconSubscription != null && !beaconSubscription.isUnsubscribed()) {
            beaconSubscription.unsubscribe();
        }
        deviceAdapter.clear();
        stopScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        deviceAdapter.clear();
        startScan();
    }


    @Override public void onDeviceSelect(DeviceItem device) {
        Observable.just(this)
                .map(activity -> {
                    Intent commandIntent = new Intent(activity, DeviceProfileActivity_.class);
                    commandIntent.putExtra(KEY_DEVICE_NAME, device.getName());
                    commandIntent.putExtra(KEY_DEVICE_ADDRESS, device.getAddress());
                    return commandIntent;
                })
                .subscribe(this::startActivity)
                .unsubscribe();
    }


    @AfterViews void setViews() {
        refreshScanLayout.setOnRefreshListener(this);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setHasFixedSize(true);
        deviceList.setAdapter(deviceAdapter);
    }


    private void startScan() {
        if (!canObserveBeacons()) {
            return;
        }
        beaconSubscription = reactiveBeacons.observe()
                .take(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> refreshScanLayout.setRefreshing(false))
                .map(beacon1 -> new DeviceItem(beacon1.device, beacon1.rssi))
                .subscribe(deviceAdapter::addDevice);
    }


    private void stopScan() {
        refreshScanLayout.setRefreshing(false);
    }


    private boolean canObserveBeacons() {
        if (!reactiveBeacons.isBleSupported()) {
            Snackbar.make(refreshScanLayout, R.string.bluetooth_fail, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (!reactiveBeacons.isBluetoothEnabled()) {
            reactiveBeacons.requestBluetoothAccess(this);
            return false;
        }
        BluetoothHelper.requestBluetoothPermission(this);
        return true;
    }
}
