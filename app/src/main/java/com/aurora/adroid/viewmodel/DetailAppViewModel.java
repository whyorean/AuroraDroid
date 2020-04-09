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

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.database.PackageRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.util.CertUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailAppViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private PackageRepository packageRepository;
    private MutableLiveData<App> liveApp = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public DetailAppViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        packageRepository = new PackageRepository(application);
    }

    public MutableLiveData<App> getLiveApp() {
        return liveApp;
    }

    public void getFullAppByPackageName(String packageName, String repoName) {
        disposable.add(Observable.fromCallable(() -> StringUtils.isEmpty(repoName)
                ? appRepository.getAppByPackageName(packageName)
                : appRepository.getAppByPackageNameAndRepo(packageName, repoName))
                .map(app -> {

                    final List<Package> pkgList = StringUtils.isEmpty(repoName)
                            ? packageRepository.getAllPackages(packageName)
                            : packageRepository.getAllPackages(packageName, repoName);

                    if (!pkgList.isEmpty()) {
                        final String RSA256 = CertUtil.getSHA256(getApplication(), app.getPackageName());
                        app.setPackageList(pkgList, RSA256, true);
                    }

                    return app;
                })
                .map(app -> {
                    final String screenshots = appRepository.getScreenShots(packageName);
                    app.setScreenShots(screenshots);
                    return app;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(app -> liveApp.setValue(app), throwable -> throwable.printStackTrace()));
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}
