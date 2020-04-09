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

import com.aurora.adroid.model.items.BlacklistItem;
import com.aurora.adroid.task.InstalledAppsTask;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BlackListedAppsModel extends BaseViewModel {

    private MutableLiveData<List<BlacklistItem>> data = new MutableLiveData<>();

    public BlackListedAppsModel(@NonNull Application application) {
        super(application);
        fetchBlackListedApps();
    }

    public LiveData<List<BlacklistItem>> getBlacklistedItems() {
        return data;
    }

    public void fetchBlackListedApps() {
        disposable.add(Observable.fromCallable(() -> new InstalledAppsTask(getApplication())
                .getAllLocalApps())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(apps -> Observable
                        .fromIterable(apps)
                        .map(BlacklistItem::new))
                .toList()
                .subscribe(blacklistItems -> data.setValue(blacklistItems), throwable -> throwable.printStackTrace()));
    }

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }
}
