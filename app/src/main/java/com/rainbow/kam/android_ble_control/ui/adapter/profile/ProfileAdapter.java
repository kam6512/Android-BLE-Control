package com.rainbow.kam.android_ble_control.ui.adapter.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.ui.adapter.ViewHolder;
import com.rainbow.kam.ble_gatt_manager.GattAttributes;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kam6512 on 2016-04-27.
 */
public class ProfileAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final String uuidLabel;

    private static final int TYPE_SERVICE = 0;
    private static final int TYPE_CHARACTERISTIC = 1;

    private static int CURRENT_TYPE = TYPE_SERVICE;

    private List<BluetoothGattService> bluetoothGattServices = Lists.newArrayList();
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = Lists.newArrayList();

    private final OnGattItemClickListener onGattItemClickListener;


    @Inject public ProfileAdapter(Context context) {
        uuidLabel = context.getString(R.string.profile_uuid_label);
        this.onGattItemClickListener = (OnGattItemClickListener) context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == TYPE_SERVICE) {
            view = layoutInflater.inflate(R.layout.i_profile_bluetooth_service, parent, false);
            return new ServiceViewHolder(view);
        } else if (viewType == TYPE_CHARACTERISTIC) {
            view = layoutInflater.inflate(R.layout.i_profile_bluetooth_characteristics, parent, false);
            return new CharacteristicViewHolder(view);
        }
        return null;
    }


    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_SERVICE) {
            holder.bindViews(bluetoothGattServices.get(position));

        } else if (type == TYPE_CHARACTERISTIC) {
            holder.bindViews(bluetoothGattCharacteristics.get(position));
        }
    }


    @Override public int getItemCount() {
        if (CURRENT_TYPE == TYPE_SERVICE) {
            return bluetoothGattServices.size();
        } else {
            return bluetoothGattCharacteristics.size();
        }
    }


    @Override public int getItemViewType(int position) {
        return CURRENT_TYPE;
    }


    public void setServiceList(List<BluetoothGattService> bluetoothGattServices) {
        this.bluetoothGattServices.clear();
        this.bluetoothGattServices.addAll(bluetoothGattServices);
        showServiceList();
    }


    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        this.bluetoothGattCharacteristics.clear();
        this.bluetoothGattCharacteristics.addAll(bluetoothGattCharacteristics);
        showCharacteristic();
    }


    public void showServiceList() {
        CURRENT_TYPE = TYPE_SERVICE;
        notifyDataSetChanged();
    }


    public void showCharacteristic() {
        CURRENT_TYPE = TYPE_CHARACTERISTIC;
        notifyDataSetChanged();
    }


    public boolean hasBackStack() {
        return CURRENT_TYPE == TYPE_CHARACTERISTIC;
    }


    class ServiceViewHolder extends ViewHolder {

        @Bind(R.id.profile_parent_list_item_service_name) TextView serviceTitle;
        @Bind(R.id.profile_parent_list_item_service_UUID) TextView serviceUuid;
        @Bind(R.id.profile_parent_list_item_service_type) TextView serviceType;


        public ServiceViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void bindViews(final BluetoothGattService bluetoothGattService) {
            String uuid = bluetoothGattService.getUuid().toString();
            String name = GattAttributes.resolveServiceName(uuid);
            String type = GattAttributes.resolveServiceType(bluetoothGattService.getType());

            uuid = uuidLabel + uuid.substring(4, 8);
            serviceTitle.setText(name);
            serviceUuid.setText(uuid);
            serviceType.setText(type);
        }


        @OnClick(R.id.profile_parent_list_item_service) void onServiceSelect() {
            onGattItemClickListener.onServiceClickListener(bluetoothGattServices.get(getLayoutPosition()));
        }
    }

    class CharacteristicViewHolder extends ViewHolder {

        @Bind(R.id.profile_child_list_item_characteristic_name) TextView characteristicTitle;
        @Bind(R.id.profile_child_list_item_characteristic_UUID) TextView characteristicUuid;


        public CharacteristicViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void bindViews(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            String uuid = bluetoothGattCharacteristic.getUuid().toString();
            String name = GattAttributes.resolveCharacteristicName(uuid);
            uuid = uuidLabel + uuid.substring(4, 8);

            characteristicTitle.setText(name);
            characteristicUuid.setText(uuid);

        }


        @OnClick(R.id.profile_child_list_item_characteristic)
        void onCharacteristicSelect() {
            onGattItemClickListener.onCharacteristicClickListener(bluetoothGattCharacteristics.get(getLayoutPosition()));
        }
    }
}

