/*
 * Aurora Droid
 * Copyright (C) 2019-20, Rahul Kumar Patel <whyorean@gmail.com>
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
 *
 */

package com.aurora.adroid.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.aurora.adroid.R;
import com.aurora.adroid.util.Util;

public class SyncNotification {

    private Context context;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;

    public SyncNotification(Context context) {
        this.context = context;
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.builder = getBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("REPO_NOTIFICATION_CHANNEL",
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Aurora Droid Notification Channel");
            manager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
    }

    public NotificationCompat.Builder getBuilder() {
        return new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentTitle(context.getString(R.string.sync_progress))
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_notification_outlined)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    public void notifyQueued() {
        builder.setOngoing(true);
        builder.setProgress(0, 0, true);
        show();
    }

    public void notifySyncProgress(int progress, int total) {
        builder.setOngoing(true);
        builder.setContentText(new StringBuilder().append(progress).append("/").append(total));
        builder.setProgress(total, progress, false);
        show();
    }

    public void notifyCompleted() {
        builder.setOngoing(false);
        builder.setContentText(context.getString(R.string.sync_completed_all));
        builder.setProgress(0, 0, false);
        builder.setAutoCancel(true);
        show();
    }

    public void show() {
        if (Util.isNotificationEnabled(context)) {
            manager.notify(11, builder.build());
        }
    }
}
