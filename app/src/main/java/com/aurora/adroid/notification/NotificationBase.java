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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.receiver.DownloadCancelReceiver;
import com.aurora.adroid.receiver.DownloadPauseReceiver;
import com.aurora.adroid.receiver.DownloadResumeReceiver;
import com.aurora.adroid.receiver.InstallReceiver;
import com.aurora.adroid.ui.activity.DetailsActivity;


public class NotificationBase {

    public static final String INTENT_APK_FILE_NAME = "INTENT_APK_FILE_NAME";
    public static final String REQUEST_ID = "REQUEST_ID";

    protected NotificationCompat.Builder builder;
    protected NotificationChannel channel;
    protected NotificationManager manager;

    protected Context context;
    protected App app;

    public NotificationBase(Context context) {
        this.context = context;
    }

    public NotificationBase(Context context, App app) {
        this.context = context;
        this.app = app;
    }

    protected NotificationCompat.Builder getBuilder() {
        return new NotificationCompat.Builder(context, app.getPackageName())
                .setAutoCancel(true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentIntent(getContentIntent())
                .setContentTitle(app.getName())
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_notifications)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    /*
     *
     * All Pending Intents to handle App Download & App Installations
     * getContentIntent() to launch DetailsActivity for the App
     * getInstallIntent() to broadcast Install action on download complete
     * getCancelIntent() to broadcast Download Cancel action
     * getPauseIntent() to broadcast Download Pause action
     *
     */

    protected PendingIntent getContentIntent() {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(INTENT_APK_FILE_NAME, app.getPackageName());
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected PendingIntent getInstallIntent() {
        Intent intent = new Intent(context, InstallReceiver.class);
        intent.putExtra(INTENT_APK_FILE_NAME, app.getAppPackage().getApkName());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected PendingIntent getCancelIntent(int requestId) {
        Intent intent = new Intent(context, DownloadCancelReceiver.class);
        intent.putExtra(REQUEST_ID, requestId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected PendingIntent getPauseIntent(int requestId) {
        Intent intent = new Intent(context, DownloadPauseReceiver.class);
        intent.putExtra(REQUEST_ID, requestId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected PendingIntent getResumeIntent(int requestId) {
        Intent intent = new Intent(context, DownloadResumeReceiver.class);
        intent.putExtra(REQUEST_ID, requestId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
