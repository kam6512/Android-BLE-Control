package com.rainbow.kam.android_ble_control.ui.view;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.rainbow.kam.android_ble_control.R;
import com.rainbow.kam.ble_gatt_manager.GattAttributes;

import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.bluetooth.BluetoothGattCharacteristic.*;

/**
 * Created by kam6512 on 2015-11-02.
 */
@EViewGroup(R.layout.v_profile_control)
public class ControlView extends NestedScrollView {


    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private String name;
    private String address;
    private String uuid;
    private int properties;

    private String hexValue;
    private String strValue;
    private String lastUpdateTime;
    private boolean notificationEnabled;

    @ViewById(R.id.characteristic_device_name) TextView deviceName;
    @ViewById(R.id.characteristic_device_address) TextView deviceAddress;

    @ViewById(R.id.characteristic_service_name) TextView serviceName;
    @ViewById(R.id.characteristic_service_uuid) TextView serviceUuid;

    @ViewById(R.id.control_name) TextView charName;
    @ViewById(R.id.control_uuid) TextView charUuid;

    @ViewById(R.id.control_type) TextView charDataType;
    @ViewById(R.id.control_properties) TextView charProperties;

    @ViewById(R.id.control_hex_value) EditText charHexValue;
    @ViewById(R.id.control_ascii_value) TextView charStrValue;
    @ViewById(R.id.control_timestamp) TextView charDateValue;


    @ViewById(R.id.control_notification_switcher) ToggleButton notificationBtn;
    @ViewById(R.id.control_read_btn) Button readBtn;
    @ViewById(R.id.control_write_btn) Button writeBtn;


    @StringRes(R.string.control_fail) String fail;
    @StringRes(R.string.control_none) String none;
    @StringRes(R.string.control_empty) String empty;

    @StringRes(R.string.control_format_clean) String cleaningFormat;
    @StringRes(R.string.control_format_UUID) String uuidFormat;
    @StringRes(R.string.control_format_hex) String hexFormat;
    @StringRes(R.string.control_format_string) String stringFormat;

    @StringRes(R.string.profile_timestamp) String timeStamp;
    @StringRes(R.string.control_format_properties) String propertiesFormat;

    @StringRes(R.string.control_properties_read) String propertiesRead;
    @StringRes(R.string.control_properties_write) String propertiesWrite;
    @StringRes(R.string.control_properties_notify) String propertiesNotify;
    @StringRes(R.string.control_properties_indicate) String propertiesIndicate;
    @StringRes(R.string.control_properties_wnr) String propertiesWriteNoResponse;


    private final OnControlListener onControlListener;


