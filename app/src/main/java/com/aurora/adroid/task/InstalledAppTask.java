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

package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.CertUtil;
import com.aurora.adroid.util.PackageUtil;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppTask extends ContextWrapper {

    private Context context;
    private FetchAppsTask fetchAppsTask;
    private PackageManager packageManager;

    public InstalledAppTask(Context context) {
        super(context);
        this.context = context;
        this.fetchAppsTask = new FetchAppsTask(context);
        this.packageManager = context.getPackageManager();
    }

    public List<App> getUpdatableApps() {
        List<App> updatableList = new ArrayList<>();
        for (App app : getInstalledApps(true)) {
            final PackageInfo packageInfo = PackageUtil.getPackageInfo(packageManager, app.getPackageName());
            final App tempApp = fetchAppsTask.getAppByPackageName(app.getPackageName());
            if (packageInfo != null && tempApp != null) {
                final String RSA256 = CertUtil.getSHA256(context, app.getPackageName());
                if (packageInfo.versionName.compareTo(tempApp.getAppPackage().getVersionName()) < 0
                        && RSA256.equals(app.getAppPackage().getSigner()) && PackageUtil.isSupportedPackage(app.getAppPackage())) {
                    updatableList.add(tempApp);
                } else if (packageInfo.versionName.compareTo(tempApp.getAppPackage().getVersionName()) == 0
                        && packageInfo.versionCode < tempApp.getAppPackage().getVersionCode()
                        && RSA256.equals(app.getAppPackage().getSigner())
                        && PackageUtil.isSupportedPackage(app.getAppPackage())) {
                    updatableList.add(tempApp);
                }
            }
        }
        return updatableList;
    }

    public List<App> getAllApps() {
        return fetchAppsTask.getAppsByPackageName(getAllPackages());
    }

    public List<App> getInstalledApps(boolean showSystem) {
        return fetchAppsTask.getAppsByPackageName(getInstalledPackages(showSystem));
    }

    private List<String> getInstalledPackages(boolean showSystem) {
        List<String> packageList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        for (PackageInfo packageInfo : pm.getInstalledPackages(0)) {
            final String packageName = packageInfo.packageName;
            if (null != packageInfo.applicationInfo && !packageInfo.applicationInfo.enabled)
                continue;
            if (PackageUtil.isSystemApp(pm, packageName) && !showSystem)
                continue;
            /*if (!CertUtil.isFDroidApp(context, packageName))
                continue;*/
            packageList.add(packageName);
        }
        packageList = filterBlacklistedApps(packageList);
        return packageList;
    }

    private List<String> getAllPackages() {
        List<String> packageList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        for (PackageInfo packageInfo : pm.getInstalledPackages(0)) {
            final String packageName = packageInfo.packageName;
            if (null != packageInfo.applicationInfo && !packageInfo.applicationInfo.enabled)
                continue;
            packageList.add(packageName);
        }
        return packageList;
    }

    public List<String> filterBlacklistedApps(List<String> packageList) {
        packageList.removeAll(new BlacklistManager(context).get());
        return packageList;
    }
}
