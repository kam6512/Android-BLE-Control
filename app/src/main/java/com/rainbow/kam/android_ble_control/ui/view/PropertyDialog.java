package com.rainbow.kam.android_ble_control.ui.view;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.ble_gatt_manager.GattAttributes;

import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Created by Kang Young Won on 2016-05-20.
 */
public class PropertyDialog {

    public static Observable<Integer> showPropertyDialog(Context context, CharSequence title) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            private MaterialDialog dialog;
            private Map<Integer, String> properties = GattAttributes.getProperties();


            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
                builder.title(title);
                builder.items(properties.values());
                builder.itemsCallback((dialog, itemView, which, text) -> {
                    subscriber.onNext((Integer) properties.keySet().toArray()[which]);
                    subscriber.onCompleted();
                });
                builder.canceledOnTouchOutside(true);
                builder.dismissListener(dialog1 -> subscriber.unsubscribe());
                dialog = builder.build();
                dialog.show();
                subscriber.add(Subscriptions.create(() -> dialog.dismiss()));
            }
        });
    }
}
