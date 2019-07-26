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

import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.installer.Installer;
import com.aurora.adroid.installer.InstallerService;
import com.aurora.adroid.installer.Uninstaller;
import com.aurora.adroid.util.Log;

import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;

public class AuroraApplication extends Application {

    public static RxBus rxBus;
    @SuppressLint("StaticFieldLeak")
    public static Installer installer;
    @SuppressLint("StaticFieldLeak")
    public static Uninstaller uninstaller;

    public static Uninstaller getUninstaller() {
        return uninstaller;
    }

    public static Installer getInstaller() {
        return installer;
    }

    public static Observable<Object> getRxBus() {
        return rxBus.toObservable();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(err -> Log.e(err.getMessage()));
        rxBus = RxBus.get();
        installer = new Installer(this);
        uninstaller = new Uninstaller(this);
        registerReceiver(installer.getPackageInstaller().getBroadcastReceiver(),
                new IntentFilter(InstallerService.ACTION_INSTALLATION_STATUS_NOTIFICATION));
        RxJavaPlugins.setErrorHandler(err -> {
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
