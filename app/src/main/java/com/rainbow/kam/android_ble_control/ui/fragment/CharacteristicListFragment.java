package com.rainbow.kam.android_ble_control.ui.fragment;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.data.adapter.CharacteristicAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
@EFragment(R.layout.f_profile_characteristic)
public class CharacteristicListFragment extends Fragment {

    private Context context;

    private View view;

    private CharacteristicAdapter characteristicAdapter;

    private OnCharacteristicReadyListener onCharacteristicReadyListener;

    @ViewById(R.id.profile_characteristic_recyclerView) RecyclerView recyclerView;


    @DebugLog
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onCharacteristicReadyListener = (OnCharacteristicReadyListener) context;
    }


    @AfterViews void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        characteristicAdapter = new CharacteristicAdapter(context);
        recyclerView.setAdapter(characteristicAdapter);
        onCharacteristicReadyListener.onCharacteristicReady();
    }


    @DebugLog
    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        characteristicAdapter.setCharacteristicList(bluetoothGattCharacteristics);
    }


    public interface OnCharacteristicReadyListener {
        void onCharacteristicReady();
    }
}
