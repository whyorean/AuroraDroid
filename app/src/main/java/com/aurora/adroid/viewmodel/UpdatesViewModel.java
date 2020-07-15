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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppPackageRepository;
import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.model.items.UpdatesItem;
import com.aurora.adroid.model.v2.AppPackage;
import com.aurora.adroid.util.CertUtil;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UpdatesViewModel extends BaseViewModel {

    private AppRepository appRepository;
    private AppPackageRepository appPackageRepository;
    private MutableLiveData<List<UpdatesItem>> data = new MutableLiveData<>();
    private PackageManager packageManager;

    public UpdatesViewModel(@NonNull Application application) {
        super(application);
        this.appRepository = new AppRepository(application);
        this.appPackageRepository = new AppPackageRepository(application);
        this.packageManager = application.getPackageManager();
        fetchUpdatableApps();
    }

    public MutableLiveData<List<UpdatesItem>> getAppsLiveData() {
        return data;
    }

    public void fetchUpdatableApps() {
        disposable.add(Observable.fromCallable(this::getInstalledPackages)
                .subscribeOn(Schedulers.io())
                .map(packageNames -> {
                    final List<UpdatesItem> updatesItemList = new ArrayList<>();
                    for (String packageName : packageNames) {
                        //Process only those apps which are available.
                        if (appRepository.isAvailable(packageName)) {
                            List<App> appList = appRepository.getAppsByPackageName(packageName);
                            for (App app : appList) {

                                //Get app-package associated with this app, in specific repo.
                                AppPackage appPackage = appPackageRepository.getAppPackage(packageName, app.getRepoId());

                                //Get all packages in the app-package
                                List<Package> packageList = appPackage.getPackageList();

                                //Get installed app signer
                                String RSA256 = CertUtil.getSHA256(getApplication(), app.getPackageName());

                                List<Package> compatiblePackages = new ArrayList<>();

                                if (packageList != null && !packageList.isEmpty()) {
                                    //Find best matching app package for the app
                                    compatiblePackages = PackageUtil.markCompatiblePackages(packageList, RSA256, true);
                                }

                                if (compatiblePackages != null) {
                                    final PackageInfo packageInfo = PackageUtil.getPackageInfo(packageManager, app.getPackageName());
                                    final boolean allowSuggestedOnly = Util.isSuggestedUpdatesEnabled(getApplication());

                                    for (Package pkg : compatiblePackages) {
                                        if (packageInfo != null) {
                                            if (pkg.isCompatible() && PackageUtil.isUpdatableVersion(getApplication(), pkg, packageInfo)) {
                                                if (allowSuggestedOnly) {
                                                    if (PackageUtil.isSuggestedUpdatableVersion(packageInfo, app, pkg)) {
                                                        app.setPkg(pkg);
                                                        updatesItemList.add(new UpdatesItem(app));
                                                    }
                                                } else {
                                                    app.setPkg(pkg);
                                                    updatesItemList.add(new UpdatesItem(app));
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return updatesItemList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updatesItems -> data.setValue(updatesItems), Throwable::printStackTrace));
    }
}
