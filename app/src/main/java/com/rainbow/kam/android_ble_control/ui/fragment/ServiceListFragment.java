package com.rainbow.kam.android_ble_control.ui.fragment;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.ui.adapter.ServiceAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
@EFragment(R.layout.f_profile_service)
public class ServiceListFragment extends Fragment {

    private Context context;

    private ServiceAdapter serviceAdapter;

    private OnServiceReadyListener onServiceReadyListener;

    @ViewById(R.id.profile_service_recyclerView) RecyclerView recyclerView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onServiceReadyListener = (OnServiceReadyListener) context;
    }


    @AfterViews void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        serviceAdapter = new ServiceAdapter(context);
        recyclerView.setAdapter(serviceAdapter);
        onServiceReadyListener.onServiceReady();
    }


    @DebugLog
    public void setServiceList(List<BluetoothGattService> bluetoothGattServices) {
        serviceAdapter.setServiceList(bluetoothGattServices);
    }


    public interface OnServiceReadyListener {
        void onServiceReady();
    }
}
