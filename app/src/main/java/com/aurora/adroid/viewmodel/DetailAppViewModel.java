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
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppPackageRepository;
import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.util.PackageUtil;

import org.apache.commons.lang3.StringUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailAppViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private AppPackageRepository appPackageRepository;
    private MutableLiveData<App> liveApp = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public DetailAppViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        appPackageRepository = new AppPackageRepository(application);
    }

    public MutableLiveData<App> getLiveApp() {
        return liveApp;
    }

    public void getFullAppByPackageName(String packageName, String repoName) {
        disposable.add(Observable.fromCallable(() -> StringUtils.isEmpty(repoName)
                ? appRepository.getAppByPackageName(packageName)
                : appRepository.getAppByPackageNameAndRepo(packageName, repoName))
                .map(app -> {
                    final AppPackage appPackage = appPackageRepository.getAppPackage(app.getPackageName(), app.getRepoId());
                    app.setAppPackage(appPackage);
                    app.setPkg(PackageUtil.getOptimumPackage(appPackage.getPackageList(),"",false));
                    return app;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(app -> liveApp.setValue(app), Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}
