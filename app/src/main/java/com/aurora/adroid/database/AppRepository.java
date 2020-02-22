package com.aurora.adroid.database;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.aurora.adroid.model.App;

import java.util.List;

public class AppRepository {

    private AppDao appDao;

    public AppRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        appDao = appDatabase.appDao();
    }

    public LiveData<List<App>> getAllApps() {
        return appDao.getAllApps();
    }

    public LiveData<List<App>> getAllUpdatedApps(Long refTime, int weekCount) {
        return appDao.getLatestUpdatedApps(refTime, weekCount);
    }

    public LiveData<List<App>> getAllNewApps(Long refTime, int weekCount) {
        return appDao.getLatestAddedApps(refTime, weekCount);
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

    public List<App> searchApps(SimpleSQLiteQuery sqLiteQuery) {
        return appDao.searchApps(sqLiteQuery);
    }

    public LiveData<List<App>> getAllAppsByRepositoryId(String repoId) {
        return appDao.searchAppsByRepository("%" + repoId + "%");
    }

    public String getScreenShots(String packageName) {
        return appDao.getPhoneScreenshots(packageName);
    }
}
