package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;

import java.util.List;

public class SearchAppsViewModel extends AndroidViewModel {

    private AppRepository appRepository;

    public SearchAppsViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<App>> getAppsLiveData() {
        return appRepository.getAllApps();
    }
}
