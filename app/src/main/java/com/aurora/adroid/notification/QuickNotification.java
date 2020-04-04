/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurora.adroid.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;

public class QuickNotification {

    private static final int QUICK_NOTIFICATION_CHANNEL_ID = 69;
    private Context context;

    public QuickNotification(Context context) {
        this.context = context;
    }

    public static QuickNotification show(@NonNull Context context,
                                         @NonNull String contentTitle,
                                         @NonNull String contentText,
                                         @Nullable PendingIntent contentIntent) {
        QuickNotification quickNotification = new QuickNotification(context);
        quickNotification.show(contentTitle, contentText, contentIntent);
        return quickNotification;
    }

    public void show(String contentTitle, String contentText, PendingIntent contentIntent) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALERT)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification_outlined);

        if (contentIntent != null)
            builder.setContentIntent(contentIntent);

        if (notificationManager != null)
            notificationManager.notify(QUICK_NOTIFICATION_CHANNEL_ID, builder.build());
    }
}
