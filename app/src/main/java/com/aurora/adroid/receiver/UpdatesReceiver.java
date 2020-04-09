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

package com.aurora.adroid.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UpdatesReceiver extends BroadcastReceiver {
    static public void setUpdatesInterval(Context context, int interval) {
        final Intent intent = new Intent(context, UpdatesReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            if (interval > 0) {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        Calendar.getInstance().getTimeInMillis(),
                        TimeUnit.DAYS.toMillis(interval),
                        pendingIntent
                );
            }
        }
        Log.i("Periodic update preferences updated");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Checking repo updates");
        Intent repoSyncIntent = new Intent(context, SyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(repoSyncIntent);
        } else {
            context.startService(repoSyncIntent);
        }
    }
}
