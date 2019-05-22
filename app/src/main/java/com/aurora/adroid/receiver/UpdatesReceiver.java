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

package com.aurora.adroid.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurora.adroid.util.Log;

public class UpdatesReceiver extends BroadcastReceiver {
    static public void enable(Context context, int interval) {
        Intent intent = new Intent(context, UpdatesReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        if (interval > 0) {
            Log.e("Enabling periodic update checks");
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    interval,
                    pendingIntent
            );
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /*Log.e("Update check Started");
        CompositeDisposable disposable = new CompositeDisposable();
        UpdatableApps updatableAppTask = new UpdatableApps(context);
        disposable.add(Observable.fromCallable(updatableAppTask::getUpdatableApps)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (!appList.isEmpty()) {
                        QuickNotification.show(context,
                                context.getString(R.string.action_updates),
                                new StringBuilder()
                                        .append(appList.size())
                                        .append(StringUtils.SPACE)
                                        .append(context.getString(R.string.list_update_all_txt))
                                        .toString(),
                                getContentIntent(context));
                    }
                }, err -> Log.e("Update check failed")));*/
    }

    /*private PendingIntent getContentIntent(Context context) {
        Intent intent = new Intent(context, AuroraActivity.class);
        intent.putExtra(Constants.INTENT_FRAGMENT_POSITION, 2);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }*/
}
