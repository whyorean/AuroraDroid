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

package com.aurora.adroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.model.App;
import com.aurora.adroid.receiver.PackageManagerReceiver;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

public class AuroraApplication extends Application {

    private static RxBus rxBus = null;
    private static List<App> ongoingUpdateList = new ArrayList<>();
    private static boolean isRooted = false;
    private static boolean bulkUpdateAlive = false;

    private PackageManagerReceiver packageManagerReceiver;

    public static RxBus getRxBus() {
        return rxBus;
    }

    public static void rxNotify(Event event) {
        rxBus.getBus().accept(event);
    }

    public static boolean isBulkUpdateAlive() {
        return bulkUpdateAlive;
    }

    public static void setBulkUpdateAlive(boolean updating) {
        AuroraApplication.bulkUpdateAlive = updating;
    }

    public static List<App> getOngoingUpdateList() {
        return ongoingUpdateList;
    }

    public static void setOngoingUpdateList(List<App> ongoingUpdateList) {
        AuroraApplication.ongoingUpdateList = ongoingUpdateList;
    }

    public static void removeFromOngoingUpdateList(String packageName) {
        Iterator<App> iterator = ongoingUpdateList.iterator();
        while (iterator.hasNext()) {
            if (packageName.equals(iterator.next().getPackageName()))
                iterator.remove();
        }
        if (ongoingUpdateList.isEmpty())
            setBulkUpdateAlive(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupTheme();

        rxBus = new RxBus();

        if (Util.isRootInstallEnabled(getApplicationContext())) {
            Shell.getShell(shell -> {
                if (shell.isRoot()) {
                    Log.i("Root Available");
                    isRooted = true;
                } else {
                    Log.e("Root Unavailable");
                    isRooted = false;
                }
            });
        }

        packageManagerReceiver = new PackageManagerReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
            }
        };

        registerReceiver(packageManagerReceiver, PackageUtil.getFilter());

        //Clear all old installation sessions.
        AsyncTask.execute(() -> Util.clearOldInstallationSessions(this));

        //Check & start notification service
        Util.startNotificationService(this);

        //Global RX-Error handler, just simply logs, I make sure all errors are handled at origin.
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.e(throwable.getMessage());
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace();
            }
        });
    }

    private void setupTheme() {
        ViewUtil.switchTheme(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            unregisterReceiver(packageManagerReceiver);
            AppDatabase.destroyInstance();
        } catch (Exception ignored) {
        }
    }
}
