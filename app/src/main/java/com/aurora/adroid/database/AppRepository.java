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

package com.aurora.adroid.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.aurora.adroid.model.App;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppRepository {

    private AppDao appDao;

    public AppRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        appDao = appDatabase.appDao();
    }

    public LiveData<List<App>> getAllApps() {
        return appDao.getAllApps();
    }

    public LiveData<List<App>> getAllUpdatedApps(Long refTime, int days) {
        return appDao.getLatestUpdatedApps(refTime, TimeUnit.DAYS.toMillis(days), TimeUnit.DAYS.toMillis(7));
    }

    public LiveData<List<App>> getAllNewApps(Long refTime, int days) {
        return appDao.getLatestAddedApps(refTime, TimeUnit.DAYS.toMillis(days));
    }

    public LiveData<List<App>> getAllAppsByCategory(String category) {
        category = category.replace("&", "%");
        return appDao.searchAppsByCategory("%" + category + "%");
    }

    public LiveData<App> getLiveAppByPackageName(String packageName) {
        return appDao.getLiveAppByPackageName(packageName);
    }

    public App getAppByPackageName(String packageName) {
        return appDao.getAppByPackageName(packageName);
    }

    public List<App> getAppsByPackageName(String packageName) {
        return appDao.getAppsByPackageName(packageName);
    }

    public App getAppByPackageNameAndRepo(String packageName, String repoName) {
        return appDao.getAppByPackageNameAndRepo(packageName, repoName);
    }

    public LiveData<List<App>> getAllAppsByRepositoryId(String repoId) {
        return appDao.searchAppsByRepository("%" + repoId + "%");
    }

    public LiveData<List<App>> getAllAppsByDeveloper(String authorName) {
        return appDao.getAppsByAuthorName(authorName);
    }

    public List<String> getAllPackages() {
        return appDao.getAllPackages();
    }

    public boolean isAvailable(String packageName) {
        return appDao.isAvailable(packageName);
    }
}
