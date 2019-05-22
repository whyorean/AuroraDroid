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

import com.aurora.adroid.database.AppDao;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.PackageDao;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;

import java.util.Calendar;
import java.util.List;

public class FetchAppsTask extends ContextWrapper {

    public FetchAppsTask(Context context) {
        super(context);
    }

    public List<App> fetchAllApps() {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        return appDao.getAllApps();
    }

    public List<App> searchApps(String query) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        List<App> appList = appDao.searchApps("%" + query + "%");
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public List<App> getAppsByCategory(String category) {
        category = category.replace("&", "%");
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        List<App> appList = appDao.searchAppsByCategory("%" + category + "%");
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public App getAppByName(String name) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        return appDao.getAppByName(name);
    }

    public App getAppByPackageName(String packageName) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        App app = appDao.getAppByPackageName(packageName);
        app.setAppPackage(getPackageByName(packageName));
        return app;
    }

    public List<App> getAppsByPackageName(List<String> packageNames) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        List<App> appList = appDao.getAppsByPackageName(packageNames);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public App getFullAppByPackageName(String packageName) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        PackageDao packageDao = appDatabase.packageDao();
        App app = appDao.getAppByPackageName(packageName);
        List<Package> pkgList = packageDao.getPackageListByPackageName(packageName);
        if (!pkgList.isEmpty())
            app.setPackageList(pkgList);
        app.setScreenShots(appDao.getPhoneScreenshots(packageName));
        return app;
    }

    public List<App> findAppsByName(String name) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        return appDao.findAppsByName(name);
    }

    public List<App> getLatestUpdatedApps(int weekCount) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        List<App> appList = appDao.getLatestUpdatedApps(Calendar.getInstance().getTimeInMillis(), weekCount);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public List<App> getLatestAddedApps(int weekCount) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        AppDao appDao = appDatabase.appDao();
        List<App> appList = appDao.getLatestAddedApps(Calendar.getInstance().getTimeInMillis(), weekCount);
        for (App app : appList) {
            app.setAppPackage(getPackageByName(app.getPackageName()));
        }
        return appList;
    }

    public Package getPackageByName(String packageName) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(this);
        PackageDao packageDao = appDatabase.packageDao();
        return packageDao.getPackageByPackageName(packageName);
    }
}
