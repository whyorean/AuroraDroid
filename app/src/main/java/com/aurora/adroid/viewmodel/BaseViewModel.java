package com.aurora.adroid.viewmodel;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.aurora.adroid.model.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class BaseViewModel extends AndroidViewModel {

    protected CompositeDisposable disposable = new CompositeDisposable();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public List<App> sortList(List<App> appList) {
        Collections.sort(appList, (App1, App2) -> App1.getName().compareToIgnoreCase(App2.getName()));
        return appList;
    }

    public List<String> getInstalledPackages(boolean includeSystem) {
        List<String> packageList = new ArrayList<>();
        PackageManager packageManager = getApplication().getPackageManager();

        for (PackageInfo packageInfo : packageManager.getInstalledPackages(PackageManager.GET_META_DATA)) {
            final String packageName = packageInfo.packageName;

            if (packageInfo.applicationInfo != null && !packageInfo.applicationInfo.enabled //Filter Disabled Apps
                    || (packageManager.getLaunchIntentForPackage(packageName)) == null //Filter Non-Launchable Apps
                    || ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) && !includeSystem) //Filter System-Apps
                continue;

            packageList.add(packageName);
        }
        return packageList;
    }
}
