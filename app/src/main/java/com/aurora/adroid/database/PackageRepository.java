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

    public LiveData<Package> getLivePackage(String packageName) {
        return packageDao.getLivePackageByPackageName(packageName);
    }

    public Package getAppPackage(String packageName) {
        return packageDao.getPackageByPackageName(packageName);
    }
}