    public ControlView(Context context) {
        super(context);
        onControlListener = (OnControlListener) context;
    }


    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onControlListener = (OnControlListener) context;
    }


    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onControlListener = (OnControlListener) context;
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            onControlListener.onControlReady();
        } else {
            notificationEnabled = false;
        }
    }


    public void initValue(String name, String address, BluetoothGattCharacteristic characteristic) {
        this.name = name;
        this.address = address;
        this.bluetoothGattCharacteristic = characteristic;

        if (!isControlCharacteristicDuplicated()) {
            hexValue = none;
            strValue = none;
            lastUpdateTime = none;
            notificationEnabled = false;
            uuid = bluetoothGattCharacteristic.getUuid().toString();
            properties = bluetoothGattCharacteristic.getProperties();
        }
        if (getVisibility() == VISIBLE) {
            initCharacteristicInfoView();
            initPropsView();
        }
        bindView();
    }


    private boolean isControlCharacteristicDuplicated() {
        return Objects.equals(uuid, bluetoothGattCharacteristic.getUuid().toString());
    }


    @UiThread void initCharacteristicInfoView() {
        deviceName.setText(name);
        deviceAddress.setText(address);

        String service = bluetoothGattCharacteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault());
        serviceUuid.setText(service);
        serviceName.setText(GattAttributes.resolveServiceName(service));

        String characteristic = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
        charUuid.setText(characteristic);
        charName.setText(GattAttributes.resolveCharacteristicName(characteristic));
        charDataType.setText(GattAttributes.resolveValueTypeDescription(bluetoothGattCharacteristic));
    }


    @UiThread void initPropsView() {
        StringBuilder propertiesString = new StringBuilder();

        propertiesString.append(String.format(propertiesFormat, propertiesString));

        if (propertiesEnabled(PROPERTY_READ)) {
            propertiesString.append(propertiesRead);
        }
        if (propertiesEnabled(PROPERTY_WRITE)) {
            propertiesString.append(propertiesWrite);
        }
        if (propertiesEnabled(PROPERTY_NOTIFY)) {
            propertiesString.append(propertiesNotify);
        }
        if (propertiesEnabled(PROPERTY_INDICATE)) {
            propertiesString.append(propertiesIndicate);
        }
        if (propertiesEnabled(PROPERTY_WRITE_NO_RESPONSE)) {
            propertiesString.append(propertiesWriteNoResponse);
        }
        charProperties.setText(propertiesString.toString());

        notificationBtn.setEnabled(propertiesEnabled(PROPERTY_NOTIFY));
        notificationBtn.setChecked(notificationEnabled);
        readBtn.setEnabled(propertiesEnabled(PROPERTY_READ));
        writeBtn.setEnabled(propertiesEnabled(PROPERTY_WRITE | PROPERTY_WRITE_NO_RESPONSE));
        charHexValue.setEnabled(writeBtn.isEnabled());
    }


    private boolean propertiesEnabled(int props) {
        return (properties & props) != 0;
    }


    @UiThread
    public void showNewValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        byte[] rawValue = bluetoothGattCharacteristic.getValue();

        setTimeStamp();

        if (rawValue != null && rawValue.length > 0) {
            hexValue = uuidFormat + getValue(rawValue, hexFormat);
            strValue = getValue(rawValue, stringFormat);
        } else {
            hexValue = none;
            strValue = none;
        }

        bindView();
    }


    private void setTimeStamp() {
        lastUpdateTime = new SimpleDateFormat(timeStamp, Locale.getDefault()).format(new Date().getTime());
    }


    private String getValue(byte[] rawValue, String format) {
        final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
        for (byte byteChar : rawValue) {
            stringBuilder.append(String.format(format, byteChar));
        }
        return stringBuilder.toString();
    }


    public void showFail() {
        hexValue = fail;
        strValue = fail;
        lastUpdateTime = fail;
        bindView();
    }


    @UiThread void bindView() {
        if (getVisibility() == VISIBLE) {
            charHexValue.setText(hexValue);
            charStrValue.setText(strValue);
            charDateValue.setText(lastUpdateTime);
        }
    }


    @Click(R.id.control_read_btn) void clickReadBtn() {
        onControlListener.setReadValue();
    }


    @Click(R.id.control_write_btn) void clickWriteBtn() {
        String newValue = charHexValue.getText().toString().toLowerCase(Locale.getDefault());
        if (!TextUtils.isEmpty(newValue) || newValue.length() > 1) {
            try {
                byte[] dataToWrite = getBytes(newValue);
                onControlListener.setWriteValue(dataToWrite);
            } catch (StringIndexOutOfBoundsException e) {
                onControlListener.setWriteValue(new byte[0]);
            }

        } else {
            Snackbar.make(this, R.string.control_empty, Snackbar.LENGTH_SHORT).show();
        }
    }


    @CheckedChange(R.id.control_notification_switcher)
    void clickNotificationBtn(CompoundButton buttonView, boolean isChecked) {
        if (isChecked == notificationEnabled) {
            return;
        }
        onControlListener.setNotification(isChecked);
        notificationEnabled = isChecked;
    }


    private byte[] getBytes(String hex) {
        hex = hex.toLowerCase(Locale.getDefault()).replaceAll(cleaningFormat, "");
        byte[] bytes = new byte[(hex.length() / 2) + 1];

        int length = bytes.length;
        int checksum = 0;

        for (int i = 0; i < length - 1; ++i) {
            bytes[i] = Long.decode(uuidFormat + hex.substring(i * 2, i * 2 + 2)).byteValue();

            if (i > 1 && i <= length - 2) {
                if (bytes[i] < 0x00) {
                    checksum ^= bytes[i] + 256;
                } else {
                    checksum ^= bytes[i];
                }
            }
        }
        bytes[length - 1] = Long.decode(uuidFormat + String.format(hexFormat, checksum)).byteValue();

        return bytes;
    }
}