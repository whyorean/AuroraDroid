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
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.model.items.BlacklistItem;
import com.aurora.adroid.util.PackageUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BlackListedAppsModel extends BaseViewModel {

    private MutableLiveData<List<BlacklistItem>> data = new MutableLiveData<>();

    public BlackListedAppsModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<BlacklistItem>> getBlacklistedItems() {
        return data;
    }

    public void fetchBlackListedApps() {
        fetchBlackListedApps(new HashSet<>());
    }

    public void fetchBlackListedApps(Set<String> packageNames) {
        PackageManager packageManager = getApplication().getPackageManager();
        BlacklistManager blacklistManager = new BlacklistManager(getApplication());
        disposable.add(Observable.fromCallable(() -> getInstalledPackages(true))
                .subscribeOn(Schedulers.io())
                .flatMap(stringList -> Observable.fromIterable(stringList)
                        .map(s -> PackageUtil.getAppFromPackageName(packageManager, s, true))
                        .map(app -> {
                            BlacklistItem blacklistItem = new BlacklistItem(app);
                            blacklistItem.setSelected(blacklistManager.isBlacklisted(app.getPackageName()));

                            //Blacklist imported packages
                            if (packageNames.contains(app.getPackageName()))
                                blacklistItem.setSelected(true);

                            return blacklistItem;
                        }))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(blacklistItems -> data.setValue(blacklistItems), Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}
