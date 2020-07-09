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

import com.aurora.adroid.model.v2.AppPackage;

import java.util.List;

public class AppPackageRepository {

    private AppPackageDao appPackageDao;

    public AppPackageRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        appPackageDao = appDatabase.appPackageDao();
    }

    public LiveData<List<AppPackage>> getAllPackagesLive(String packageName) {
        return appPackageDao.getLivePackageList(packageName);
    }

    public List<AppPackage> getAllPackages(String packageName) {
        return appPackageDao.getAppPackageList(packageName);
    }

    public LiveData<AppPackage> getLiveAppPackage(String packageName, String repoId) {
        return appPackageDao.getLiveAppPackageList(packageName, repoId);
    }

    public AppPackage getAppPackage(String packageName, String repoId) {
        return appPackageDao.getAppPackageList(packageName, repoId);
    }
}
