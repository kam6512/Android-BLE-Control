package com.rainbow.kam.android_ble_control.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.android_ble_control.data.DeviceItem;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Animation.AnimationListener {

    private final OnDeviceSelectListener onDeviceSelectListener;

    private final SortedListAdapterCallback<DeviceItem> sortedListAdapterCallback = new SortedListAdapterCallback<DeviceItem>(this) {
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

    private final SortedList<DeviceItem> sortedList = new SortedList<>(DeviceItem.class, sortedListAdapterCallback);

    private final Animation expandAnimation, collapseAnimation;

    private DeviceViewHolder animateDeviceViewHolder;

    private final String defaultDeviceName;
    private final int iconColor;


    @Inject public DeviceAdapter(@NonNull Context context) {
        defaultDeviceName = context.getString(R.string.device_name_def);
        iconColor = ContextCompat.getColor(context, android.R.color.black);
        this.onDeviceSelectListener = (OnDeviceSelectListener) context;

        expandAnimation = AnimationUtils.loadAnimation(context, R.anim.expand_device_item);
        expandAnimation.setAnimationListener(this);
        expandAnimation.setInterpolator(context, android.R.anim.anticipate_overshoot_interpolator);
        collapseAnimation = AnimationUtils.loadAnimation(context, R.anim.collapse_device_item);
        collapseAnimation.setAnimationListener(this);
        collapseAnimation.setInterpolator(context, android.R.anim.anticipate_overshoot_interpolator);
    }


    @Override @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.i_bluetooth_device, parent, false);
        return new DeviceViewHolder(root);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(sortedList.get(position));
    }


    @Override
    public int getItemCount() {
        return sortedList.size();
    }


    public void addDevice(@NonNull final DeviceItem deviceItem) {
        sortedList.add(deviceItem);
    }


    public void clear() {
        sortedList.clear();
    }


    @Override public void onAnimationStart(@NonNull Animation animation) {
        animateDeviceViewHolder.itemView.requestLayout();
        if (animation == expandAnimation) {
            animateDeviceViewHolder.expandGroup.setVisibility(View.VISIBLE);

            animateDeviceViewHolder.expendImageView.setImageResource(R.drawable.ic_expand_less_white_36dp);
        }
    }


    @Override public void onAnimationEnd(@NonNull Animation animation) {
        animateDeviceViewHolder.itemView.requestLayout();
        if (animation == collapseAnimation) {
            animateDeviceViewHolder.expandGroup.setVisibility(View.GONE);
            animateDeviceViewHolder.expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);
        }
    }


    @Override public void onAnimationRepeat(Animation animation) {
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener { //뷰 초기화

        private DeviceItem deviceItem;

        private final View itemView;

        private final TextView extraName;
        private final TextView extraAddress;
        private final TextView extraBondState;
        private final TextView extraType;
        private final TextView extraRssi;

        private final LinearLayout expandGroup;
        private final ImageView expendImageView;


        public DeviceViewHolder(@NonNull final View itemView) {
            super(itemView);

            this.itemView = itemView;
            this.itemView.setOnClickListener(this);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            expandGroup = (LinearLayout) itemView.findViewById(R.id.row_expand);
            expendImageView = (ImageView) itemView.findViewById(R.id.button_expand);
            expendImageView.setOnClickListener(this);
            expendImageView.setColorFilter(iconColor);
        }


        private void bindViews(@NonNull DeviceItem deviceItem) {
            this.deviceItem = deviceItem;
            String deviceName = this.deviceItem.getExtraName();
            if (Strings.isNullOrEmpty(deviceName)) {
                deviceName = defaultDeviceName;
            }
            extraName.setText(deviceName);
            extraAddress.setText(this.deviceItem.getExtraAddress());
            extraBondState.setText(String.valueOf(this.deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(this.deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(this.deviceItem.getExtraRssi()));

            expandGroup.setVisibility(View.GONE);

            expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);
        }


        public void clickDeviceItem() {
            onDeviceSelectListener.onDeviceSelect(deviceItem);
        }


        public void clickExpandIcon() {
            animateDeviceViewHolder = this;
            if (expandGroup.isShown()) {
                collapsedView();
            } else {
                expandView();
            }
        }


        private void expandView() {
            expandAnimation.reset();
            expandGroup.clearAnimation();
            expandGroup.startAnimation(expandAnimation);
        }


        private void collapsedView() {
            collapseAnimation.reset();
            expandGroup.clearAnimation();
            expandGroup.startAnimation(collapseAnimation);
        }


        @Override public void onClick(View v) {
            if (v == itemView) {
                clickDeviceItem();
            } else if (v == expendImageView) {
                clickExpandIcon();
            }
        }
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(DeviceItem device);
    }
}