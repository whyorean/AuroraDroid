package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;

import java.util.List;

public class AppsViewModel extends AndroidViewModel {

    private AppRepository appRepository;

    public AppsViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<App>> getNewAppsLiveData() {
        return appRepository.getAllNewApps(System.currentTimeMillis(), 3);
    }

    public LiveData<List<App>> getUpdatedAppsLiveData() {
        return appRepository.getAllUpdatedApps(System.currentTimeMillis(), 3);
    }

    public LiveData<List<App>> getCategoryAppsLiveData(String category) {
        return appRepository.getAllAppsByCategory(category);
    }

    public LiveData<List<App>> getRepoAppsLiveData(String repoId) {
        return appRepository.getAllAppsByRepositoryId(repoId);
    }
}
