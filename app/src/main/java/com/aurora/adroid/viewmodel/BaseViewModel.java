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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.aurora.adroid.manager.BlacklistManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class BaseViewModel extends AndroidViewModel {

    protected CompositeDisposable disposable = new CompositeDisposable();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public List<String> getInstalledPackages() {
        List<String> packageList = getInstalledPackages(true);
        packageList = filterBlacklistedApps(packageList);
        return packageList;
    }

    public List<String> getInstalledPackages(boolean includeSystem) {
        List<String> packageList = new ArrayList<>();
        PackageManager packageManager = getApplication().getPackageManager();

        for (PackageInfo packageInfo : packageManager.getInstalledPackages(PackageManager.GET_META_DATA)) {
            final String packageName = packageInfo.packageName;

            if (packageName == null || packageInfo.applicationInfo == null)
                continue;

            if (!packageInfo.applicationInfo.enabled
                    || ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    && !includeSystem) //Filter System-Apps
                continue;

            packageList.add(packageName);
        }
        return packageList;
    }

    public List<String> filterBlacklistedApps(List<String> packageList) {
        packageList.removeAll(new BlacklistManager(getApplication()).getBlacklistedPackages());
        return packageList;
    }
}
