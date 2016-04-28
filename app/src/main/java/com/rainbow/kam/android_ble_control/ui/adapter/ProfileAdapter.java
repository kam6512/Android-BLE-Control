package com.rainbow.kam.android_ble_control.ui.adapter;

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
import com.rainbow.kam.ble_gatt_manager.GattAttributes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by kam6512 on 2016-04-27.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SERVICE = 0;
    private static final int TYPE_CHARACTERISTIC = 1;

    private static int CURRENT_TYPE = TYPE_SERVICE;

    private final ArrayList<Object> gattList = Lists.newArrayList();

    private final OnGattItemClickListener onGattItemClickListener;

    private final String uuidLabel;


    @Inject public ProfileAdapter(Context context) {
        uuidLabel = context.getString(R.string.profile_uuid_label);
        this.onGattItemClickListener = (OnGattItemClickListener) context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_SERVICE) {
            ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
            serviceViewHolder.bindViews((BluetoothGattService) gattList.get(position));
        } else if (type == TYPE_CHARACTERISTIC) {
            CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
            characteristicViewHolder.bindViews((BluetoothGattCharacteristic) gattList.get(position));
        }
    }


    @Override public int getItemCount() {
        return gattList.size();
    }


    @Override public int getItemViewType(int position) {
        Object o = gattList.get(position);
        if (o instanceof BluetoothGattService) {
            return TYPE_SERVICE;
        } else if (o instanceof BluetoothGattCharacteristic) {
            return TYPE_CHARACTERISTIC;
        }
        return TYPE_SERVICE;
    }


    public void setServiceList(List<BluetoothGattService> bluetoothGattServices) {
        gattList.clear();
        gattList.addAll(bluetoothGattServices);
        notifyDataSetChanged();
        CURRENT_TYPE = TYPE_SERVICE;
    }


    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        gattList.clear();
        gattList.addAll(bluetoothGattCharacteristics);
        notifyDataSetChanged();
        CURRENT_TYPE = TYPE_CHARACTERISTIC;
    }


    public boolean isBackPressedAvailable() {
        return CURRENT_TYPE == TYPE_SERVICE;
    }


    class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView serviceTitle;
        private TextView serviceUuid;
        private TextView serviceType;


        public ServiceViewHolder(View itemView) {
            super(itemView);
            serviceTitle = (TextView) itemView.findViewById(R.id.profile_parent_list_item_service_name);
            serviceUuid = (TextView) itemView.findViewById(R.id.profile_parent_list_item_service_UUID);
            serviceType = (TextView) itemView.findViewById(R.id.profile_parent_list_item_service_type);
            itemView.setOnClickListener(this);
        }


        private void bindViews(BluetoothGattService bluetoothGattService) {
            String uuid = bluetoothGattService.getUuid().toString();
            String name = GattAttributes.resolveServiceName(uuid);
            uuid = uuidLabel + uuid.substring(4, 8);

            String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? "Primary" : "Secondary";

            serviceTitle.setText(name);
            serviceUuid.setText(uuid);
            serviceType.setText(type);
        }


        @Override
        public void onClick(View v) {
            onGattItemClickListener.onServiceClickListener(getLayoutPosition());
        }
    }

    class CharacteristicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView characteristicTitle;
        private TextView characteristicUuid;


        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            characteristicTitle = (TextView) itemView.findViewById(R.id.profile_child_list_item_characteristics_name);
            characteristicUuid = (TextView) itemView.findViewById(R.id.profile_child_list_item_characteristics_UUID);
            itemView.setOnClickListener(this);
        }


        private void bindViews(BluetoothGattCharacteristic characteristicItem) {
            String uuid = characteristicItem.getUuid().toString();
            String name = GattAttributes.resolveCharacteristicName(uuid);
            uuid = uuidLabel + uuid.substring(4, 8);
            characteristicTitle.setText(name);
            characteristicUuid.setText(uuid);
        }


        @Override
        public void onClick(View v) {
            onGattItemClickListener.onCharacteristicClickListener(getLayoutPosition());
        }
    }

    public interface OnGattItemClickListener {
        void onServiceClickListener(int position);

        void onCharacteristicClickListener(int position);
    }
}

