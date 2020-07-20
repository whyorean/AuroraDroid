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

package com.aurora.adroid.installer;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;

import org.apache.commons.lang3.StringUtils;

public class InstallerService extends Service {

    private static final String ACTION_SESSION_INSTALLER = "ACTION_SESSION_INSTALLER";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
        String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);

        //Send broadcast for the installation status of the package
        sendStatusBroadcast(status, packageName);

        //Launch user confirmation activity
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(confirmationIntent);
            } catch (Exception e) {
                sendStatusBroadcast(PackageInstaller.STATUS_FAILURE, packageName);
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    private void sendStatusBroadcast(int status, String packageName) {
        if (StringUtils.isNotEmpty(packageName)) {
            Intent statusIntent = new Intent(ACTION_SESSION_INSTALLER);
            statusIntent.putExtra(PackageInstaller.EXTRA_STATUS, status);
            statusIntent.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName);
            sendBroadcast(statusIntent);
            AuroraApplication.rxNotify(new Event(EventType.SESSION, packageName, status));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
