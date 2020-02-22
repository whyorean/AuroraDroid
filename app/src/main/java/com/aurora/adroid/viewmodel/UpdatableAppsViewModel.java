package com.aurora.adroid.viewmodel;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.database.PackageRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.util.CertUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UpdatableAppsViewModel extends BaseViewModel {

    private AppRepository appRepository;
    private PackageRepository packageRepository;
    private MutableLiveData<List<App>> liveAppList = new MutableLiveData<>();
    private PackageManager packageManager;

    public UpdatableAppsViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        packageRepository = new PackageRepository(application);
        packageManager = application.getPackageManager();
    }

    public MutableLiveData<List<App>> getAppsLiveData() {
        return liveAppList;
    }

    public void fetchUpdatableApps() {
        Observable.fromCallable(() -> getInstalledPackages(true))
                .map(packages -> {
                    final List<App> appList = new ArrayList<>();
                    for (String packageName : packages) {
                        final App app = appRepository.getAppByPackageName(packageName);
                        if (app != null) {
                            final Package appPackage = packageRepository.getAppPackage(packageName);
                            final PackageInfo packageInfo = PackageUtil
                                    .getPackageInfo(packageManager, app.getPackageName());
                            if (appPackage != null && packageInfo != null) {
                                final ComparableVersion installedVersion = new ComparableVersion(
                                        packageInfo.versionName + "." + packageInfo.versionCode);
                                final ComparableVersion repoVersion = new ComparableVersion(
                                        appPackage.getVersionName() + "." + appPackage.getVersionCode());
                                final String RSA256 = CertUtil.getSHA256(getApplication(),
                                        app.getPackageName());
                                if (installedVersion.compareTo(repoVersion) < 0
                                        && RSA256.equals(appPackage.getSigner())
                                        && (PackageUtil.isBestFitSupportedPackage(app.getAppPackage())
                                        || PackageUtil.isSupportedPackage(app.getAppPackage()))) {
                                    app.setAppPackage(appPackage);
                                    appList.add(app);
                                }
                            }
                        }
                    }
                    return appList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(appList -> liveAppList.setValue(appList))
                .doOnError(throwable -> Log.e("Failed to fetch updatable app list"))
                .subscribe();
    }
}
