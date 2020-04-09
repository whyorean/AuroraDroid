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
import android.text.TextUtils;

import com.aurora.adroid.model.App;
import com.aurora.adroid.util.PackageUtil;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppsTask extends AllAppsTask {


    public InstalledAppsTask(Context context) {
        super(context);
    }

    public List<App> getInstalledApps() {
        List<App> appList = new ArrayList<>();
        for (App app : getAllLocalApps()) {
            if ((getPackageManager().getLaunchIntentForPackage(app.getPackageName())) != null)
                appList.add(app);
        }
        return appList;
    }

    public List<App> getAllLocalApps() {
        List<App> appList = new ArrayList<>();
        List<String> packageList = getLocalInstalledApps();
        for (String packageName : packageList) {

            if (TextUtils.isEmpty(packageName)) {
                continue;
            }

            final App app = PackageUtil.getAppFromPackageName(getPackageManager(), packageName, true);
            appList.add(app);
        }
        return appList;
    }
}