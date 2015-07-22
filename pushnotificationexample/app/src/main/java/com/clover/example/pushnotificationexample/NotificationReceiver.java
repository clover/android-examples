package com.clover.example.pushnotificationexample;

import android.content.Context;
import android.content.Intent;

import com.clover.sdk.v1.app.AppNotification;
import com.clover.sdk.v1.app.AppNotificationReceiver;

/**
 * Created by jiongshen on 12/9/14.
 */
public class NotificationReceiver extends AppNotificationReceiver {
    public final static String TEST_NOTIFICATION_ACTION = "test_notification";

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, AppNotification notification) {
        if (notification.appEvent.equals(TEST_NOTIFICATION_ACTION)) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_PAYLOAD, notification.payload);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
