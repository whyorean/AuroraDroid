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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppPackageRepository;
import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.items.FavouriteItem;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.util.PackageUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FavouriteAppsModel extends BaseViewModel {


    private AppRepository appRepository;
    private AppPackageRepository appPackageRepository;

    private MutableLiveData<List<FavouriteItem>> data = new MutableLiveData<>();

    public FavouriteAppsModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        appPackageRepository = new AppPackageRepository(application);
    }

    public LiveData<List<FavouriteItem>> getFavouriteApps() {
        return data;
    }

    public void fetchFavoriteApps() {
        final FavouritesManager favouritesManager = new FavouritesManager(getApplication());
        final List<App> appList = favouritesManager.getFavouriteApps();
        if (appList != null && !appList.isEmpty()) {
            fetchFavouriteApps(appList);
        } else {
            data.setValue(new ArrayList<>());
        }
    }

    public void fetchFavouriteApps(List<App> favApps) {
        disposable.add(Observable.fromCallable(() -> favApps)
                .subscribeOn(Schedulers.io())
                .map(appList -> {
                    List<App> processedAppList = new ArrayList<>();
                    for (App app : appList) {
                        if (appRepository.isAvailable(app.getPackageName())) {

                            final AppPackage appPackage = appPackageRepository.getAppPackage(app.getPackageName(), app.getRepoId());

                            //Get all packages in the app-package
                            List<Package> packageList = appPackage.getPackageList();
                            List<Package> compatiblePackages = new ArrayList<>();

                            if (packageList != null && !packageList.isEmpty()) {
                                //Find best matching app package for the app
                                compatiblePackages = PackageUtil.markCompatiblePackages(packageList, null, true);
                                if (compatiblePackages != null) {
                                    app.setPkg(compatiblePackages.get(0));
                                }

                                app.setInstalled(PackageUtil.isInstalled(getApplication(), app.getPackageName()));
                                processedAppList.add(app);
                            }
                        }
                    }
                    return processedAppList;
                })
                .flatMap(apps -> Observable.fromIterable(apps).map(FavouriteItem::new))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favouriteItems -> data.setValue(favouriteItems), throwable -> throwable.printStackTrace()));
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}

