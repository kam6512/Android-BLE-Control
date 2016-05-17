package com.rainbow.kam.android_ble_control.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.pwittchen.reactivebeacons.library.ReactiveBeacons;
import com.rainbow.kam.android_ble_control.BuildConfig;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.dagger.component.ActivityComponent;
import com.rainbow.kam.android_ble_control.data.DeviceItem;
import com.rainbow.kam.android_ble_control.ui.adapter.scan.DeviceAdapter;
import com.rainbow.kam.android_ble_control.ui.adapter.scan.OnDeviceSelectListener;
import com.rainbow.kam.ble_gatt_manager.BluetoothHelper;

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
        OnDeviceSelectListener {


    @ViewById(R.id.scan_root) SwipeRefreshLayout refreshScanLayout;
    @ViewById(R.id.device_list) RecyclerView deviceList;
    private Snackbar scanEmptySnackBar;

    @Inject DeviceAdapter deviceAdapter;

    private ReactiveBeacons reactiveBeacons;

    private Subscription beaconSubscription;


    @Override protected void injectComponent(ActivityComponent component) {
        component.inject(this);
        reactiveBeacons = new ReactiveBeacons(this);
    }


    @Override protected void onResume() {
        super.onResume();
        startScan();
    }


    @Override protected void onPause() {
        super.onPause();
        stopScan();
        refreshScanLayout.setRefreshing(false);
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
        stopScan();
        startScan();
    }


    @Override public void onDeviceSelect(DeviceItem device) {
        Observable.just(this)
                .map(activity -> {
                    Intent commandIntent = new Intent(activity, DeviceProfileActivity_.class);
                    commandIntent.putExtra(BuildConfig.KEY_DEVICE_NAME, device.getName());
                    commandIntent.putExtra(BuildConfig.KEY_DEVICE_ADDRESS, device.getAddress());
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


    @AfterViews void setEmptySnackBar() {
        scanEmptySnackBar = Snackbar.make(refreshScanLayout, R.string.scan_none_snack_title, Snackbar.LENGTH_INDEFINITE).setAction(R.string.scan_none_snack_label, v -> {
            startScan();
        }).setCallback(new Snackbar.Callback() {
            @Override public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                startScan();
            }


            @Override public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                if (!refreshScanLayout.isRefreshing()) {
                    refreshScanLayout.setRefreshing(false);
                }
            }
        });
    }


    private void startScan() {
        if (canObserveBeacons()) {
            beaconSubscription = reactiveBeacons.observe()
                    .take(5, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(beacon -> {
                        if (!refreshScanLayout.isRefreshing()) {
                            refreshScanLayout.setRefreshing(true);
                        }
                    })
                    .doOnUnsubscribe(() -> {
                        refreshScanLayout.setRefreshing(false);
                        if (deviceAdapter.getItemCount() == 0) {
                            scanEmptySnackBar.show();
                        }
                    })
                    .map(beacon1 -> new DeviceItem(beacon1.device, beacon1.rssi))
                    .subscribe(deviceAdapter::addDevice);
        }
    }


    private void stopScan() {
        if (beaconSubscription != null && !beaconSubscription.isUnsubscribed()) {
            beaconSubscription.unsubscribe();
        }
        deviceAdapter.clear();
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
