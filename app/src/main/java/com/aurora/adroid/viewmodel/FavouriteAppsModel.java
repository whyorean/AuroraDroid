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

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.FavouriteItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FavouriteAppsModel extends BaseViewModel {

    private AppRepository appRepository;

    private MutableLiveData<List<FavouriteItem>> data = new MutableLiveData<>();

    public FavouriteAppsModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        fetchFavouriteApps();
    }

    public LiveData<List<FavouriteItem>> getFavouriteApps() {
        return data;
    }

    public void fetchFavouriteApps() {
        final FavouritesManager favouritesManager = new FavouritesManager(getApplication());
        final List<String> packageList1 = favouritesManager.getFavouritePackages();
        if (packageList1.size() > 0) {
            disposable.add(Observable.fromCallable(() -> new FavouritesManager(getApplication())
                    .getFavouritePackages())
                    .subscribeOn(Schedulers.io())
                    .map(packageList -> {
                        List<App> appList = new ArrayList<>();
                        for (String packageName : packageList) {

                            final App app = appRepository.getAppByPackageName(packageName);

                            if (app == null) //Filter non-existing apps in current synced repos
                                continue;

                            appList.add(app);
                        }
                        return appList;
                    })
                    .map(apps -> sortList(apps))
                    .flatMap(apps -> Observable.fromIterable(apps).map(FavouriteItem::new))
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(favouriteItems -> data.setValue(favouriteItems), throwable -> throwable.printStackTrace()));
        } else {
            data.setValue(new ArrayList<>());
        }
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}

