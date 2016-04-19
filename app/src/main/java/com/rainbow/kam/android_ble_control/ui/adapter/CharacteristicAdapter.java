package com.rainbow.kam.android_ble_control.ui.adapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.listener.click.OnCharacteristicItemClickListener;
import com.rainbow.kam.ble_gatt_manager.GattAttributes;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<BluetoothGattCharacteristic> characteristicArrayList = Lists.newArrayList();

    private final OnCharacteristicItemClickListener onCharacteristicItemClickListener;

    private final String uuidLabel;


    public CharacteristicAdapter(Context context) {
        uuidLabel = context.getString(R.string.profile_uuid_label);
        this.onCharacteristicItemClickListener = (OnCharacteristicItemClickListener) context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_profile_bluetooth_characteristics, parent, false);
        return new CharacteristicViewHolder(view);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
        characteristicViewHolder.bindViews(characteristicArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return characteristicArrayList.size();
    }


    @DebugLog
    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        if (!characteristicArrayList.equals(bluetoothGattCharacteristics)) {
            characteristicArrayList.clear();
            characteristicArrayList.addAll(bluetoothGattCharacteristics);
            notifyDataSetChanged();
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
            onCharacteristicItemClickListener.onItemClick(getLayoutPosition());
        }
    }
}
