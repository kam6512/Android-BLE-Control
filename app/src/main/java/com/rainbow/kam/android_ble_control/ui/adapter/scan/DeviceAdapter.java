package com.rainbow.kam.android_ble_control.ui.adapter.scan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.data.DeviceItem;
import com.rainbow.kam.android_ble_control.ui.adapter.ViewHolder;

import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final OnDeviceSelectListener onDeviceSelectListener;

    private final SortedListAdapterCallback<DeviceItem> deviceListAdapterCallback = new SortedListAdapterCallback<DeviceItem>(this) {
        @Override
        public int compare(DeviceItem deviceItem1, DeviceItem deviceItem2) {
            return deviceItem1.compareTo(deviceItem2);
        }


        @Override
        public boolean areContentsTheSame(DeviceItem oldDeviceItem, DeviceItem newDeviceItem) {
            return oldDeviceItem.equals(newDeviceItem);
        }


        @Override
        public boolean areItemsTheSame(DeviceItem DeviceItem1, DeviceItem DeviceItem2) {
            return Objects.equals(DeviceItem1, DeviceItem2);
        }
    };

    private final SortedList<DeviceItem> deviceList = new SortedList<>(DeviceItem.class, deviceListAdapterCallback);


    @Inject public DeviceAdapter(@NonNull Context context) {
        this.onDeviceSelectListener = (OnDeviceSelectListener) context;
    }


    @Override @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.i_bluetooth_device, parent, false);
        return new DeviceViewHolder(root);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindViews(deviceList.get(position));
    }


    @Override public int getItemCount() {
        return deviceList.size();
    }


    public void addDevice(@NonNull final DeviceItem deviceItem) {
        deviceList.add(deviceItem);
    }


    public void clear() {
        deviceList.clear();
    }


    public class DeviceViewHolder extends ViewHolder {

        private DeviceItem deviceItem;

        @Bind(R.id.item_name) TextView name;
        @Bind(R.id.item_address) TextView address;
        @BindString(R.string.device_name_format) String deviceNameFormat;


        public DeviceViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void bindViews(@NonNull DeviceItem deviceItem) {
            this.deviceItem = deviceItem;
            String deviceName = String.format(Locale.getDefault(), deviceNameFormat, deviceItem.getName(), deviceItem.getType());
            name.setText(deviceName);
            address.setText(deviceItem.getAddress());

        }


        @OnClick(R.id.device_item) void clickDeviceItem() {
            onDeviceSelectListener.onDeviceSelect(deviceItem);
        }
    }
}

