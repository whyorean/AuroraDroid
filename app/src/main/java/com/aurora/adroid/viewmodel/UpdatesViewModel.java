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
import com.aurora.adroid.model.items.UpdatesItem;
import com.aurora.adroid.util.CertUtil;
import com.aurora.adroid.util.PackageUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UpdatesViewModel extends BaseViewModel {

    private AppRepository appRepository;
    private PackageRepository packageRepository;
    private MutableLiveData<List<UpdatesItem>> data = new MutableLiveData<>();
    private PackageManager packageManager;

    public UpdatesViewModel(@NonNull Application application) {
        super(application);
        this.appRepository = new AppRepository(application);
        this.packageRepository = new PackageRepository(application);
        this.packageManager = application.getPackageManager();
        fetchUpdatableApps();
    }

    public MutableLiveData<List<UpdatesItem>> getAppsLiveData() {
        return data;
    }

    public void fetchUpdatableApps() {
        disposable.add(Observable.fromCallable(() -> getInstalledPackages(true))
                .subscribeOn(Schedulers.io())
                .map(packages -> {
                    final List<App> appList = new ArrayList<>();
                    for (String packageName : packages) {
                        final App app = appRepository.getAppByPackageName(packageName);
                        if (app != null) {
                            final Package pkg = packageRepository.getAppPackage(packageName);
                            final PackageInfo packageInfo = PackageUtil.getPackageInfo(packageManager, app.getPackageName());



                            if (pkg != null && packageInfo != null) {
                                final String RSA256 = CertUtil.getSHA256(getApplication(), app.getPackageName());

                                if (pkg.getVersionCode() > packageInfo.versionCode
                                        && (PackageUtil.isCompatibleVersion(getApplication(),pkg, packageInfo))
                                        && RSA256.equals(pkg.getSigner())
                                        && (PackageUtil.isBestFitSupportedPackage(app.getAppPackage()) || PackageUtil.isSupportedPackage(app.getAppPackage()))) {
                                    app.setAppPackage(pkg);
                                    appList.add(app);
                                }
                            }
                        }
                    }
                    return appList;
                })
                .map(this::sortList)
                .flatMap(apps -> Observable
                        .fromIterable(apps)
                        .map(UpdatesItem::new))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updatesItems -> data.setValue(updatesItems), Throwable::printStackTrace));
    }
}
