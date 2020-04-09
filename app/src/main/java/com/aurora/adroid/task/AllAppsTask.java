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

package com.aurora.adroid.task;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class AllAppsTask {

    private Context context;
    private PackageManager packageManager;

    public AllAppsTask(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public Context getContext() {
        return context;
    }

    List<String> getLocalInstalledApps() {
        final List<String> packageList = new ArrayList<>();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(PackageManager.GET_META_DATA)) {
            final String packageName = packageInfo.packageName;

            if (packageInfo.applicationInfo != null && !packageInfo.applicationInfo.enabled)
                continue;

            packageList.add(packageName);
        }
        return packageList;
    }
}
