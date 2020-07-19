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

package com.aurora.adroid.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.Constants;
import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.items.InstalledItem;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;

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
        PackageManager packageManager = getApplication().getPackageManager();
        disposable.add(Observable.fromCallable(() -> getInstalledPackages(!userOnly))
                .subscribeOn(Schedulers.io())
                .flatMap(packageNames -> Observable.fromIterable(packageNames)
                        .filter(packageName -> appRepository.isAvailable(packageName))
                        .map(packageName -> PackageUtil.getAppFromPackageName(packageManager, packageName, true))
                        .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                        .map(InstalledItem::new))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(installedItems -> data.setValue(installedItems), Throwable::printStackTrace));
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
