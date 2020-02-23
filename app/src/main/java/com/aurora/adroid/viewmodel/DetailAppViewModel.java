package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.database.PackageRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DetailAppViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private PackageRepository packageRepository;
    private MutableLiveData<App> liveApp = new MutableLiveData<>();

    public DetailAppViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        packageRepository = new PackageRepository(application);
    }

    public MutableLiveData<App> getLiveApp() {
        return liveApp;
    }

    public void getFullAppByPackageName(String packageName, String repoName) {
        Observable.fromCallable(() -> StringUtils.isEmpty(repoName)
                ? appRepository.getAppByPackageName(packageName)
                : appRepository.getAppByPackageNameAndRepo(packageName, repoName))
                .map(app -> {
                    List<Package> pkgList = StringUtils.isEmpty(repoName)
                            ? packageRepository.getAllPackages(packageName)
                            : packageRepository.getAllPackages(packageName, repoName);
                    if (!pkgList.isEmpty())
                        app.setPackageList(pkgList);
                    return app;
                })
                .map(app -> {
                    String screenshots = appRepository.getScreenShots(packageName);
                    app.setScreenShots(screenshots);
                    return app;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(app -> liveApp.setValue(app))
                .doOnError(throwable -> Log.e("Failed to fetch app details"))
                .doOnError(throwable -> throwable.printStackTrace())
                .subscribe();
    }
}
