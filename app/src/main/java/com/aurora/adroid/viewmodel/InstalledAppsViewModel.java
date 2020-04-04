package com.aurora.adroid.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.Constants;
import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.InstalledItem;
import com.aurora.adroid.task.InstalledAppsTask;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InstalledAppsViewModel extends BaseViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppRepository appRepository;
    private boolean userOnly;
    private SharedPreferences sharedPreferences;

    private MutableLiveData<List<InstalledItem>> data = new MutableLiveData<>();

    public InstalledAppsViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = Util.getPrefs(getApplication());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        userOnly = PrefUtil.getBoolean(application, Constants.PREFERENCE_INCLUDE_SYSTEM);

        appRepository = new AppRepository(application);

        fetchInstalledApps(userOnly);
    }

    public MutableLiveData<List<InstalledItem>> getData() {
        return data;
    }

    public void fetchInstalledApps(boolean userOnly) {
        disposable.add(Observable.fromCallable(() -> new InstalledAppsTask(getApplication())
                .getInstalledApps())
                .subscribeOn(Schedulers.io())
                .map(apps -> filterList(apps, userOnly))
                .map(apps -> sortList(apps))
                .flatMap(apps -> Observable
                        .fromIterable(apps)
                        .map(InstalledItem::new))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(installedItems -> data.setValue(installedItems), throwable -> throwable.printStackTrace()));
    }

    private List<App> filterList(List<App> appList, boolean userOnly) {
        List<App> filteredList = new ArrayList<>();
        for (App app : appList) {

            if (userOnly && app.isSystemApp()) //Filter system apps
                continue;

            final App repoApp = appRepository.getAppByPackageName(app.getPackageName());

            if (repoApp == null) //Filter non-existing apps in current synced repos
                continue;

            filteredList.add(app);
        }
        return filteredList;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.PREFERENCE_INCLUDE_SYSTEM)) {
            userOnly = PrefUtil.getBoolean(getApplication(), Constants.PREFERENCE_INCLUDE_SYSTEM);
            fetchInstalledApps(userOnly);
        }
    }

    @Override
    protected void onCleared() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        disposable.dispose();
        super.onCleared();
    }
}
