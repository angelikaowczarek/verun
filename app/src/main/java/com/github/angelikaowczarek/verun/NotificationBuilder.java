package com.github.angelikaowczarek.verun;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.github.angelikaowczarek.verun.activity.MainActivity;

/**
 * Created by angelika on 25.03.17.
 */

public class NotificationBuilder {

    private NotificationCompat.Builder builder;
    private TaskStackBuilder stackBuilder;
    private Intent intent;

    public NotificationBuilder(Context service, Intent intent) {
        builder =
                new NotificationCompat.Builder(service)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Verun")
                        .setContentText("There is a Verun station near you!");
        stackBuilder = TaskStackBuilder.create(service);
        this.intent = intent;
        setupNotification();
    }

    private void setupNotification() {

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
    }

    public Notification getNotification() {
        return builder.build();
    }
}
