package com.rainbow.kam.android_ble_control.ui.adapter;

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

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<BluetoothGattService> serviceItemArrayList = Lists.newArrayList();

    private final OnServiceItemClickListener onServiceItemClickListener;

    private final String uuidLabel;


    public ServiceAdapter(Context context) {
        uuidLabel = context.getString(R.string.profile_uuid_label);
        this.onServiceItemClickListener = (OnServiceItemClickListener) context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_profile_bluetooth_service, parent, false);
        return new ServiceViewHolder(view);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
        serviceViewHolder.bindViews(serviceItemArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return serviceItemArrayList.size();
    }


    @DebugLog
    public void setServiceList(List<BluetoothGattService> bluetoothGattServices) {
        if (getItemCount() == 0) {
            // 서비스는 한 기기에서 오직 1개의 리스트만 있고 변경되지 않으므로 한번 가져오고 난 뒤에는 가져올 일이없다
            serviceItemArrayList.addAll(bluetoothGattServices);
            notifyDataSetChanged();
        }
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

            String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? "primary" : "secondary";

            serviceTitle.setText(name);
            serviceUuid.setText(uuid);
            serviceType.setText(type);
        }


        @Override
        public void onClick(View v) {
            onServiceItemClickListener.onServiceItemClick(getLayoutPosition());
        }

    }


    public interface OnServiceItemClickListener {
        void onServiceItemClick(int position);
    }
}
