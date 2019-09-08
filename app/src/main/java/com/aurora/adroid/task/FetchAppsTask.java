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

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.aurora.adroid.database.AppDao;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.database.PackageDao;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FetchAppsTask extends ContextWrapper {

    private AppDatabase appDatabase;
    private AppDao appDao;
    private PackageDao packageDao;

    public FetchAppsTask(Context context) {
        super(context);
        appDatabase = AppDatabase.getAppDatabase(this);
        appDao = appDatabase.appDao();
        packageDao = appDatabase.packageDao();
    }

    public List<App> fetchAllApps() {
        List<App> appList = appDao.getAllApps();
        appList = removeDuplicates(appList);
        return appList;
    }

    public synchronized List<App> searchApps(String query) {
        final String rawQuery = "%" + query + "%";
        final String sqlQuery = "SELECT * FROM app WHERE (name like ?) OR (summary like ?) OR (`en-US-summary` like ?);";

        List<String> args = new ArrayList<>();
        args.add(rawQuery);
        args.add(rawQuery);
        args.add(rawQuery);
        SimpleSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(sqlQuery, args.toArray());

        List<App> appList = appDao.searchApps(sqLiteQuery);
        appList = removeDuplicates(appList);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public List<App> getAppsByCategory(String category) {
        category = category.replace("&", "%");
        List<App> appList = appDao.searchAppsByCategory("%" + category + "%");
        appList = removeDuplicates(appList);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public List<App> getAppsByRepository(String repoId) {
        List<App> appList = appDao.searchAppsByRepository("%" + repoId + "%");
        appList = removeDuplicates(appList);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public App getAppByName(String name) {
        return appDao.getAppByName(name);
    }

    public App getAppByPackageName(String packageName) {
        App app = appDao.getAppByPackageName(packageName);
        app.setAppPackage(getPackageByName(packageName));
        return app;
    }

    public List<App> getAppsByPackageName(List<String> packageNames) {
        List<App> appList = appDao.getAppsByPackageName(packageNames);
        appList = removeDuplicates(appList);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public App getFullAppByPackageName(String packageName) {
        App app = appDao.getAppByPackageName(packageName);
        List<Package> pkgList = packageDao.getPackageListByPackageName(packageName);
        if (!pkgList.isEmpty())
            app.setPackageList(pkgList);
        app.setScreenShots(appDao.getPhoneScreenshots(packageName));
        return app;
    }

    public List<App> findAppsByName(String name) {
        return appDao.findAppsByName(name);
    }

    public List<App> getLatestUpdatedApps(int weekCount) {
        List<App> appList = appDao.getLatestUpdatedApps(Calendar.getInstance().getTimeInMillis(), weekCount);
        appList = removeDuplicates(appList);
        for (App app : appList)
            app.setAppPackage(getPackageByName(app.getPackageName()));
        return appList;
    }

    public List<App> getLatestAddedApps(int weekCount) {
        List<App> appList = appDao.getLatestAddedApps(Calendar.getInstance().getTimeInMillis(), weekCount);
        appList = removeDuplicates(appList);
        for (App app : appList) {
            app.setAppPackage(getPackageByName(app.getPackageName()));
        }
        return appList;
    }

    public Package getPackageByName(String packageName) {
        return packageDao.getPackageByPackageName(packageName);
    }

    public List<App> removeDuplicates(List<App> appList) {
        Set<App> unique = new LinkedHashSet<App>(appList);
        appList = new ArrayList<App>(unique);
        return appList;
    }
}
