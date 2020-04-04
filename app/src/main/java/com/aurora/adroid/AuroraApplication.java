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

package com.aurora.adroid;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.IntentFilter;
import android.os.AsyncTask;

import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.installer.Installer;
import com.aurora.adroid.installer.InstallerService;
import com.aurora.adroid.installer.Uninstaller;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.plugins.RxJavaPlugins;

public class AuroraApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Installer installer;
    @SuppressLint("StaticFieldLeak")
    public static Uninstaller uninstaller;

    private static RxBus rxBus = null;
    private static boolean bulkUpdateAlive = false;
    private static List<App> ongoingUpdateList = new ArrayList<>();

    public static RxBus getRxBus() {
        return rxBus;
    }

    public static void rxNotify(Event event) {
        rxBus.getBus().accept(event);
    }

    public static Uninstaller getUninstaller() {
        return uninstaller;
    }

    public static Installer getInstaller() {
        return installer;
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

        rxBus = new RxBus();
        installer = new Installer(this);
        uninstaller = new Uninstaller(this);

        //Clear all old installation sessions.
        AsyncTask.execute(() -> Util.clearOldInstallationSessions(this));

        //Check & start notification service
        Util.startNotificationService(this);

        registerReceiver(installer.getPackageInstaller().getBroadcastReceiver(),
                new IntentFilter(InstallerService.ACTION_INSTALLATION_STATUS_NOTIFICATION));

        //Global RX-Error handler, just simply logs, I make sure all errors are handled at origin.
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.e(throwable.getMessage());
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            unregisterReceiver(installer.getPackageInstaller().getBroadcastReceiver());
        } catch (Exception ignored) {
        }
    }
}
