package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InstalledAppsViewModel extends BaseViewModel {

    private AppRepository appRepository;
    private MutableLiveData<List<App>> liveAppList = new MutableLiveData<>();

    public InstalledAppsViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public MutableLiveData<List<App>> getAppsLiveData() {
        return liveAppList;
    }

    public void fetchNewApps(boolean includeSystem) {
        Observable.fromCallable(() -> getInstalledPackages(includeSystem))
                .map(packages -> {
                    List<App> appList = new ArrayList<>();
                    for (String packageName : packages) {
                        App app = appRepository.getAppByPackageName(packageName);
                        if (app == null)
                            continue;
                        appList.add(app);
                    }
                    return appList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(appList -> liveAppList.setValue(appList))
                .doOnError(throwable -> Log.e("Failed to fetch installed app list"))
                .subscribe();
    }
}
