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

package com.aurora.adroid.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.model.App;
import com.aurora.adroid.task.LiveUpdate;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class BulkUpdateService extends Service {

    public static BulkUpdateService instance = null;

    private List<App> appList = new ArrayList<>();

    public static boolean isServiceRunning() {
        try {
            return instance != null && instance.isRunning();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean isRunning() {
        return true;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appList = AuroraApplication.getOngoingUpdateList();
        updateAllApps();
    }

    private void updateAllApps() {
        AuroraApplication.setBulkUpdateAlive(true);
        AuroraApplication.rxNotify(new Event(EventType.BULK_UPDATE_NOTIFY));
        Observable.fromIterable(appList)
                .subscribeOn(Schedulers.io())
                .doOnNext(app -> new LiveUpdate(this, app).enqueueUpdate())
                .doOnComplete(() -> {
                })
                .subscribe();
    }

    @Override
    public void onDestroy() {
        AuroraApplication.setBulkUpdateAlive(false);
        AuroraApplication.rxNotify(new Event(EventType.BULK_UPDATE_NOTIFY));
        instance = null;
        super.onDestroy();
    }
}
