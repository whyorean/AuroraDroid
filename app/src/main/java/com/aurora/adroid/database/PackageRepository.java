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

import com.aurora.adroid.model.Package;

import java.util.List;

public class PackageRepository {

    private PackageDao packageDao;

    public PackageRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        packageDao = appDatabase.packageDao();
    }

    public LiveData<List<Package>> getAllPackagesLive(String packageName) {
        return packageDao.getLivePackageListByPackageName(packageName);
    }

    public List<Package> getAllPackages(String packageName) {
        return packageDao.getPackageListByPackageName(packageName);
    }

    public List<Package> getAllPackages(String packageName, String repoName) {
        return packageDao.getPackageListByPackageNameAndRepo(packageName, repoName);
    }

    public LiveData<Package> getLivePackage(String packageName) {
        return packageDao.getLivePackageByPackageName(packageName);
    }

    public Package getAppPackage(String packageName) {
        return packageDao.getPackageByPackageName(packageName);
    }
}
